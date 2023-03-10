/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.compiler;

import gabien.datum.DatumSymbol;
import gabien.uslx.append.ISupplier;
import r48.minivm.MVMEnv;
import r48.minivm.MVMSlot;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCSetSlot;

/**
 * Forked from MVMCompileScope for reasons.
 * Created 1st March 2023.
 */
public class MVMToplevelScope extends MVMCompileScope {
    public MVMToplevelScope(MVMEnv ctx) {
        super(ctx);
    }
    private MVMToplevelScope(MVMToplevelScope tls) {
        super(tls);
    }

    @Override
    public MVMCExpr compileDefine(DatumSymbol sym, ISupplier<MVMCExpr> value) {
        MVMSlot slot = context.ensureSlot(sym);
        try {
            return new MVMCSetSlot(slot, value.get());
        } catch (Exception ex) {
            throw new RuntimeException("during " + sym + " definition", ex);
        }
    }

    @Override
    public MVMToplevelScope extendMayFrame() {
        return new MVMToplevelScope(this);
    }

    @Override
    public MVMSubScope extendWithFrame() {
        return new MVMSubScope(this);
    }
}
