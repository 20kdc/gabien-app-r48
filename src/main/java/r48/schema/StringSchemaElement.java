/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UITextBox;
import r48.FontSizes;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/29/16.
 */
public class StringSchemaElement extends SchemaElement {
    public final String defaultStr;
    public final char type;

    public StringSchemaElement(String arg, char t) {
        defaultStr = arg;
        type = t;
    }

    // Note the type must be UITextBox - This is so StringLenSchemaElement can latch on.
    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITextBox tb = new UITextBox(decodeVal(target), FontSizes.schemaFieldTextHeight);
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
            encodeVal(defaultStr, target);
            path.changeOccurred(true);
        }
    }
}
