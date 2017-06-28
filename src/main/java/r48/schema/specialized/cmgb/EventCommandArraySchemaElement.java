/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized.cmgb;

import gabien.ui.IFunction;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
import r48.ArrayUtils;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.*;
import r48.schema.AggregateSchemaElement;
import r48.schema.ArrayElementSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.SubwindowSchemaElement;
import r48.schema.arrays.ArraySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.Collections;
import java.util.LinkedList;

/**
 * ArraySchemaElement + some eventcommand specific stuff to automatically correct issues.
 * (On top of it's previous behaviors, this is now responsible for indent. It monitors subelements *anyway*,
 * and moving the logic here allows me to cut off some of SchemaPath's rube-goldberg-iness.
 * Anything to simplify that thing. Jun 2, 2017.)
 * Created on 1/2/17.
 */
public class EventCommandArraySchemaElement extends ArraySchemaElement {
    private final CMDB database;
    private final RPGCommandSchemaElement baseElement;

    public EventCommandArraySchemaElement(SchemaElement a, SchemaElement b, CMDB db, boolean indentControl) {
        super(0, false);
        baseElement = new RPGCommandSchemaElement(a, b, db, indentControl);
        // gets rid of subwindows & proxies
        database = db;
    }

    public RPGCommandSchemaElement getPureRC() {
        SchemaElement eventCommand = baseElement;
        while (eventCommand instanceof IProxySchemaElement)
            eventCommand = ((IProxySchemaElement) eventCommand).getEntry();
        return (RPGCommandSchemaElement) eventCommand;
    }

    @Override
    public boolean monitorsSubelements() {
        return true;
    }

    @Override
    public boolean autoCorrectArray(RubyIO array, SchemaPath path) {
        if (!SDB.allowControlOfEventCommandIndent)
            return false;
        boolean needsEndingBlock = false;
        if (array.arrVal.length == 0) {
            needsEndingBlock = database.listLeaveCmd != -1;
        } else {
            if (array.arrVal[array.arrVal.length - 1].getInstVarBySymbol("@code").fixnumVal != database.listLeaveCmd)
                needsEndingBlock = database.listLeaveCmd != -1;
        }

        LinkedList<RubyIO> arr = new LinkedList<RubyIO>();
        Collections.addAll(arr, array.arrVal);

        if (needsEndingBlock) {
            // 0 so that the code won't combust from lacking an array
            RubyIO c = SchemaPath.createDefaultValue(baseElement, new RubyIO().setFX(database.listLeaveCmd));
            c.getInstVarBySymbol("@code").fixnumVal = database.listLeaveCmd;
            arr.add(c);
        }

        boolean modified = needsEndingBlock;

        // NOTE: This method is deliberately awkward to allow for the concurrent modification...
        // Attempting to 'fix' it will only make it worse.
        boolean lastWasBlockLeave = false;
        int lastCode = -1;

        // Indent tracking
        int indent = 0;

        // Note that this array can grow as it's being searched.
        for (int i = 0; i < arr.size(); i++) {
            RubyIO commandTarg = arr.get(i);
            int code = (int) commandTarg.getInstVarBySymbol("@code").fixnumVal;
            RPGCommand rc = database.knownCommands.get(code);
            if (rc != null) {
                // Indent stuff
                indent += rc.indentPre;
                if (baseElement.allowControlOfIndent) {
                    if (indent != commandTarg.getInstVarBySymbol("@indent").fixnumVal) {
                        commandTarg.getInstVarBySymbol("@indent").fixnumVal = indent;
                        modified = true;
                    }
                }
                indent += rc.indentPost.apply(commandTarg.getInstVarBySymbol("@parameters"));
                // Group Behavior
                if (rc.groupBehavior != null)
                    modified |= rc.groupBehavior.correctElement(arr, i, commandTarg);
                //

                if (rc.needsBlockLeavePre) {
                    if (!lastWasBlockLeave) {
                        if (rc.blockLeaveReplacement != lastCode) {
                            RubyIO c = SchemaPath.createDefaultValue(baseElement, new RubyIO().setFX(0));
                            c.getInstVarBySymbol("@code").fixnumVal = database.blockLeaveCmd;
                            if (baseElement.allowControlOfIndent)
                                c.getInstVarBySymbol("@indent").fixnumVal = commandTarg.getInstVarBySymbol("@indent").fixnumVal + 1;
                            arr.add(i, c);
                            // About to re-handle the same code.
                            lastWasBlockLeave = true;
                            // What to do here depends on a few things. They'll be handled in CMDB.
                            lastCode = database.blockLeaveCmd;
                            modified = true;
                            continue;
                        }
                    }
                }
                lastWasBlockLeave = rc.typeBlockLeave;
            } else {
                lastWasBlockLeave = false;
            }
            lastCode = code;
        }

        if (modified)
            array.arrVal = arr.toArray(new RubyIO[0]);
        return modified;
    }

    private int getGroupLengthCore(RubyIO[] arr, int j) {
        RubyIO commandTarg = arr[j];
        int code = (int) commandTarg.getInstVarBySymbol("@code").fixnumVal;
        RPGCommand rc = database.knownCommands.get(code);
        if (rc != null)
            if (rc.groupBehavior != null)
                return rc.groupBehavior.getGroupLength(arr, j);
        return 0;
    }

    @Override
    public int getGroupLength(RubyIO[] arr, int j) {
        int l = getGroupLengthCore(arr, j);
        if (l == 0)
            return 1;
        return l;
    }

    @Override
    protected SchemaElement getElementSchema(int j) {
        return new SubwindowSchemaElement(baseElement, new IFunction<RubyIO, String>() {
            @Override
            public String apply(RubyIO rubyIO) {
                return database.buildCodename(rubyIO, true);
            }
        });
    }

    private SchemaElement getGroupElement(RubyIO[] arr, final int start) {
        // Uhoh.
        final int length;
        boolean addRemove = false;
        if (start < arr.length) {
            int p = getGroupLengthCore(arr, start);
            if (p == 0) {
                length = 1;
            } else {
                length = p;
                addRemove = true;
            }
        } else {
            final String text = TXDB.get("The group is invalid.");
            return new SchemaElement() {
                @Override
                public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
                    return new UILabel(text, FontSizes.schemaFieldTextHeight);
                }

                @Override
                public int maxHoldingHeight() {
                    return UILabel.getRecommendedSize(text, FontSizes.schemaFieldTextHeight).height;
                }

                @Override
                public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                }
            };
        }
        int iSize = addRemove ? 1 : 0;
        SchemaElement[] group = new SchemaElement[length + iSize];
        RPGCommandSchemaElement rcse = getPureRC();
        for (int i = 0; i < group.length - iSize; i++) {
            boolean elemAddRemove = addRemove && (i != 0);
            group[i] = new ArrayElementSchemaElement(start + i, "", rcse, elemAddRemove ? "" : null, elemAddRemove);
            if (i == 0)
                rcse = rcse.hideHeaderVer();
        }
        final String addText = TXDB.get("Add to group...");
        if (addRemove) {
            group[group.length - 1] = new SchemaElement() {
                @Override
                public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
                    return new UITextButton(FontSizes.schemaButtonTextHeight, addText, new Runnable() {
                        @Override
                        public void run() {
                            RubyIO commandTarg = target.arrVal[start];
                            int code = (int) commandTarg.getInstVarBySymbol("@code").fixnumVal;
                            RPGCommand rc = database.knownCommands.get(code);
                            if (rc != null)
                                if (rc.groupBehavior != null) {
                                    RubyIO ne = SchemaPath.createDefaultValue(baseElement, null);
                                    ne.getInstVarBySymbol("@code").fixnumVal = rc.groupBehavior.getAdditionCode();
                                    ArrayUtils.insertRioElement(target, ne, start + length);
                                    path.changeOccurred(false);
                                }
                        }
                    });
                }

                @Override
                public int maxHoldingHeight() {
                    return UITextButton.getRecommendedSize(addText, FontSizes.schemaButtonTextHeight).height;
                }

                @Override
                public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                }
            };
        }
        return new AggregateSchemaElement(group);
    }

    @Override
    protected SchemaElement getElementContextualSchema(RubyIO[] arr, final int start, final int length) {
        return new SubwindowSchemaElement(new SchemaElement() {
            @Override
            public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
                return getGroupElement(target.arrVal, start).buildHoldingEditor(target, launcher, path);
            }

            @Override
            public int maxHoldingHeight() {
                throw new RuntimeException("Cannot use this here");
            }

            @Override
            public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                getGroupElement(target.arrVal, start).modifyVal(target, path, setDefault);
            }
        }, new IFunction<RubyIO, String>() {
            @Override
            public String apply(RubyIO rubyIO) {
                String tx = database.buildCodename(rubyIO.arrVal[start], true);
                if (getGroupLengthCore(rubyIO.arrVal, start) != 0)
                    tx += TXDB.get(" (...)");
                return tx;
            }
        });
    }

    @Override
    public int maxHoldingHeight() {
        return 128;
    }
}
