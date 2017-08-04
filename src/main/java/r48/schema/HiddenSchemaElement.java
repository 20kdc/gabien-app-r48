/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema;

import gabien.ui.IFunction;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.RubyIO;
import r48.dbs.IProxySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Allows for things to disappear & appear as needed.
 * Created on 04/08/17.
 */
public class HiddenSchemaElement extends SchemaElement implements IProxySchemaElement {
    public final SchemaElement content;
    public final IFunction<RubyIO, Boolean> show;

    public HiddenSchemaElement(SchemaElement hide, IFunction<RubyIO, Boolean> shouldShow) {
        content = hide;
        show = shouldShow;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        if (show.apply(target))
            return content.buildHoldingEditor(target, launcher, path);
        UIPanel panel = new UIPanel();
        panel.setBounds(new Rect(0, 0, 0, 0));
        return panel;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        content.modifyVal(target, path, setDefault);
    }

    @Override
    public SchemaElement getEntry() {
        return content;
    }
}
