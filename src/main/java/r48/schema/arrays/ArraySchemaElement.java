/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.arrays;

import gabien.ui.*;
import r48.AppMain;
import r48.ArrayUtils;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

/**
 * Notably, abstracting away sizeFixed and atLeastOne would just be an overcomplication.
 * Created on 12/28/16. Abstractified 16 Feb 2017.
 */
public abstract class ArraySchemaElement extends SchemaElement {
    public int sizeFixed;
    public boolean atLeastOne;

    public ArraySchemaElement(int fixedSize, boolean al1) {
        sizeFixed = fixedSize;
        atLeastOne = al1;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path2) {
        final SchemaPath path = monitorsSubelements() ? path2.tagSEMonitor(target, this) : path2;
        final UIScrollLayout uiSVL = new UIScrollLayout(true);
        // this object is needed as a pin to hold things together.
        // It used to be kind of redundant, but now with the selection stuff...
        final Runnable runCompleteRelayout = new Runnable() {
            // Only check selectedStart.
            int selectedStart = -1;
            int selectedEnd = -1;
            // Because of name ambiguity, but also whacks uiSVL
            public void containerRCL() {
                run();
                uiSVL.setBounds(uiSVL.getBounds());
            }
            @Override
            public void run() {
                uiSVL.panels.clear();
                // Work out how big each array index field has to be.
                final Rect maxSize = UILabel.getRecommendedSize(target.arrVal.length + " ", FontSizes.schemaFieldTextHeight);
                int nextAdvance;
                for (int i = 0; i < target.arrVal.length; i += nextAdvance) {
                    nextAdvance = 1;
                    int pLevel = elementPermissionsLevel(i, target);
                    if (pLevel < 1)
                        continue;
                    SchemaPath ind = path.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]");
                    addAdditionButton(i, ind);
                    SchemaElement subelem = getElementSchema(i);
                    nextAdvance = getGroupLength(target.arrVal, i);
                    boolean hasNIdxSchema = false;
                    if (nextAdvance == 0) {
                        nextAdvance = 1;
                    } else {
                        subelem = getElementContextualSchema(target.arrVal, i, nextAdvance);
                        hasNIdxSchema = true;
                    }
                    final int thisNextAdvance = nextAdvance;

                    UIElement uie;
                    if (hasNIdxSchema) {
                        uie = subelem.buildHoldingEditor(target, launcher, ind);
                    } else {
                        uie = subelem.buildHoldingEditor(target.arrVal[i], launcher, ind);
                    }
                    final int mi = i;
                    if (selectedStart == -1) {
                        uie = new UIAppendButton(TXDB.get("Sel"), uie, new Runnable() {
                            @Override
                            public void run() {
                                selectedStart = mi;
                                selectedEnd = mi + (thisNextAdvance - 1);
                                containerRCL();
                            }
                        }, FontSizes.schemaButtonTextHeight);
                        if (pLevel >= 2) {
                            uie = new UIAppendButton("-", uie, new Runnable() {
                                @Override
                                public void run() {
                                    for (int j = 0; j < thisNextAdvance; j++)
                                        ArrayUtils.removeRioElement(target, mi);
                                    // whack the UI & such
                                    path.changeOccurred(false);
                                }
                            }, FontSizes.schemaButtonTextHeight);
                        }
                    } else {
                        if (selectedStart == i) {
                            uie = new UIAppendButton(TXDB.get("DeSel"), uie, new Runnable() {
                                @Override
                                public void run() {
                                    selectedStart = -1;
                                    containerRCL();
                                }
                            }, FontSizes.schemaButtonTextHeight);
                            uie = new UIAppendButton("Cp.", uie, new Runnable() {
                                @Override
                                public void run() {
                                    // the clipboard is very lenient...
                                    RubyIO rio = new RubyIO();
                                    rio.type = '[';
                                    rio.arrVal = new RubyIO[(selectedEnd - selectedStart) + 1];
                                    for (int j = 0; j < rio.arrVal.length; j++)
                                        rio.arrVal[j] = new RubyIO().setDeepClone(target.arrVal[j + selectedStart]);
                                    AppMain.theClipboard = rio;
                                    selectedStart = -1;
                                    containerRCL();
                                }
                            }, FontSizes.schemaButtonTextHeight);
                        } else if ((mi < selectedStart) || (mi > selectedEnd)){
                            uie = new UIAppendButton(TXDB.get("Select..."), uie, new Runnable() {
                                @Override
                                public void run() {
                                    if (mi < selectedStart)
                                        selectedStart = mi;
                                    int re = mi + (thisNextAdvance - 1);
                                    if (re > selectedEnd)
                                        selectedEnd = re;
                                    containerRCL();
                                }
                            }, FontSizes.schemaButtonTextHeight);
                        }
                    }
                    int sz = subelem.maxHoldingHeight();
                    uie.setBounds(new Rect(0, 0, 128, sz));
                    // Add indexes for clarity.
                    final UIElement editor = uie;
                    final UIElement label = new UILabel(i + " ", FontSizes.schemaFieldTextHeight);
                    UIPanel panel = new UIPanel() {
                        @Override
                        public void setBounds(Rect r) {
                            super.setBounds(r);
                            label.setBounds(new Rect(0, 0, maxSize.width, maxSize.height));
                            editor.setBounds(new Rect(maxSize.width, 0, r.width - maxSize.width, r.height));
                        }
                    };
                    panel.allElements.add(label);
                    panel.allElements.add(editor);
                    panel.setBounds(new Rect(0, 0, 128, Math.max(sz, maxSize.height)));
                    uiSVL.panels.add(panel);
                }
                // Deal with 1-indexing and such
                for (int i = 0; i < 4; i++) {
                    if (elementPermissionsLevel(target.arrVal.length + i, target) != 0) {
                        addAdditionButton(target.arrVal.length + i, path.arrayHashIndex(new RubyIO().setFX(target.arrVal.length + i), "[" + (target.arrVal.length + i) + "]"));
                        break;
                    }
                }
            }

            private void addAdditionButton(final int i, final SchemaPath ind) {
                if (sizeFixed != 0)
                    return;
                UIElement uie = new UITextButton(FontSizes.schemaArrayAddTextHeight, FormatSyntax.formatExtended(TXDB.get("Add #@ #A"), new RubyIO().setFX(i)), new Runnable() {
                    @Override
                    public void run() {
                        RubyIO rio = new RubyIO();
                        SchemaElement subelem = getElementSchema(i);
                        subelem.modifyVal(rio, ind, true);

                        ArrayUtils.insertRioElement(target, rio, i);
                        // whack the UI
                        path.changeOccurred(false);
                    }
                });
                if (AppMain.theClipboard != null) {
                    if (AppMain.theClipboard.type == '[') {
                        uie = new UIAppendButton("Ps.", uie, new Runnable() {
                            @Override
                            public void run() {
                                if (AppMain.theClipboard != null) {
                                    // could have changed
                                    if (AppMain.theClipboard.type == '[') {
                                        RubyIO[] finalInsertionRv = AppMain.theClipboard.arrVal;
                                        for (int j = finalInsertionRv.length - 1; j >= 0; j--)
                                            ArrayUtils.insertRioElement(target, finalInsertionRv[j], i);
                                        // whack the UI
                                        path.changeOccurred(false);
                                    }
                                }
                            }
                        }, FontSizes.schemaButtonTextHeight);
                    }
                }
                uiSVL.panels.add(uie);
            }
        };
        runCompleteRelayout.run();
        uiSVL.setBounds(new Rect(0, 0, 32, maxHoldingHeight()));
        return uiSVL;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path2, boolean setDefault) {
        final SchemaPath path = monitorsSubelements() ? path2.tagSEMonitor(target, this) : path2;
        setDefault = IntegerSchemaElement.ensureType(target, '[', setDefault);
        if (target.arrVal == null) {
            setDefault = true;
        } else if ((target.arrVal.length == 0) && atLeastOne) {
            setDefault = true;
        }
        if (setDefault) {
            if ((sizeFixed == 0) && atLeastOne) {
                target.arrVal = new RubyIO[1];
            } else {
                target.arrVal = new RubyIO[sizeFixed];
            }
            for (int i = 0; i < target.arrVal.length; i++)
                target.arrVal[i] = new RubyIO();
        }
        boolean modified = setDefault;
        while (true) {
            for (int j = 0; j < target.arrVal.length; j++) {
                RubyIO rio = target.arrVal[j];
                // Fun fact: There's a reason for this not-quite-linear timeline.
                // It's because otherwise, when the subelement tries to notify the array of the modification,
                //  it will lead to an infinite loop!
                // So it has to be able to see it's own object for the loop to terminate.
                target.arrVal[j] = rio;
                getElementSchema(j).modifyVal(rio, path.arrayHashIndex(new RubyIO().setFX(j), "[" + j + "]"), setDefault);
            }
            int groupStep;
            for (int j = 0; j < target.arrVal.length; j += groupStep) {
                groupStep = getGroupLength(target.arrVal, j);
                if (groupStep == 0) {
                    groupStep = 1;
                    continue;
                }
                getElementContextualSchema(target.arrVal, j, groupStep).modifyVal(target, path, setDefault);
            }
            boolean aca = autoCorrectArray(target, path);
            modified = modified || aca;
            if (!aca)
                break;
            setDefault = false;
        }
        if (modified)
            path.changeOccurred(true);
    }

    // Used to do the correct tagging so that updates to children will affect the parent
    public boolean monitorsSubelements() {
        return false;
    }

    // Allows performing automatic correction of structural issues,
    //  after any data issues have been cleaned up.
    // path is already in the array entry.
    // Note that this is solely for auto-correction.
    // If setDefault is required, just override modifyVal
    //  and don't bother to call super if setDefault is true,
    //  instead doing whatever you need yourself.
    // Also note that if a modification is performed, another check is done so that things like indent processing can run.
    protected abstract boolean autoCorrectArray(RubyIO array, SchemaPath path);

    // Allows using a custom schema for specific elements in specific contexts in subclasses.
    // Note that this is meant to be used by things messing with getGroupLength, and will not be used otherwise.
    // Also note that for modifyVal purposes this acts *in addition* to getElementSchema,
    //  so that getGroupLength can safely assume that getElementSchema is being followed.
    protected SchemaElement getElementContextualSchema(RubyIO[] arr, int start, int length) {
        throw new RuntimeException("Group length was used, but no contextual schema was defined for it.");
    }
    protected abstract SchemaElement getElementSchema(int j);

    // Used to replace groups of elements with a single editor, where this makes sense.
    // If this is non-zero for a given element, then the element schema is assumed to apply to the array.
    // Use with care.
    protected int getGroupLength(RubyIO[] array, int j) {
        return 0;
    }

    // 0: Do not even show this element.
    // 1: Show & allow editing of this element, but disallow deletion.
    // 2: All permissions.
    // (Used to prevent a user shooting themselves in the foot - should not be considered a serious mechanism.)
    protected int elementPermissionsLevel(int i, RubyIO target) {
        boolean canDelete = (sizeFixed == 0) && (!(atLeastOne && target.arrVal.length <= 1));
        return canDelete ? 2 : 1;
    }
}
