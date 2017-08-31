/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UITextBox;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/29/16.
 */
public class StringSchemaElement extends SchemaElement {
    public String defaultStr = "";
    public final char type;

    public StringSchemaElement(String arg, char t) {
        defaultStr = arg;
        type = t;
    }

    // Note the type must be UITextBox - This is so StringLenSchemaElement can latch on.
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITextBox tb = new UITextBox(FontSizes.schemaFieldTextHeight);
        tb.text = decodeVal(target);
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


    protected void encodeVal(String text, RubyIO target) {
        target.encString(text);
    }

    protected boolean verifier(String text) {
        return true;
    }

    protected String decodeVal(RubyIO target) {
        return target.decString();
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, type, setDefault)) {
            encodeVal(defaultStr, target);
            path.changeOccurred(true);
        } else if (target.strVal == null) {
            encodeVal(defaultStr, target);
            path.changeOccurred(true);
        }
    }
}
