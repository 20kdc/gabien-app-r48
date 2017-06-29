/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.*;
import r48.ArrayUtils;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

/**
 * NOTE: This doesn't provide the array entry object!!!
 * This is because ArrayElementSchemaElement should only exist inside arrayDAM.
 * (Well THAT didn't end up happening. fixing R2k schemascripting...)
 * Also note that since this is meant to emulate the RPGCommand system where that is not usable,
 * among other things, '_' as a name will act to make a given parameter invisible.
 * Created on 12/31/16.
 */
public class ArrayElementSchemaElement extends SchemaElement implements IFieldSchemaElement {
    public int index;
    public String name;
    public SchemaElement subSchema;
    public String optional;
    // Removes the element rather than cutting the array. Only use when it is safe to do so.
    public boolean delRemove;

    private boolean fieldWidthOverride = false;
    private int fieldWidth;

    public ArrayElementSchemaElement(int ind, String niceName, SchemaElement ise, String opt, boolean dr) {
        index = ind;
        name = niceName;
        subSchema = ise;
        optional = opt;
        delRemove = dr;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        if (name.equals("_")) {
            UIPanel panel = new UIPanel();
            panel.setBounds(new Rect(0, 0, 0, 0));
            return panel;
        }
        if (target.arrVal.length <= index) {
            String tx = TXDB.get("(This index isn't valid - did you modify a group from another window?)");
            if (optional != null)
                tx = FormatSyntax.formatExtended(TXDB.get("Field #A doesn't exist (default #B)"), new RubyIO[] {new RubyIO().setString(name), new RubyIO().setString(optional)});
            return new UITextButton(FontSizes.schemaButtonTextHeight, tx, new Runnable() {
                @Override
                public void run() {
                    // resize to include and set default
                    RubyIO[] newArr = new RubyIO[index + 1];
                    System.arraycopy(target.arrVal, 0, newArr, 0, target.arrVal.length);
                    for (int i = target.arrVal.length; i < newArr.length; i++)
                        newArr[i] = new RubyIO().setNull();
                    subSchema.modifyVal(newArr[index], path.arrayHashIndex(new RubyIO().setFX(index), "." + name), true);
                    target.arrVal = newArr;
                    path.changeOccurred(false);
                }
            });
        }
        UIElement core = subSchema.buildHoldingEditor(target.arrVal[index], launcher, path.arrayHashIndex(new RubyIO().setFX(index), "." + name));

        if (optional != null)
            return new UIAppendButton("-", core, new Runnable() {
                @Override
                public void run() {
                    if (delRemove) {
                        ArrayUtils.removeRioElement(target, index);
                    } else {
                        // Cut array and call modification alerter.
                        RubyIO[] newArr = new RubyIO[index];
                        System.arraycopy(target.arrVal, 0, newArr, 0, newArr.length);
                        target.arrVal = newArr;
                    }
                    path.changeOccurred(false);
                }
            }, FontSizes.schemaFieldTextHeight);

        if (!name.equals("")) {
            UILabel label = new UILabel(name, FontSizes.schemaFieldTextHeight);
            if (fieldWidthOverride) {
                label.setBounds(new Rect(0, 0, fieldWidth, label.getBounds().height));
                fieldWidthOverride = false;
            }
            core = new UISplitterLayout(label, core, false, 0);
        }

        return core;
    }

    @Override
    public int getDefaultFieldWidth() {
        return UILabel.getRecommendedSize(name + " ", FontSizes.schemaFieldTextHeight).width;
    }

    @Override
    public void setFieldWidthOverride(int w) {
        fieldWidth = w;
        fieldWidthOverride = true;
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
                System.arraycopy(target.arrVal, 0, newArr, 0, target.arrVal.length);
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
