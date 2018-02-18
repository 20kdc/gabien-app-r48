/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.integers;

import gabien.ui.UIElement;
import gabien.ui.UINumberBox;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/29/16.
 */
public class IntegerSchemaElement extends SchemaElement {
    public long defaultInt;

    public IntegerSchemaElement(long i) {
        defaultInt = i;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UINumberBox unb = new UINumberBox(target.fixnumVal, FontSizes.schemaFieldTextHeight);
        unb.readOnly = isReadOnly();
        unb.onEdit = new Runnable() {
            @Override
            public void run() {
                target.fixnumVal = filter(unb.number);
                path.changeOccurred(false); // does UI update, yadayadayada
            }
        };
        return unb;
    }

    public boolean isReadOnly() {
        return false;
    }

    public long filter(long i) {
        return i;
    }

    // For lack of a better place.
    public static boolean ensureType(RubyIO tgt, int t, boolean setDefault) {
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
