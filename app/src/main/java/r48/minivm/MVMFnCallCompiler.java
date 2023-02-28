/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import org.eclipse.jdt.annotation.NonNull;

import r48.minivm.expr.MVMCExpr;

/**
 * Rather nonsensical
 * Created 28th February 2023.
 */
public class MVMFnCallCompiler {

    public static MVMCExpr compile(final MVMFn mvmFn, MVMCompileScope cs, MVMCExpr[] exprs) {
        boolean isPure = mvmFn.isPure;
        for (int i = 0; i < exprs.length; i++)
            isPure &= exprs[i].isPure;
        if (exprs.length == 0) {
            return new MVMCExpr(isPure) {
                @Override
                public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    return mvmFn.callDirect();
                }
            };
        } else if (exprs.length == 1) {
            return new MVMCExpr(isPure) {
                @Override
                public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    Object v0 = exprs[0].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.callDirect(v0);
                }
            };
        } else if (exprs.length == 2) {
            return new MVMCExpr(isPure) {
                @Override
                public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    Object v0 = exprs[0].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v1 = exprs[1].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.callDirect(v0, v1);
                }
            };
        } else if (exprs.length == 3) {
            return new MVMCExpr(isPure) {
                @Override
                public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    Object v0 = exprs[0].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v1 = exprs[1].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v2 = exprs[2].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.callDirect(v0, v1, v2);
                }
            };
        } else if (exprs.length == 4) {
            return new MVMCExpr(isPure) {
                @Override
                public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    Object v0 = exprs[0].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v1 = exprs[1].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v2 = exprs[2].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v3 = exprs[3].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.callDirect(v0, v1, v2, v3);
                }
            };
        } else {
            return new MVMCExpr(isPure) {
                @Override
                public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    Object[] v = new Object[exprs.length];
                    for (int i = 0; i < v.length; i++)
                        v[i] = exprs[i].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.callIndirect(v);
                }
            };
        }
    }

}
