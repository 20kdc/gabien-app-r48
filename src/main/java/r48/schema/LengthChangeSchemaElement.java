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
import r48.schema.integers.IntegerSchemaElement;
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
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        // This is never meant to *actually* scroll.
        UITextButton r = new UITextButton(translatedText, FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                RubyIO[] rubies = new RubyIO[targetLen];
                for (int i = target.arrVal.length; i < rubies.length; i++)
                    rubies[i] = new RubyIO().setNull();
                System.arraycopy(target.arrVal, 0, rubies, 0, Math.min(targetLen, target.arrVal.length));
                target.arrVal = rubies;
                path.changeOccurred(false);
            }
        }) {
            final Runnable r = AggregateSchemaElement.hookButtonForPressPreserve(path, launcher, LengthChangeSchemaElement.this, target, this, "main");

            @Override
            public void update(double deltaTime) {
                super.update(deltaTime);
                r.run();
            }
        }.togglable(target.arrVal.length == targetLen);
        return r;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (defaultLen) {
            if (IntegerSchemaElement.ensureType(target, '[', setDefault)) {
                target.arrVal = new RubyIO[targetLen];
                for (int i = 0; i < target.arrVal.length; i++)
                    target.arrVal[i] = new RubyIO().setNull();
                path.changeOccurred(true);
            }
        }
    }
}
