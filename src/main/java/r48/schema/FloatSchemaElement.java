/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
        System.arraycopy(target.strVal, 0, text, 0, text.length);
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
