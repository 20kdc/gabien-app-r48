/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextBox;
import r48.App;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;

/**
 * Created on 12/29/16.
 */
public class StringSchemaElement extends SchemaElement.Leaf {
    public final FF0 defaultStr;
    public final char type;

    public StringSchemaElement(App app, FF0 arg, char t) {
        super(app);
        defaultStr = arg;
        type = t;
    }

    // Note the type must be UITextBox - This is so StringLenSchemaElement can latch on.
    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITextBox tb = new UITextBox(decodeVal(target), app.f.schemaFieldTH);
        tb.onEdit = new Runnable() {
            @Override
            public void run() {
                if (verifier(tb.text)) {
                    encodeVal(tb.text, target);
                    path.changeOccurred(false);
                } else {
                    tb.text = decodeVal(target);
                }
            }
        };
        return tb;
    }


    protected void encodeVal(String text, IRIO target) {
        target.setString(text);
    }

    protected boolean verifier(String text) {
        return true;
    }

    protected String decodeVal(IRIO target) {
        return target.decString();
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (SchemaElement.checkType(target, type, null, setDefault)) {
            encodeVal(defaultStr.r(), target);
            path.changeOccurred(true);
        }
    }
}
