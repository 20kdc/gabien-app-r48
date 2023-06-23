/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.Nullable;

import gabien.GaBIEn;
import gabien.ui.UIBorderedElement;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
import r48.App;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;

/**
 * Note that since this is meant to emulate the RPGCommand system where that is not usable,
 * among other things, '_' as a name will act to make a given parameter invisible.
 * Created on 12/31/16.
 */
public class ArrayElementSchemaElement extends SchemaElement implements IFieldSchemaElement {
    public int index;
    public @Nullable FF0 nameCb;
    public SchemaElement subSchema;
    public @Nullable FF0 optional;
    // Removes the element rather than cutting the array. Only use when it is safe to do so.
    public boolean delRemove;

    private boolean fieldWidthOverride = false;
    private int fieldWidth;

    public ArrayElementSchemaElement(App app, int ind, @Nullable FF0 niceName, SchemaElement ise, @Nullable FF0 opt, boolean dr) {
        super(app);
        index = ind;
        nameCb = niceName;
        subSchema = ise;
        optional = opt;
        delRemove = dr;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        if (nameCb == null)
            return HiddenSchemaElement.makeHiddenElement();
        final String name = nameCb.r();
        if (target.getALen() <= index) {
            String tx = T.s.aElmInv;
            if (optional != null)
                tx = T.s.aElmOpt.r(name, optional.r());
            return new UITextButton(tx, app.f.schemaFieldTH, () -> {
                // resize to include and set default
                resizeToInclude(target);
                subSchema.modifyVal(target.getAElem(index), path.arrayHashIndex(DMKey.of(index), "." + name), true);
                path.changeOccurred(false);
            });
        }
        UIElement core = subSchema.buildHoldingEditor(target.getAElem(index), launcher, path.arrayHashIndex(DMKey.of(index), "." + name));

        if (!name.equals("")) {
            UILabel label = new UILabel(name, app.f.schemaFieldTH);
            core = new UIFieldLayout(label, core, fieldWidth, fieldWidthOverride);
            fieldWidthOverride = false;
        }

        if (optional != null)
            return new UIAppendButton("-", core, () -> {
                if (delRemove) {
                    target.rmAElem(index);
                } else {
                    while (target.getALen() > index)
                        target.rmAElem(index);
                }
                path.changeOccurred(false);
            }, app.f.schemaFieldTH);

        return core;
    }

    @Override
    public int getDefaultFieldWidth(IRIO target) {
        if (nameCb == null)
            return 0;
        return UIBorderedElement.getRecommendedTextSize(GaBIEn.sysThemeRoot.getTheme(), nameCb.r() + " ", app.f.schemaFieldTH).width;
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
        if (target.getALen() > index) {
            String indexStr = nameCb != null ? "." + nameCb.r() : ("]" + index);
            subSchema.modifyVal(target.getAElem(index), path.arrayHashIndex(DMKey.of(index), indexStr), setDefault);
        }
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
