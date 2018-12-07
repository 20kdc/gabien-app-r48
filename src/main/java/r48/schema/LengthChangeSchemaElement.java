/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on Sep 17 2017
 */
public class LengthChangeSchemaElement extends SchemaElement {
    public String translatedText;
    public int targetLen;
    // this implies that this element is responsible for creating the array
    public boolean defaultLen;

    public LengthChangeSchemaElement(String tx, int l, boolean def) {
        translatedText = tx;
        targetLen = l;
        defaultLen = def;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        // This was hooked up to a button preserver by accident. Useless because it's a toggle now.
        return new UITextButton(translatedText, FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                int alen;
                for (alen = target.getALen(); alen < targetLen; alen++)
                    target.addAElem(alen);
                for (; alen > targetLen; alen--)
                    target.rmAElem(alen - 1);
                path.changeOccurred(false);
            }
        }).togglable(target.getALen() == targetLen);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (defaultLen) {
            if (checkType(target, '[', null, setDefault)) {
                target.setArray();
                int alen;
                while ((alen = target.getALen()) < targetLen)
                    target.addAElem(alen);
                path.changeOccurred(true);
            }
        }
    }
}
