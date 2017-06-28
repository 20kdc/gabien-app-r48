/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.cmgb;

import gabien.ui.UILabel;
import gabien.ui.UIPanel;
import r48.RubyIO;
import r48.schema.specialized.cmgb.IGroupBehavior;
import r48.schema.util.SchemaPath;

/**
 * Created on 6/28/17.
 */
public class MessageboxGroupEditor implements IGroupEditor {
    public final int idx, len;
    public MessageboxGroupEditor(int index, int l) {
        idx = index;
        len = l;
    }

    @Override
    public UIPanel getEditor(final RubyIO target, final SchemaPath arrayPath) {
        return new UILabel("NYI", 16);
    }

    @Override
    public int getLength() {
        return len;
    }
}
