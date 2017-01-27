/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UINumberBox;
import r48.schema.util.ISchemaHost;
import r48.RubyIO;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/29/16.
 */
public class IntegerSchemaElement implements ISchemaElement {
    public int defaultInt;
    public IntegerSchemaElement(int i) {
        defaultInt = i;
    }
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UINumberBox unb = new UINumberBox(false);
        unb.number = (int) target.fixnumVal;
        unb.readOnly = isReadOnly();
        unb.onEdit = new Runnable() {
            @Override
            public void run() {
                target.fixnumVal = unb.number;
                path.changeOccurred(false);
            }
        };
        unb.setBounds(new Rect(0, 0, 9, 9));
        return unb;
    }

    public boolean isReadOnly() {
        return false;
    }

    @Override
    public int maxHoldingHeight() {
        return 9;
    }

    // For lack of a better place.
    public static boolean ensureType(RubyIO tgt, char t, boolean setDefault) {
        if (tgt.type != t) {
            tgt.setNull();
            tgt.type = t;
            return true;
        }
        return setDefault;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (ensureType(target, 'i', setDefault)) {
            target.fixnumVal = defaultInt;
            path.changeOccurred(true);
        }
    }
}
