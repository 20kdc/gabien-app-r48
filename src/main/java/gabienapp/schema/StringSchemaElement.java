/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UITextBox;
import gabienapp.schema.util.ISchemaHost;
import gabienapp.RubyIO;
import gabienapp.schema.util.SchemaPath;

import java.io.UnsupportedEncodingException;

/**
 * Created on 12/29/16.
 */
public class StringSchemaElement implements ISchemaElement {
    public String defaultStr = "";

    public StringSchemaElement(String arg) {
        defaultStr = arg;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITextBox tb = new UITextBox(false);
        tb.text = target.decString();
        tb.onEdit = new Runnable() {
            @Override
            public void run() {
                target.encString(tb.text);
                path.changeOccurred(false);
            }
        };
        tb.setBounds(new Rect(0, 0, 9, 9));
        return tb;
    }

    @Override
    public int maxHoldingHeight() {
        return 9;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, '\"', setDefault)) {
            try {
                target.strVal = defaultStr.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                target.strVal = new byte[0];
                e.printStackTrace();
            }
            path.changeOccurred(true);
        } else if (target.strVal == null) {
            try {
                target.strVal = defaultStr.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                target.strVal = new byte[0];
                e.printStackTrace();
            }
            path.changeOccurred(true);
        }
    }
}
