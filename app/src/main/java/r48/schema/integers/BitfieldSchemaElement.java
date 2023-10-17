/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.integers;

import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import r48.App;
import r48.schema.specialized.tbleditors.BitfieldTableCellEditor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * BitfieldTableCellEditor as a schema element
 * Created on Sep 19 2017
 */
public class BitfieldSchemaElement extends IntegerSchemaElement {
    public final String[] flags;

    public BitfieldSchemaElement(App app, int def, String[] f) {
        super(app, def);
        flags = f;
    }

    @Override
    public ActiveInteger buildIntegerEditor(long oldVal, final IIntegerContext context) {
        final UIScrollLayout uiSVL = context.newSVL();
        final Consumer<Integer> refresh = BitfieldTableCellEditor.installEditor(app, flags, new Consumer<UIElement>() {
            @Override
            public void accept(UIElement element) {
                uiSVL.panelsAdd(element);
            }
        }, new AtomicReference<Consumer<Integer>>(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                context.update((long) (int) integer);
            }
        }));
        refresh.accept((int) oldVal);
        uiSVL.panelsAdd(new UILabel(T.s.manualEdit, app.f.tableElementTH));
        final ActiveInteger ai = super.buildIntegerEditor(oldVal, context);
        uiSVL.panelsAdd(ai.uie);
        return new ActiveInteger(uiSVL, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                refresh.accept((int) (long) aLong);
                ai.onValueChange.accept(aLong);
            }
        });
    }
}
