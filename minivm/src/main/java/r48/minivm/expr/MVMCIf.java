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
import r48.minivm.MVMU;

/**
 * MiniVM if
 * Created 2nd March 2023.
 */
public final class MVMCIf extends MVMCExpr {
    public final @NonNull MVMCExpr c;
    public final @Nullable MVMCExpr a, b;

    public MVMCIf(@NonNull MVMCExpr c, @Nullable MVMCExpr a, @Nullable MVMCExpr b) {
        this.c = c;
        this.a = a;
        this.b = b;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        Object res = c.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        MVMCExpr x = MVMU.isTruthy(res) ? a : b;
        return x != null ? x.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7) : res;
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("if"), c.disasm(), a != null ? a.disasm() : null, b != null ? b.disasm() : null);
    }
}