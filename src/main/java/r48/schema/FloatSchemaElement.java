/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UITextBox;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.UnsupportedEncodingException;

/**
 * Basically a copy of StringSchemaElement with some modifications
 * ~~Created on 12/29/16.~~ cloned on 15 feb.2017
 */
public class FloatSchemaElement extends StringSchemaElement {
    public FloatSchemaElement(String arg) {
        super(arg, 'f');
    }

    @Override
    public boolean verifier(String text) {
        try {
            Float.parseFloat(text);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
