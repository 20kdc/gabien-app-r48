/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on November 21, 2018.
 */
public abstract class IRIOAwareSchemaElement extends SchemaElement {
    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return buildHoldingEditor((IRIO) target, launcher, path);
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        modifyVal((IRIO) target, path, setDefault);
    }

    @Override
    public abstract UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path);

    @Override
    public abstract void modifyVal(IRIO target, SchemaPath path, boolean setDefault);
}
