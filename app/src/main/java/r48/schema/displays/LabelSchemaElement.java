/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.displays;

import gabien.ui.UIElement;
import gabien.ui.elements.UILabel;
import r48.App;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;

/**
 * Label schema element.
 * Literally just a label.
 * Created 28th May 2024.
 */
public class LabelSchemaElement extends SchemaElement.Leaf {
    public final FF0 text;

    public LabelSchemaElement(App app, FF0 t) {
        super(app);
        text = t;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return new UILabel(text.r(), app.f.helpTH);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
    }
}
