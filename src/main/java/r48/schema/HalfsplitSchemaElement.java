/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
    public double weight = 0.5d;

    public HalfsplitSchemaElement(SchemaElement va, SchemaElement vb) {
        a = va;
        b = vb;
    }

    public HalfsplitSchemaElement(SchemaElement va, SchemaElement vb, double wei) {
        a = va;
        b = vb;
        weight = wei;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return new UISplitterLayout(a.buildHoldingEditor(target, launcher, path), b.buildHoldingEditor(target, launcher, path), false, weight);
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        a.modifyVal(target, path, setDefault);
        b.modifyVal(target, path, setDefault);
    }
}
