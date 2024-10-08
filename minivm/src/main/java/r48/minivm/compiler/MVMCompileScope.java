/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.compiler;

import java.util.List;
import java.util.function.Supplier;

import datum.DatumSrcLoc;
import datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMSlot;
import r48.minivm.MVMType;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCSetSlot;
import r48.minivm.fn.MVMMacro;

/**
 * The compiler lives here!
 * Created 28th February 2023.
 */
public abstract class MVMCompileScope {
    public final DatumSrcLoc topLevelSrcLoc;
    public final MVMEnv context;

    public MVMCompileScope(MVMEnv ctx, DatumSrcLoc srcLoc) {
        context = ctx;
        topLevelSrcLoc = srcLoc;
    }
    public MVMCompileScope(MVMCompileScope cs) {
        context = cs.context;
        topLevelSrcLoc = cs.topLevelSrcLoc;
    }

    /**
     * Compiles a define in this scope.
     * Note the supplier. The define/local must be in place before the expression is given.
     */
    public abstract MVMCExpr compileDefine(DatumSymbol sym, Supplier<MVMCExpr> value);

    /**
     * Compiles a typed define in this scope.
     * Note the supplier. The define/local must be in place before the expression is given.
     */
    public abstract MVMCExpr compileDefine(DatumSymbol sym, MVMType type, Supplier<MVMCExpr> value);

    /**
     * Extends with a formal frame boundary (into a lambdas for example)
     * This means you're responsible for wrapRuntimeScope.
     */
    public abstract MVMSubScope extendWithFrame();

    /**
     * Extends while trying to avoid a formal frame boundary.
     * Beware, this MAY or MAY NOT create a formal frame boundary.
     * So you're still responsible for wrapRuntimeScope.
     * The big advantage is that extendMayFrame won't always frame, so inner-loop lets won't suddenly become storms of endless allocation.
     * A notable disadvantage is that means that having a function complex enough that such a thing is possible will allocate (but once).
     */
    public abstract MVMSubScope extendMayFrame();

    /**
     * Compiles a symbol read.
     */
    public MVMCExpr readLookup(DatumSymbol ds) {
        // Context
        MVMSlot s = context.getSlot(ds);
        if (s != null)
            return s;
        throw new RuntimeException("Undefined symbol: " + ds);
    }

    /**
     * Compiles a symbol write.
     */
    public MVMCExpr writeLookup(DatumSymbol ds, MVMCExpr compile) {
        // Context
        MVMSlot s = context.getSlot(ds);
        if (s != null)
            return new MVMCSetSlot(s, compile);
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
            List<Object> ol = (List<Object>) o;
            int olSize = ol.size();
            // Tradition states this is AOK, shush...
            if (olSize == 0)
                throw new RuntimeException("Cannot compile empty list.");
            // Split into ol1o (first arg) and oa (rest of args)
            Object[] oa = new Object[olSize - 1];
            int idx = -1;
            Object ol1o = null;
            for (Object obj : ol) {
                if (idx == -1)
                    ol1o = obj;
                else
                    oa[idx] = obj;
                idx++;
            }
            // Call of some kind.
            // What we have to do here is compile the first value, and then retroactively work out if it's a macro.
            MVMCExpr ol1v = compile(ol1o);
            Object effectiveValueForMacroLookup = null;
            if (ol1v instanceof MVMSlot) {
                effectiveValueForMacroLookup = ((MVMSlot) ol1v).v;
            } else if (ol1v instanceof MVMCExpr.Const) {
                effectiveValueForMacroLookup = ((MVMCExpr.Const) ol1v).value;
            }
            if (effectiveValueForMacroLookup instanceof MVMMacro) {
                // Macro compile tiiiiimmmeeeee
                MVMCExpr macroRes = ((MVMMacro) effectiveValueForMacroLookup).compile(this, oa);
                if (macroRes == null)
                    return new MVMCExpr.Const(null, MVMType.ANY);
                return macroRes;
            }
            final MVMCExpr[] exprs = new MVMCExpr[oa.length];
            for (int i = 0; i < exprs.length; i++)
                exprs[i] = compile(oa[i]);
            return MVMFnCallCompiler.compile(this, ol1v, exprs);
        } else {
            return new MVMCExpr.Const(o, MVMType.typeOf(o));
        }
    }
}
