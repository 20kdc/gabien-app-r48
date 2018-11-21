/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.cmgb;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.RPGCommand;
import r48.dbs.SDB;
import r48.dbs.TXDB;
import r48.schema.AggregateSchemaElement;
import r48.schema.ArrayElementSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.SubwindowSchemaElement;
import r48.schema.arrays.ArraySchemaElement;
import r48.schema.arrays.StandardArrayInterface;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * ArraySchemaElement + some eventcommand specific stuff to automatically correct issues.
 * (On top of it's previous behaviors, this is now responsible for indent. It monitors subelements *anyway*,
 * and moving the logic here allows me to cut off some of SchemaPath's rube-goldberg-iness.
 * Anything to simplify that thing. Jun 2, 2017.)
 *
 * OK, so here's the March 13th, 2018 accurate notes:
 *
 * The direct subelements of this are halfsplit schema elements designed to generate the indentation reliably.
 * The right side of those is a set of subwindows containing super-special schema elements,
 *  that are tagged with the specific command RubyIO to look for.
 * This allows editing to work properly with groups *and* for it to still be able to handle command movement and deletion.
 * getGroupElement is run inside of these after the target has been located,
 *  in order to finish up the specific interface we're showing to the user at a given time.
 *
 * Created on 1/2/17.
 */
public class EventCommandArraySchemaElement extends ArraySchemaElement {
    private final CMDB database;
    private final RPGCommandSchemaElement baseElement;

    public EventCommandArraySchemaElement(SchemaElement a, SchemaElement b, CMDB db, boolean indentControl) {
        super(-1, 0, 0, new StandardArrayInterface());
        baseElement = new RPGCommandSchemaElement(a, b, db, indentControl, true);
        // gets rid of subwindows & proxies
        database = db;
    }

    @Override
    public boolean monitorsSubelements() {
        return true;
    }

    @Override
    public boolean autoCorrectArray(RubyIO array, SchemaPath path) {
        if (!SDB.allowControlOfEventCommandIndent)
            return false;

        boolean debugInfloop = false;

        if (debugInfloop)
            System.out.println("---");

        boolean modified = false;

        // NOTE: This method is deliberately awkward to allow for the concurrent modification...
        // Attempting to 'fix' it will only make it worse.
        boolean lastWasBlockLeave = false;
        boolean lastWasStrictLeave = false;
        int lastCode = -1;

        // Indent tracking
        int indent = 0;

        // Note that this array can grow as it's being searched.
        boolean hasValidListLeave = database.listLeaveCmd == -1;
        for (int i = 0; i < array.getALen(); i++) {
            RubyIO commandTarg = array.getAElem(i);
            int code = (int) commandTarg.getInstVarBySymbol("@code").fixnumVal;
            RPGCommand rc = database.knownCommands.get(code);
            if (rc != null) {
                // Indent stuff
                final int indentOld = indent;
                indent += rc.indentPre;
                if (baseElement.allowControlOfIndent) {
                    if (indent != commandTarg.getInstVarBySymbol("@indent").fixnumVal) {
                        commandTarg.getInstVarBySymbol("@indent").fixnumVal = indent;
                        modified = true;
                    }
                }
                // Used to understand infinite loops
                if (debugInfloop)
                    System.out.println("i " + i + " " + code + " " + indent);
                indent += rc.indentPost.apply(commandTarg.getInstVarBySymbol("@parameters"));
                // Group Behavior
                for (IGroupBehavior groupBehavior : rc.groupBehaviors)
                    modified |= groupBehavior.correctElement(array, i, commandTarg);

                if (rc.needsBlockLeavePre) {
                    if (!lastWasBlockLeave) {
                        if (rc.blockLeaveReplacement != lastCode) {
                            RubyIO c = array.addAElem(i);
                            SchemaPath.setDefaultValue(c, baseElement, new RubyIO().setFX(i));
                            c.getInstVarBySymbol("@code").fixnumVal = database.blockLeaveCmd;
                            if (baseElement.allowControlOfIndent)
                                c.getInstVarBySymbol("@indent").fixnumVal = indentOld;

                            // About to re-handle the same code.
                            indent = indentOld;
                            lastWasBlockLeave = true;
                            // What to do here depends on a few things. They'll be handled in CMDB.
                            lastCode = database.blockLeaveCmd;
                            modified = true;
                            continue;
                        }
                    }
                } else {
                    if (lastWasBlockLeave && lastWasStrictLeave) {
                        array.rmAElem(i - 1);
                        i--;
                        modified = true;
                    }
                }
                if (rc.typeListLeave) {
                    if (i != array.getALen() - 1) {
                        if (rc.typeStrictLeave) {
                            array.rmAElem(i);
                            i--;
                            modified = true;
                            continue;
                        }
                    } else {
                        hasValidListLeave = true;
                    }
                }
                lastWasBlockLeave = rc.typeBlockLeave;
                lastWasStrictLeave = rc.typeStrictLeave;
            } else {
                lastWasBlockLeave = false;
                lastWasStrictLeave = false;
            }
            lastCode = code;
        }

        // This second pass is used by certain group-behaviors that *really, really* need accurate indent information to not cause damage.
        // Specifically consider this for behaviors which add/remove commands.
        boolean continueToBreak = false;
        for (int i = 0; i < array.getALen(); i++) {
            RubyIO commandTarg = array.getAElem(i);
            int code = (int) commandTarg.getInstVarBySymbol("@code").fixnumVal;
            RPGCommand rc = database.knownCommands.get(code);
            if (rc != null) {
                for (IGroupBehavior groupBehavior : rc.groupBehaviors) {
                    if (groupBehavior.majorCorrectElement(array, i, commandTarg, baseElement)) {
                        // System.err.println(code);
                        modified = true;
                        continueToBreak = true;
                        break;
                    }
                }
            }
            if (continueToBreak)
                break;
        }

        // After it's done with major structural work, add ending block
        if (!continueToBreak) {
            if (!hasValidListLeave) {
                // 0 so that the code won't combust from lacking an array
                int l = array.getALen();
                RubyIO c = array.addAElem(l);
                SchemaPath.setDefaultValue(c, baseElement, new RubyIO().setFX(array.getALen()));
                c.getInstVarBySymbol("@code").fixnumVal = database.listLeaveCmd;
                modified = true;
            }
        }

        return modified;
    }

    private int getGroupLengthCore(RubyIO[] arr, int j) {
        RubyIO commandTarg = arr[j];
        int code = (int) commandTarg.getInstVarBySymbol("@code").fixnumVal;
        RPGCommand rc = database.knownCommands.get(code);
        int max = 0;
        if (rc != null)
            for (IGroupBehavior groupBehavior : rc.groupBehaviors)
                max = Math.max(max, groupBehavior.getGroupLength(arr, j));
        return max;
    }

    // Note that this always returns != 0, so all schemas are in fact array-based.
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
                return TXDB.get("This text should not be visible. Grouping is used for all commands.");
            }
        });
    }

    private SchemaElement getGroupElement(RubyIO[] arr, final int start, final SchemaElement binding) {
        // Uhoh.
        final int length;
        boolean addRemove = false;
        int p = getGroupLengthCore(arr, start);
        if (p == 0) {
            length = 1;
        } else {
            length = p;
            addRemove = true;
        }
        int iSize = addRemove ? 1 : 0;
        SchemaElement[] group = new SchemaElement[length + iSize];
        RPGCommandSchemaElement rcse = baseElement;
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
                    return new UITextButton(addText, FontSizes.schemaFieldTextHeight, new Runnable() {
                        @Override
                        public void run() {
                            RubyIO commandTarg = target.arrVal[start];
                            int code = (int) commandTarg.getInstVarBySymbol("@code").fixnumVal;
                            RPGCommand rc = database.knownCommands.get(code);
                            if (rc != null)
                                for (IGroupBehavior groupBehavior : rc.groupBehaviors) {
                                    RubyIO ne = target.addAElem(start + length);
                                    SchemaPath.setDefaultValue(ne, baseElement, null);
                                    ne.getInstVarBySymbol("@code").fixnumVal = groupBehavior.getAdditionCode();
                                    path.changeOccurred(false);
                                }
                        }
                    });
                }

                @Override
                public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                }
            };
        }
        return new AggregateSchemaElement(group, binding);
    }

    @Override
    protected ElementContextual getElementContextualSchema(RubyIO[] arr, final int start, final int length) {
        // Record the first RubyIO of the group.
        // getGroupElement seeks for it now, so it "tracks" the group properly despite array changes.
        final RubyIO tracker = arr[start];
        int indent = 0;
        final RubyIO trackerIndent = tracker.getInstVarBySymbol("@indent");
        if (trackerIndent != null)
            indent = (int) trackerIndent.fixnumVal;
        return new ElementContextual(indent, getElementContextualSubwindowSchema(tracker, start));
    }

    private SubwindowSchemaElement getElementContextualSubwindowSchema(final RubyIO tracker, final int start) {
        // The reason why the inside changes index but the outside doesn't care,
        //  is because the subwindow gets recreated anytime a change happens, while the inside doesn't.
        return new SubwindowSchemaElement(new SchemaElement() {
            public int actualStart(RubyIO target) {
                for (int i = 0; i < target.arrVal.length; i++)
                    if (target.arrVal[i] == tracker)
                        return i;
                return -1;
            }

            @Override
            public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
                int actualStart = actualStart(target);
                if (actualStart == -1)
                    return new UILabel(TXDB.get("The command isn't in the list anymore, so it has no context."), FontSizes.schemaFieldTextHeight);
                return getGroupElement(target.arrVal, actualStart, this).buildHoldingEditor(target, launcher, path);
            }

            @Override
            public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                int actualStart = actualStart(target);
                if (actualStart == -1)
                    return;
                getGroupElement(target.arrVal, actualStart, this).modifyVal(target, path, setDefault);
            }
        }, new IFunction<RubyIO, String>() {
            @Override
            public String apply(RubyIO rubyIO) {
                String tx = database.buildCodename(rubyIO.arrVal[start], true);
                int groupLen = getGroupLengthCore(rubyIO.arrVal, start);
                for (int i = 1; i < groupLen; i++)
                    tx += "\n" + database.buildCodename(rubyIO.arrVal[start + i], true);
                return tx;
            }
        });
    }

    @Override
    protected void elementOnCreateMagic(final RubyIO target, final int idx, ISchemaHost launcher, SchemaPath ind, SchemaPath path) {
        final SchemaPath sp = path;
        // Notably:
        //  1. the inner-schema always uses the 'path' path.
        //  2. the path constructed must have "back" going to inside the command, then to the array
        //     (so the user knows the command was added anyway)
        SubwindowSchemaElement targ = getElementContextualSubwindowSchema(target.arrVal[idx], idx);
        path = path.arrayHashIndex(new RubyIO().setFX(idx), "[" + idx + "]");
        path = path.newWindow(targ.heldElement, target);
        launcher.pushObject(path);
        // Ok, now navigate to the command selector
        path = path.newWindow(RPGCommandSchemaElement.navigateToCode(launcher, target.arrVal[idx], new IConsumer<int[]>() {
            @Override
            public void accept(int[] i) {
                for (int j = 0; j < i.length; j++) {
                    RubyIO ne = target.addAElem(idx + j + 1);
                    SchemaPath.setDefaultValue(ne, baseElement, null);
                    ne.getInstVarBySymbol("@code").fixnumVal = i[j];
                }
                sp.changeOccurred(false);
            }
        }, path, database), target);
        launcher.pushObject(path);
    }
}
