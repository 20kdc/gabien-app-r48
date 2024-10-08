/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.compiler;

import java.util.function.Supplier;

import datum.DatumSrcLoc;
import datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMSlot;
import r48.minivm.MVMType;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCSetSlot;

/**
 * Forked from MVMCompileScope for reasons.
 * Created 1st March 2023.
 */
public class MVMToplevelScope extends MVMCompileScope {
    public MVMToplevelScope(MVMEnv ctx, DatumSrcLoc srcLoc) {
        super(ctx, srcLoc);
    }
    private MVMToplevelScope(MVMToplevelScope tls) {
        super(tls);
    }

    @Override
    public MVMCExpr compileDefine(DatumSymbol sym, Supplier<MVMCExpr> value) {
        MVMSlot slot = context.ensureSlot(sym);
        try {
            MVMCExpr expr = value.get();
            slot.type = expr.returnType;
            return new MVMCSetSlot(slot, expr);
        } catch (Exception ex) {
            throw new RuntimeException("during " + sym + " definition", ex);
        }
    }

    @Override
    public MVMCExpr compileDefine(DatumSymbol sym, MVMType type, Supplier<MVMCExpr> value) {
        MVMSlot slot = context.ensureSlot(sym);
        // for ANY, this will be fine
        slot.type.assertCanImplicitlyCastFrom(type, "compileDefine of " + sym);
        slot.type.assertCanImplicitlyCastTo(type, "compileDefine of " + sym);
        slot.type = type;
        try {
            return new MVMCSetSlot(slot, value.get());
        } catch (Exception ex) {
            throw new RuntimeException("during " + sym + " definition", ex);
        }
    }

    @Override
    public MVMSubScope extendWithFrame() {
        return new MVMSubScope(this);
    }

    @Override
    public MVMSubScope extendMayFrame() {
        return new MVMSubScope(this);
    }
}
