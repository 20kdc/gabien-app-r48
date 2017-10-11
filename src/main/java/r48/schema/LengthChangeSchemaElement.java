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
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on Sep 17 2017 (thank you IDEA for my install still being broken and no clue how to fix it without a wipe)
 */
public class LengthChangeSchemaElement extends SchemaElement {
    public String translatedText;
    public int targetLen;

    public LengthChangeSchemaElement(String tx, int l) {
        translatedText = tx;
        targetLen = l;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        // This is never meant to *actually* scroll.
        UITextButton r = new UITextButton(FontSizes.schemaButtonTextHeight, translatedText, new Runnable() {
            @Override
            public void run() {
                RubyIO[] rubies = new RubyIO[targetLen];
                for (int i = target.arrVal.length; i < rubies.length; i++)
                    rubies[i] = new RubyIO().setNull();
                System.arraycopy(target.arrVal, 0, rubies, 0, Math.min(targetLen, target.arrVal.length));
                target.arrVal = rubies;
                path.changeOccurred(false);
            }
        }).togglable();
        r.state = target.arrVal.length == targetLen;
        return r;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
    }
}
