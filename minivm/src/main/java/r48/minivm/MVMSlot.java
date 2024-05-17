/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import static gabien.datum.DatumTreeUtils.sym;

import org.eclipse.jdt.annotation.NonNull;

import gabien.datum.DatumSymbol;
import r48.minivm.expr.MVMCExpr;

/**
 * A Slot represents a stored value in the context.
 * Slots are also the expressions for retrieving them for execution efficiency reasons.
 * Pulled out of MVMEnv 10th March 2023.
 */
public final class MVMSlot extends MVMCExpr {
    public final DatumSymbol s;
    public final MVMType type;
    public Object v;

    public MVMSlot(DatumSymbol s, MVMType t) {
        super(t);
        this.s = s;
        this.type = t;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        return v;
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("slot"), s, sym(type.toString()));
    }
}