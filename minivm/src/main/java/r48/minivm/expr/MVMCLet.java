/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumSymbol;
import r48.minivm.MVMScope;
import r48.minivm.compiler.MVMCompileFrame;

/**
 * Extends the scope, writing in locals with values computed using expressions in the calling scope.
 * This is sort of a corollary to MVMLambdaFn in that both of them have uses in avoiding heap allocation when possible.
 * Created March 18th, 2023
 */
public class MVMCLet extends MVMCExpr {
    public final MVMCExpr[] expressions;
    public final MVMCLocal[] roots;
    public final @Nullable MVMCompileFrame rootFrame;
    public final MVMCExpr inner;

    public MVMCLet(MVMCExpr[] e, MVMCLocal[] lr, @Nullable MVMCompileFrame rf, MVMCExpr i) {
        super(i.returnType);
        expressions = e;
        roots = lr;
        rootFrame = rf;
        inner = i;
    }

    @Override
    public Object execute(@NonNull MVMScope octx, Object ol0, Object ol1, Object ol2, Object ol3, Object ol4, Object ol5, Object ol6, Object ol7) {
        MVMScope ictx = MVMCompileFrame.wrapRuntimeScope(rootFrame, octx);
        Object l0 = ol0, l1 = ol1, l2 = ol2, l3 = ol3, l4 = ol4, l5 = ol5, l6 = ol6, l7 = ol7;
        // l0-l7 are a copy of ol0-ol7 at first
        // they are then modified if necessary with the incoming locals from the expressions run in the outer scope
        for (int i = 0; i < expressions.length; i++) {
            Object aV = expressions[i].execute(octx, ol0, ol1, ol2, ol3, ol4, ol5, ol6, ol7);
            MVMCLocal cLocal = roots[i];
            switch (cLocal.getFastSlot()) {
            case 0:
                l0 = aV;
                break;
            case 1:
                l1 = aV;
                break;
            case 2:
                l2 = aV;
                break;
            case 3:
                l3 = aV;
                break;
            case 4:
                l4 = aV;
                break;
            case 5:
                l5 = aV;
                break;
            case 6:
                l6 = aV;
                break;
            case 7:
                l7 = aV;
                break;
            default:
                cLocal.directWrite(ictx, aV);
                break;
            }
        }
        return inner.execute(ictx, l0, l1, l2, l3, l4, l5, l6, l7);
    }

    @Override
    public Object disasm() {
        LinkedList<Object> res = new LinkedList<>();
        res.add(new DatumSymbol("let"));
        for (MVMCExpr ex : expressions)
            res.add(ex.disasm());
        res.add(inner.disasm());
        return res;
    }
}
