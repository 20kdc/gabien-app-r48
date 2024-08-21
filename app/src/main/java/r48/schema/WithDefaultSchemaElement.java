/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * With this default.
 * Created 22nd August 2024.
 */
public class WithDefaultSchemaElement extends SchemaElement {
    public final SchemaElement content;
    public final DMKey def;

    public WithDefaultSchemaElement(@NonNull SchemaElement content, @NonNull DMKey def) {
        super(content.app);
        this.content = content;
        this.def = def;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return content.buildHoldingEditor(target, launcher, path);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (setDefault) {
            target.setDeepClone(def);
            content.modifyVal(target, path, false);
            path.changeOccurred(true);
        } else {
            content.modifyVal(target, path, setDefault);
        }
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        content.visit(target, path, v, detailedPaths);
    }
}
