/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 29/07/17.
 */
public class HalfsplitSchemaElement extends SchemaElement {
    public SchemaElement a, b;
    public double weight = 0.5d;

    public HalfsplitSchemaElement(@NonNull SchemaElement va, @NonNull SchemaElement vb) {
        super(va.app);
        a = va;
        b = vb;
    }

    public HalfsplitSchemaElement(@NonNull SchemaElement va, @NonNull SchemaElement vb, double wei) {
        super(va.app);
        a = va;
        b = vb;
        weight = wei;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return new UISplitterLayout(a.buildHoldingEditor(target, launcher, path), b.buildHoldingEditor(target, launcher, path), false, weight);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        a.modifyVal(target, path, setDefault);
        b.modifyVal(target, path, setDefault);
    }
}
