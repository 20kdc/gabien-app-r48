/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import r48.App;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;

/**
 * Button which sets a fixed value and is pressed if that value is set.
 * Created on 16th February, 2024.
 */
public class ValButtonSchemaElement extends SchemaElement.Leaf {
    private final FF0 text;
    private final DMKey value;

    public ValButtonSchemaElement(App app, @NonNull FF0 text2, @NonNull DMKey value) {
        super(app);
        text = text2;
        this.value = value;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        UITextButton utb = new UITextButton(text.r(), app.f.schemaFieldTH, () -> {
            target.setDeepClone(value);
            path.changeOccurred(false);
        }).togglable(RORIO.rubyEquals(target, value));
        return utb;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
    }
}
