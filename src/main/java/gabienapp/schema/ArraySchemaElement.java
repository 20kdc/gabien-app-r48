/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema;

import gabien.ui.*;
import gabienapp.schema.util.ISchemaHost;
import gabienapp.RubyIO;
import gabienapp.schema.util.SchemaPath;
import gabienapp.ui.UIAppendButton;
import gabienapp.ui.UIScrollVertLayout;

/**
 * Created on 12/28/16.
 */
public class ArraySchemaElement implements ISchemaElement {
    public ISchemaElement subelems;
    public int sizeFixed;
    public boolean atLeastOne;
    public ArraySchemaElement(ISchemaElement elems, int fixedSize, boolean al1) {
        subelems = elems;
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
                    SchemaPath ind = path.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]");
                    addAdditionButton(i, ind);
                    UIElement uie = subelems.buildHoldingEditor(target.arrVal[i], launcher, ind);
                    final int mi = i;
                    if ((sizeFixed == 0) && (!(atLeastOne && (target.arrVal.length <= 1)))) {
                        uie = new UIAppendButton("-", uie, new Runnable() {
                            @Override
                            public void run() {
                                RubyIO[] old = target.arrVal;
                                RubyIO[] newArr = new RubyIO[old.length - 1];
                                for (int j = 0; j < mi; j++)
                                    newArr[j] = old[j];
                                for (int j = mi + 1; j < old.length; j++)
                                    newArr[j - 1] = old[j];
                                newArr = correctShift(newArr, mi, old.length - mi, -1);
                                target.arrVal = newArr;
                                // fixup array indices!
                                modifyVal(target, path, false);
                                // whack the UI
                                path.changeOccurred(false);
                                me.run();
                            }
                        }, false);
                    }
                    int sz = subelems.maxHoldingHeight();
                    uie.setBounds(new Rect(0, 0, 128, sz));
                    uiSVL.panels.add(uie);
                }
                addAdditionButton(target.arrVal.length, path.arrayHashIndex(new RubyIO().setFX(target.arrVal.length), "[" + target.arrVal.length + "]"));
            }

            private void addAdditionButton(final int i, final SchemaPath ind) {
                if (sizeFixed != 0)
                    return;
                final Runnable me = this;
                uiSVL.panels.add(new UITextButton(false, "Add @ " + i, new Runnable() {
                    @Override
                    public void run() {
                        RubyIO[] old = target.arrVal;
                        RubyIO[] newArr = new RubyIO[old.length + 1];
                        for (int j = 0; j < i; j++)
                            newArr[j] = old[j];
                        RubyIO rio = new RubyIO();
                        subelems.modifyVal(rio, ind, true);
                        newArr[i] = rio;
                        for (int j = i; j < old.length; j++)
                            newArr[j + 1] = old[j];
                        newArr = correctShift(newArr, i, old.length - i, 1);
                        target.arrVal = newArr;
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
    public int maxHoldingHeight() {
        if (sizeFixed != 0)
            return subelems.maxHoldingHeight() * sizeFixed;
        // *gulp* guess, and hope the guess is correct.
        return subelems.maxHoldingHeight() * 16;
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
                subelems.modifyVal(rio, path.arrayHashIndex(new RubyIO().setFX(j), "[" + j + "]"), tempSetDefault);
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

    // Function to be overridden by subclasses to shift references
    //  in some parts of an array to account for changes in others.
    // (Think RPG Script jumps)
    // start and len are in the pre-shift array's indices, because those are the references you're looking for.
    // The condition is if ((ref >= start) && ((ref < start + len)).
    // Now, the reason this thing returns a value is because a specific subclass might need to
    //  treat "delete the last line" as "don't actually delete the last line, but instead reset it" or some other logic.
    // You never know!
    public RubyIO[] correctShift(RubyIO[] newArr, int start, int len, int shift) {
        return newArr;
    }

    // Allows performing automatic correction of structural issues,
    //  after any data issues have been cleaned up.
    // path is already in the array entry.
    // Note that this is solely for auto-correction.
    // If setDefault is required, just override modifyVal
    //  and don't bother to call super if setDefault is true,
    //  instead doing whatever you need yourself.
    // Also note that if a modification is performed, another check is done so that things like indent processing can run.
    public boolean autoCorrectArray(RubyIO array, SchemaPath path) {
        return false;
    }
}
