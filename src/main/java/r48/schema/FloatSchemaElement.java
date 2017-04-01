/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import r48.RubyIO;

import java.nio.charset.Charset;

/**
 * Basically a copy of StringSchemaElement with some modifications
 * ~~Created on 12/29/16.~~ cloned on 15 feb.2017
 */
public class FloatSchemaElement extends StringSchemaElement {
    public FloatSchemaElement(String arg) {
        super(arg, 'f');
    }

    // Notably, there's still extra mantissa bit nonsense in there that really shouldn't be.
    // However, it seems always bordered with a 0 byte?
    @Override
    protected void encodeVal(String text, RubyIO target) {
        // Encode normally...
        super.encodeVal(text, target);
    }

    @Override
    protected String decodeVal(RubyIO target) {
        // Stop at the first null byte.
        int firstNull = 0;
        for (int i = 0; i < target.strVal.length; i++) {
            if (target.strVal[i] == 0)
                break;
            firstNull = i + 1;
        }
        byte[] text = new byte[firstNull];
        for (int i = 0; i < text.length; i++)
            text[i] = target.strVal[i];
        return new String(text, Charset.forName("UTF-8"));
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
