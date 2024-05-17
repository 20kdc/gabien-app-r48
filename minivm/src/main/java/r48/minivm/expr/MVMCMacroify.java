/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import org.eclipse.jdt.annotation.NonNull;

import static gabien.datum.DatumTreeUtils.*;

import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.compiler.MVMFnCallCompiler;
import r48.minivm.fn.MVMFn;
import r48.minivm.fn.MVMMacro;

/**
 * MiniVM convert function to macro
 * Created 2nd March 2023.
 */
public final class MVMCMacroify extends MVMCExpr {
    public final MVMCExpr expr;

    public MVMCMacroify(MVMCExpr f) {
        super(MVMType.typeOfClass(MVMMacro.class));
        expr = f;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        final MVMFn fn = MVMFnCallCompiler.asFn(expr.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7));
        return new MVMMacro(fn.nameHint) {
            @Override
            public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
                return cs.compile(fn.clIndirect(call));
            }
        };
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("macroify"), expr.disasm());
    }
}