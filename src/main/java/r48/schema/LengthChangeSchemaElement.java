/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.IFunction;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.IProxySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UINSVertLayout;

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
