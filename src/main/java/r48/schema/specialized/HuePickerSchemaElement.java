/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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