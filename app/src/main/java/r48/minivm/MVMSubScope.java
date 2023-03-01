/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import gabien.datum.DatumSymbol;
import gabien.uslx.append.ISupplier;
import r48.minivm.expr.MVMCExpr;

/**
 * Forked from MVMCompileScope for reasons.
 * Created 1st March 2023.
 */
public class MVMSubScope extends MVMCompileScope {
    // Stack frame.
    public final Frame frame;
    public MVMSubScope(MVMToplevelScope tl) {
        super(tl);
        frame = new Frame();
    }
    private MVMSubScope(MVMSubScope csa, boolean formalFrameBoundary) {
        super(csa);
        MVMSubScope cs = (MVMSubScope) csa; 
        frame = formalFrameBoundary ? new Frame(cs.frame) : cs.frame;
        if (formalFrameBoundary) {
            // Fast locals don't hold across frame boundaries, so remove them.
            for (int i = 0; i < fastLocalsAlloc.length; i++)
                fastLocalsAlloc[i] = false;
            HashSet<DatumSymbol> remove = new HashSet<>();
            for (Map.Entry<DatumSymbol, Local> entry : locals.entrySet())
                if (entry.getValue().parent == null)
                    remove.add(entry.getKey());
            for (DatumSymbol ds : remove)
                locals.remove(ds);
        }
    }

    @Override
    public MVMCExpr compileDefine(DatumSymbol sym, ISupplier<MVMCExpr> value) {
        Local local = frame.allocateLocal();
        locals.put(sym, local);
        final int frameID = frame.frameID;
        final int localID = local.localID;
        final MVMCExpr ex = value.get();
        return new MVMCExpr() {
            @Override
            public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                Object v = ex.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                ctx.set(frameID, localID, v);
                return v;
            }
        };
    }

    @Override
    public MVMSubScope extendNoFrame() {
        return new MVMSubScope(this, false);
    }

    @Override
    public MVMSubScope extendWithFrame() {
        return new MVMSubScope(this, true);
    }
}
