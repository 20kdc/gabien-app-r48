/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.arrays;

import gabien.ui.*;
import r48.ArrayUtils;
import r48.FontSizes;
import r48.schema.ISchemaElement;
import r48.schema.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.RubyIO;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UIScrollVertLayout;

/**
 * Notably, abstracting away sizeFixed and atLeastOne would just be an overcomplication.
 * Created on 12/28/16. Abstractified 16 Feb 2017.
 */
public abstract class ArraySchemaElement implements ISchemaElement {
    public int sizeFixed;
    public boolean atLeastOne;
    public ArraySchemaElement(int fixedSize, boolean al1) {
        sizeFixed = fixedSize;
        atLeastOne = al1;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path2) {
        final SchemaPath path = path2.arrayEntry(target, this);
        final UIScrollVertLayout uiSVL = new UIScrollVertLayout() {
            @Override
            public String toString() {
                return "SCHEMA Array";
            }
        };
        // this object is needed as a pin to hold things together
        final Runnable runCompleteRelayout = new Runnable() {
            @Override
            public void run() {
                uiSVL.panels.clear();
                final Runnable me = this;
                for (int i = 0; i < target.arrVal.length; i++) {
                    int pLevel = elementPermissionsLevel(i, target);
                    if (pLevel < 1)
                        continue;
                    SchemaPath ind = path.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]");
                    addAdditionButton(i, ind);
                    ISchemaElement subelem = getElementSchema(i);
                    UIElement uie = subelem.buildHoldingEditor(target.arrVal[i], launcher, ind);
                    final int mi = i;
                    if (pLevel >= 2) {
                        uie = new UIAppendButton("-", uie, new Runnable() {
                            @Override
                            public void run() {
                                ArrayUtils.removeRioElement(target, mi);
                                // fixup array indices!
                                modifyVal(target, path, false);
                                // whack the UI
                                path.changeOccurred(false);
                                me.run();
                            }
                        }, FontSizes.schemaButtonTextHeight);
                    }
                    int sz = subelem.maxHoldingHeight();
                    uie.setBounds(new Rect(0, 0, 128, sz));
                    uiSVL.panels.add(uie);
                }
                addAdditionButton(target.arrVal.length, path.arrayHashIndex(new RubyIO().setFX(target.arrVal.length), "[" + target.arrVal.length + "]"));
            }

            private void addAdditionButton(final int i, final SchemaPath ind) {
                if (sizeFixed != 0)
                    return;
                final Runnable me = this;
                uiSVL.panels.add(new UITextButton(FontSizes.schemaArrayAddTextHeight, "Add @ " + i, new Runnable() {
                    @Override
                    public void run() {
                        RubyIO rio = new RubyIO();
                        ISchemaElement subelem = getElementSchema(i);
                        subelem.modifyVal(rio, ind, true);

                        ArrayUtils.insertRioElement(target, rio, i);
                        // fixup array indices!
                        modifyVal(target, path, false);
                        // whack the UI
                        path.changeOccurred(false);
                        me.run();
                    }
                }));
            }
        };
        runCompleteRelayout.run();
        return uiSVL;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        path = path.arrayEntry(target, this);
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
        }
        boolean modified = setDefault;
        while (true) {
            for (int j = 0; j < target.arrVal.length; j++) {
                RubyIO rio = target.arrVal[j];
                boolean tempSetDefault = setDefault || (rio == null);
                if (tempSetDefault) {
                    rio = new RubyIO();
                    modified = true;
                }
                // Fun fact: There's a reason for this not-quite-linear timeline.
                // It's because otherwise, when the subelement tries to notify the array of the modification,
                //  it will lead to an infinite loop!
                // So it has to be able to see it's own object for the loop to terminate.
                target.arrVal[j] = rio;
                getElementSchema(j).modifyVal(rio, path.arrayHashIndex(new RubyIO().setFX(j), "[" + j + "]"), tempSetDefault);
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

    // Allows performing automatic correction of structural issues,
    //  after any data issues have been cleaned up.
    // path is already in the array entry.
    // Note that this is solely for auto-correction.
    // If setDefault is required, just override modifyVal
    //  and don't bother to call super if setDefault is true,
    //  instead doing whatever you need yourself.
    // Also note that if a modification is performed, another check is done so that things like indent processing can run.
    protected abstract boolean autoCorrectArray(RubyIO array, SchemaPath path);

    // Allows using a custom schema for specific elements in subclasses.
    protected abstract ISchemaElement getElementSchema(int j);

    // 0: Do not even show this element.
    // 1: Show & allow editing of this element, but disallow deletion.
    // 2: All permissions.
    // (Used to prevent a user shooting themselves in the foot - should not be considered a serious mechanism.)
    protected int elementPermissionsLevel(int i, RubyIO target) {
        boolean canDelete = (sizeFixed == 0) && (!(atLeastOne && target.arrVal.length <= 1));
        return canDelete ? 2 : 1;
    }
}
