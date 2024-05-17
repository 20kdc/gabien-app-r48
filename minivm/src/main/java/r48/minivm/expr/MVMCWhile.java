/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import static gabien.datum.DatumTreeUtils.*;

import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;

/**
 * MiniVM while
 * Created 16th April 2023.
 */
public final class MVMCWhile extends MVMCExpr {
    public final @NonNull MVMCExpr c;
    public final @Nullable MVMCExpr v;

    public MVMCWhile(@NonNull MVMCExpr c, @Nullable MVMCExpr v) {
        super(MVMType.ANY);
        this.c = c;
        this.v = v;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        Object res = Boolean.FALSE;
        while (MVMU.isTruthy(c.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7)))
            res = v.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        return res;
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("while"), c.disasm(), v.disasm());
    }
}