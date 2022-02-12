/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.displays;

import gabien.uslx.append.*;
import gabien.ui.UIPublicPanel;
import gabien.ui.UISplitterLayout;
import r48.imagefx.HueShiftImageEffect;
import r48.schema.integers.IntegerSchemaElement;

/**
 * Created on 7/31/17.
 */
public class HuePickerSchemaElement extends IntegerSchemaElement {
    public HuePickerSchemaElement() {
        super(0);
    }

    @Override
    public ActiveInteger buildIntegerEditor(final long oldVal, IntegerSchemaElement.IIntegerContext context) {
        final UIPublicPanel uie = TonePickerSchemaElement.createTotem(new HueShiftImageEffect((int) oldVal));
        final ActiveInteger ai2 = super.buildIntegerEditor(oldVal, context);
        return new ActiveInteger(new UISplitterLayout(ai2.uie, uie, true, 0.5), new IConsumer<Long>() {
            @Override
            public void accept(Long aLong) {
                uie.baseImage = TonePickerSchemaElement.compositeTotem(new HueShiftImageEffect((int) oldVal));
                ai2.onValueChange.accept(aLong);
            }
        });
    }

    @Override
    public long filter(long i) {
        return i % 360;
    }
}
