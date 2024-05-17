/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.compiler;

import static gabien.datum.DatumTreeUtils.sym;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.fn.MVMFn;

/**
 * Rather nonsensical
 * Created 28th February 2023.
 */
public class MVMFnCallCompiler {

    public static MVMCExpr compile(MVMCompileScope cs, MVMCExpr call, MVMCExpr[] exprs) {
        MVMType infer = MVMType.ANY;
        if (call.returnType instanceof MVMType.Fn) {
            MVMType.Fn fnType = (MVMType.Fn) call.returnType;
            if (exprs.length < fnType.minArgs)
                throw new RuntimeException("Too few args for " + call + ": " + fnType);
            for (int i = 0; i < exprs.length; i++) {
                MVMType expected = fnType.argAt(i);
                if (expected == null)
                    throw new RuntimeException("Too many args for " + call + ": " + fnType);
                expected.assertCanImplicitlyCastFrom(exprs[i].returnType, call);
            }
            infer = fnType.returnType;
        }

        if (exprs.length == 0) {
            return new MVMCExpr(infer) {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    MVMFn mvmFn = asFn(call.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7));
                    return mvmFn.clDirect();
                }

                @Override
                public Object disasm() {
                    return makeDisassembly(call, exprs);
                }
            };
        } else if (exprs.length == 1) {
            return new MVMCExpr(infer) {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    MVMFn mvmFn = asFn(call.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7));
                    Object v0 = exprs[0].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.clDirect(v0);
                }

                @Override
                public Object disasm() {
                    return makeDisassembly(call, exprs);
                }
            };
        } else if (exprs.length == 2) {
            return new MVMCExpr(infer) {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    MVMFn mvmFn = asFn(call.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7));
                    Object v0 = exprs[0].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v1 = exprs[1].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.clDirect(v0, v1);
                }

                @Override
                public Object disasm() {
                    return makeDisassembly(call, exprs);
                }
            };
        } else if (exprs.length == 3) {
            return new MVMCExpr(infer) {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    MVMFn mvmFn = asFn(call.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7));
                    Object v0 = exprs[0].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v1 = exprs[1].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v2 = exprs[2].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.clDirect(v0, v1, v2);
                }

                @Override
                public Object disasm() {
                    return makeDisassembly(call, exprs);
                }
            };
        } else if (exprs.length == 4) {
            return new MVMCExpr(infer) {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    MVMFn mvmFn = asFn(call.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7));
                    Object v0 = exprs[0].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v1 = exprs[1].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v2 = exprs[2].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    Object v3 = exprs[3].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.clDirect(v0, v1, v2, v3);
                }

                @Override
                public Object disasm() {
                    return makeDisassembly(call, exprs);
                }
            };
        } else {
            return new MVMCExpr(infer) {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    MVMFn mvmFn = asFn(call.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7));
                    Object[] v = new Object[exprs.length];
                    for (int i = 0; i < v.length; i++)
                        v[i] = exprs[i].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return mvmFn.clIndirect(v);
                }

                @Override
                public Object disasm() {
                    return makeDisassembly(call, exprs);
                }
            };
        }
    }

    public static Object makeDisassembly(MVMCExpr call, MVMCExpr[] exprs) {
        LinkedList<Object> obj = new LinkedList<>();
        obj.add(sym("call"));
        obj.add(call.disasm());
        for (MVMCExpr ex : exprs)
            obj.add(ex.disasm());
        return obj;
    }

    public static MVMFn asFn(Object execute) {
        return (MVMFn) execute;
    }
}
