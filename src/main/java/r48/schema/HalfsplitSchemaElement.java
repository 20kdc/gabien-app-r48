/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 29/07/17.
 */
public class HalfsplitSchemaElement extends SchemaElement {
    public SchemaElement a, b;
    public HalfsplitSchemaElement(SchemaElement va, SchemaElement vb) {
        a = va;
        b = vb;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return new UISplitterLayout(a.buildHoldingEditor(target, launcher, path), b.buildHoldingEditor(target, launcher, path), false, 0.5d);
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        a.modifyVal(target, path, setDefault);
        b.modifyVal(target, path, setDefault);
    }
}
