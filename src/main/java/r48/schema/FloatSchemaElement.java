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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Basically a copy of StringSchemaElement with some modifications
 * ~~Created on 12/29/16.~~ cloned on 15 feb.2017
 */
public class FloatSchemaElement extends IRIOAwareSchemaElement {
    public boolean jsonCoerce;
    public String def;

    public FloatSchemaElement(String arg, boolean json) {
        def = arg;
        jsonCoerce = json;
    }

    private String decodeVal(IRIO target) {
        // Stop at the first null byte.
        byte[] strVal = target.getBuffer();
        int firstNull = strVal.length;
        for (int i = 0; i < strVal.length; i++) {
            if (strVal[i] == 0) {
                firstNull = i;
                break;
            }
        }
        byte[] text = new byte[firstNull];
        System.arraycopy(strVal, 0, text, 0, text.length);
        return new String(text, Charset.forName("UTF-8"));
    }

    private boolean encodeVal(IRIO target, String text) {
        if (jsonCoerce) {
            try {
                long l = Long.parseLong(text);
                target.setFX(l);
                return true;
            } catch (NumberFormatException e) {
            }
        }
        try {
            Double.parseDouble(text);
            target.setFloat(text.getBytes("UTF-8"));
            return true;
        } catch (NumberFormatException e) {
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        final String oldValue;
        if (target.getType() == 'f') {
            oldValue = decodeVal(target);
        } else if (jsonCoerce) {
            oldValue = Long.toString(target.getFX());
        } else {
            throw new RuntimeException("No JSONCoerce but got a " + target.getType() + " instead of a float.");
        }
        final UITextBox utb = new UITextBox(oldValue, FontSizes.schemaFieldTextHeight);
        utb.onEdit = new Runnable() {
            @Override
            public void run() {
                if (encodeVal(target, utb.text)) {
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
            if (!encodeVal(target, def))
                throw new RuntimeException("Float default must be valid");
            path.changeOccurred(true);
        }
    }
}
