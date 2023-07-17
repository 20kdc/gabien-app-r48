/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UITextBox;
import r48.App;
import r48.io.IntUtils;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Basically a copy of StringSchemaElement with some modifications
 * ~~Created on 12/29/16.~~ cloned on 15 feb.2017
 */
public class FloatSchemaElement extends SchemaElement.Leaf {
    public boolean jsonCoerce;
    public String def;

    public FloatSchemaElement(App app, String arg, boolean json) {
        super(app);
        def = arg;
        jsonCoerce = json;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        final String oldValue;
        if (target.getType() == 'f') {
            oldValue = IntUtils.decodeRbFloat(target.getBuffer());
        } else if (jsonCoerce) {
            oldValue = Long.toString(target.getFX());
        } else {
            throw new RuntimeException("No JSONCoerce but got a " + target.getType() + " instead of a float.");
        }
        final UITextBox utb = new UITextBox(oldValue, app.f.schemaFieldTH);
        utb.onEdit = new Runnable() {
            @Override
            public void run() {
                if (IntUtils.encodeRbFloat(target, utb.text, jsonCoerce)) {
                    path.changeOccurred(false);
                } else {
                    utb.text = oldValue;
                }
            }
        };
        return utb;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        boolean ok = false;
        int typ = target.getType();
        if (jsonCoerce)
            if (typ == 'i')
                ok = true;
        if (typ == 'f')
            ok = true;
        if (setDefault)
            ok = false;
        if (!ok) {
            if (!IntUtils.encodeRbFloat(target, def, jsonCoerce))
                throw new RuntimeException("Float default must be valid");
            path.changeOccurred(true);
        }
    }
}
