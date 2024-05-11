/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import r48.dbs.IProxySchemaElement;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Overrides the UI. Useful for small enums with "micro" UIs.
 * Created 16th February 2024.
 */
public class UIOverrideSchemaElement extends SchemaElement implements IProxySchemaElement {
    public final @NonNull SchemaElement data, ui;

    public UIOverrideSchemaElement(@NonNull SchemaElement d, @NonNull SchemaElement u) {
        super(d.app);
        data = d;
        ui = u;
    }

    @Override
    public SchemaElement getEntry() {
        return data;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return ui.buildHoldingEditor(target, launcher, path);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        data.modifyVal(target, path, setDefault);
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        data.visitChildren(target, path, v, detailedPaths);
    }
}
