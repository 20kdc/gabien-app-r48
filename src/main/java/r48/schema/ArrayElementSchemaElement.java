/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UIHHalfsplit;

/**
 * NOTE: This doesn't provide the array entry object!!!
 * This is because ArrayElementSchemaElement should only exist inside arrayDAM.
 * (Well THAT didn't end up happening. fixing R2k schemascripting...)
 * Also note that since this is meant to emulate the RPGCommand system where that is not usable,
 *  among other things, '_' as a name will act to make a given parameter invisible.
 * Created on 12/31/16.
 */
public class ArrayElementSchemaElement extends SchemaElement {
    public int index;
    public String name;
    public SchemaElement subSchema;
    public String optional;

    public ArrayElementSchemaElement(int ind, String niceName, SchemaElement ise, String opt) {
        index = ind;
        name = niceName;
        subSchema = ise;
        optional = opt;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        if (name.equals("_")) {
            UIPanel panel = new UIPanel();
            panel.setBounds(new Rect(0, 0, 0, 0));
            return panel;
        }
        if ((target.arrVal.length <= index) && (optional != null)) {
            return new UITextButton(FontSizes.schemaButtonTextHeight, "Field " + name + " doesn't exist (default " + optional + ")", new Runnable() {
                @Override
                public void run() {
                    // resize to include and set default
                    RubyIO[] newArr = new RubyIO[index + 1];
                    for (int i = 0; i < target.arrVal.length; i++)
                        newArr[i] = target.arrVal[i];
                    for (int i = target.arrVal.length; i < newArr.length; i++)
                        newArr[i] = new RubyIO().setNull();
                    subSchema.modifyVal(newArr[index], path.arrayHashIndex(new RubyIO().setFX(index), "." + name), true);
                    target.arrVal = newArr;
                    path.changeOccurred(false);
                }
            });
        }
        UIElement core = new UIHHalfsplit(1, 3, new UILabel(name, FontSizes.schemaFieldTextHeight), subSchema.buildHoldingEditor(target.arrVal[index], launcher, path.arrayHashIndex(new RubyIO().setFX(index), "." + name)));;
        if (optional != null)
            return new UIAppendButton("-", core, new Runnable() {
                @Override
                public void run() {
                    // Cut array and call modification alerter.
                    RubyIO[] newArr = new RubyIO[index];
                    for (int i = 0; i < newArr.length; i++)
                        newArr[i] = target.arrVal[i];
                    target.arrVal = newArr;
                    path.changeOccurred(false);
                }
            }, FontSizes.schemaFieldTextHeight);
        return core;
    }

    @Override
    public int maxHoldingHeight() {
        if (name.equals("_"))
            return 0;
        return Math.max(UILabel.getRecommendedSize("", FontSizes.schemaFieldTextHeight).height, subSchema.maxHoldingHeight());
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        boolean changed = false;
        // Just in case.
        // Turns out there was a reason, if not a good one, for array encapsulation - it ensured the object was actually an array.
        // Oops. Well, this resolves it.
        if (IntegerSchemaElement.ensureType(target, '[', false)) {
            target.arrVal = new RubyIO[index + 1];
            for (int i = 0; i < target.arrVal.length; i++)
                target.arrVal[i] = new RubyIO().setNull();
            changed = true;
        }
        // Resize array if required?
        if (target.arrVal.length <= index) {
            if (optional == null) {
                RubyIO[] newArr = new RubyIO[index + 1];
                for (int i = 0; i < target.arrVal.length; i++)
                    newArr[i] = target.arrVal[i];
                for (int i = target.arrVal.length; i < newArr.length; i++)
                    newArr[i] = new RubyIO().setNull();
                target.arrVal = newArr;
                changed = true;
            } else {
                if (changed)
                    path.changeOccurred(true);
                return;
            }
        }
        subSchema.modifyVal(target.arrVal[index], path.arrayHashIndex(new RubyIO().setFX(index), "." + name), setDefault);
        if (changed)
            path.changeOccurred(true);
    }
}
