/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.compiler;

import java.util.List;

import gabien.datum.DatumSymbol;
import gabien.uslx.append.ISupplier;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnv.Slot;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.fn.MVMMacro;

/**
 * The compiler lives here!
 * Created 28th February 2023.
 */
public abstract class MVMCompileScope {
    public final MVMEnv context;

    public MVMCompileScope(MVMEnv ctx) {
        context = ctx;
    }
    public MVMCompileScope(MVMCompileScope cs) {
        context = cs.context;
    }

    /**
     * Compiles a define in this scope.
     * Note the supplier. The define/local must be in place before the expression is given.
     */
    public abstract MVMCExpr compileDefine(DatumSymbol sym, ISupplier<MVMCExpr> value);

    /**
     * Extends with a formal frame boundary.
     * This means you're responsible for frame.wrapRoot!
     */
    public abstract MVMSubScope extendWithFrame();

    /**
     * Extends in a chill manner.
     */
    public abstract MVMCompileScope extendMayFrame();

    public MVMCExpr readLookup(DatumSymbol ds) {
        // Context
        Slot s = context.getSlot(ds);
        if (s != null)
            return s;
        throw new RuntimeException("Undefined symbol: " + ds);
    }

    /**
     * Compiles an object.
     */
    public final MVMCExpr compile(Object o) {
        if (o instanceof DatumSymbol) {
            return readLookup((DatumSymbol) o);
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            Object[] oa = ((List<Object>) o).toArray();
            // Tradition states this is AOK, shush...
            if (oa.length == 0)
                return new MVMCExpr.Const(oa);
            // Call of some kind.
            // What we have to do here is compile the first value, and then retroactively work out if it's a macro.
            MVMCExpr oa1v = compile(oa[0]);
            Object effectiveValueForMacroLookup = null;
            if (oa1v instanceof Slot) {
                effectiveValueForMacroLookup = ((Slot) oa1v).v;
            } else if (oa1v instanceof MVMCExpr.Const) {
                effectiveValueForMacroLookup = ((MVMCExpr.Const) oa1v).value;
            }
            if (effectiveValueForMacroLookup instanceof MVMMacro) {
                // Macro compile tiiiiimmmeeeee
                MVMCExpr macroRes = ((MVMMacro) effectiveValueForMacroLookup).compile(this, oa);
                if (macroRes == null)
                    return new MVMCExpr.Const(null);
                return macroRes;
            }
            final MVMCExpr[] exprs = new MVMCExpr[oa.length - 1];
            for (int i = 0; i < exprs.length; i++)
                exprs[i] = compile(oa[i + 1]);
            return MVMFnCallCompiler.compile(this, oa1v, exprs);
        } else if (o instanceof MVMCExpr) {
            // Expression compiles to itself.
            // If you're even messing around with these objects, you're expected to know what you're doing.
            return (MVMCExpr) o;
        } else {
            return new MVMCExpr.Const(o);
        }
    }
}
