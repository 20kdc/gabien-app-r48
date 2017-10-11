/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import r48.RubyIO;
import r48.imagefx.HueShiftImageEffect;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 7/31/17.
 */
public class HuePickerSchemaElement extends IntegerSchemaElement {
    public HuePickerSchemaElement() {
        super(0);
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return new UISplitterLayout(super.buildHoldingEditor(target, launcher, path), TonePickerSchemaElement.createTotem(new HueShiftImageEffect((int) target.fixnumVal)), true, 0.5);
    }

    @Override
    public int filter(int i) {
        return i % 360;
    }
}
