/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.integers;

import gabien.ui.UIElement;
import gabien.ui.elements.UILabel;
import gabien.ui.layouts.UIScrollLayout;
import r48.R48;
import r48.schema.specialized.tbleditors.BitfieldTableCellEditor;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * BitfieldTableCellEditor as a schema element
 * Created on Sep 19 2017
 */
public class BitfieldSchemaElement extends IntegerSchemaElement {
    public final String[] flags;

    public BitfieldSchemaElement(R48 app, int def, String[] f) {
        super(app, def);
        flags = f;
    }

    @Override
    public ActiveInteger buildIntegerEditor(long oldVal, final IIntegerContext context) {
        LinkedList<UIElement> elms = new LinkedList<>();
        final Consumer<Integer> refresh = BitfieldTableCellEditor.installEditor(app, flags, (element) -> {
            elms.add(element);
        }, new AtomicReference<Consumer<Integer>>((integer) -> {
            context.update((long) (int) integer);
        }));
        refresh.accept((int) oldVal);
        elms.add(new UILabel(T.s.manualEdit, app.f.tableElementTH));
        final ActiveInteger ai = super.buildIntegerEditor(oldVal, context);
        elms.add(ai.uie);
        final UIScrollLayout uiSVL = context.newSVL();
        uiSVL.panelsSet(elms);
        return new ActiveInteger(uiSVL, (aLong) -> {
            refresh.accept((int) (long) aLong);
            ai.onValueChange.accept(aLong);
        });
    }
}
