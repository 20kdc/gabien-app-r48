/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import org.eclipse.jdt.annotation.NonNull;

import r48.minivm.MVMContext;
import r48.minivm.MVMContext.Slot;

/**
 * Sets a value into a Slot.
 */
public class MVMCSetSlot extends MVMCExpr {
    public final Slot slot;
    public final MVMCExpr value;
    public MVMCSetSlot(Slot s, MVMCExpr val) {
        super(false);
        slot = s;
        value = val;
    }

    @Override
    public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        return slot.v = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
    }
}
