/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;

/**
 * Note that since this is meant to emulate the RPGCommand system where that is not usable,
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

    public ArrayElementSchemaElement(App app, int ind, String niceName, SchemaElement ise, String opt, boolean dr) {
        super(app);
        index = ind;
        name = niceName;
        subSchema = ise;
        optional = opt;
        delRemove = dr;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        if (name.equals("_"))
            return HiddenSchemaElement.makeHiddenElement();
        if (target.getALen() <= index) {
            String tx = T.s.aElmInv;
            if (optional != null)
                tx = T.s.aElmOpt.r(name, optional);
            return new UITextButton(tx, app.f.schemaFieldTextHeight, new Runnable() {
                @Override
                public void run() {
                    // resize to include and set default
                    resizeToInclude(target);
                    subSchema.modifyVal(target.getAElem(index), path.arrayHashIndex(new IRIOFixnum(index), "." + name), true);
                    path.changeOccurred(false);
                }
            });
        }
        UIElement core = subSchema.buildHoldingEditor(target.getAElem(index), launcher, path.arrayHashIndex(new IRIOFixnum(index), "." + name));

        if (!name.equals("")) {
            UILabel label = new UILabel(name, app.f.schemaFieldTextHeight);
            core = new UIFieldLayout(label, core, fieldWidth, fieldWidthOverride);
            fieldWidthOverride = false;
        }

        if (optional != null)
            return new UIAppendButton("-", core, new Runnable() {
                @Override
                public void run() {
                    if (delRemove) {
                        target.rmAElem(index);
                    } else {
                        while (target.getALen() > index)
                            target.rmAElem(index);
                    }
                    path.changeOccurred(false);
                }
            }, app.f.schemaFieldTextHeight);

        return core;
    }

    @Override
    public int getDefaultFieldWidth(IRIO target) {
        return UILabel.getRecommendedTextSize(name + " ", app.f.schemaFieldTextHeight).width;
    }

    @Override
    public void setFieldWidthOverride(int w) {
        fieldWidth = w;
        fieldWidthOverride = true;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        boolean changed = false;
        // Just in case.
        // Turns out there was a reason, if not a good one, for array encapsulation - it ensured the object was actually an array.
        // Oops. Well, this resolves it.
        if (SchemaElement.checkType(target, '[', null, false)) {
            target.setArray();
            changed = true;
        }
        // Resize array if required?
        if (optional == null)
            changed |= resizeToInclude(target);
        if (target.getALen() > index)
            subSchema.modifyVal(target.getAElem(index), path.arrayHashIndex(new IRIOFixnum(index), "." + name), setDefault);
        if (changed)
            path.changeOccurred(true);
    }

    private boolean resizeToInclude(IRIO target) {
        boolean changed = false;
        int alen;
        while ((alen = target.getALen()) < (index + 1)) {
            target.addAElem(alen);
            changed = true;
        }
        return changed;
    }
}
