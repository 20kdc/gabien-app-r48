/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import gabien.datum.DatumSymbol;
import gabien.datum.DatumWriter;
import r48.minivm.MVMEnvironment.Slot;
import r48.minivm.expr.MVMCExpr;

/**
 * The compiler lives here!
 * Created 28th February 2023.
 */
public final class MVMCompileScope {
    public final MVMEnvironment context;
    // If this is a top-level compile scope. This changes definition behaviour.
    public final boolean isTopLevel;
    public final boolean[] fastLocalsAlloc = new boolean[8];
    public final HashMap<DatumSymbol, Local> locals;

    public MVMCompileScope(MVMEnvironment ctx, boolean topLevel) {
        context = ctx;
        isTopLevel = topLevel;
        locals = new HashMap<>();
    }
    public MVMCompileScope(MVMCompileScope cs) {
        this(cs, cs.context);
    }
    public MVMCompileScope(MVMCompileScope cs, MVMEnvironment ctx) {
        context = ctx;
        isTopLevel = false;
        locals = new HashMap<>(cs.locals);
        System.arraycopy(cs.fastLocalsAlloc, 0, fastLocalsAlloc, 0, 8);
    }

    public MVMCExpr compile(Object o) {
        if (o instanceof String || o instanceof Boolean || o instanceof Number || o == null) {
            return new MVMCExpr.Const(o);
        } else if (o instanceof DatumSymbol) {
            // Local
            Local lcl = locals.get(o);
            if (lcl != null)
                return lcl.getter();
            // Context
            Slot s = context.getSlot((DatumSymbol) o);
            if (s != null)
                return s;
            throw new RuntimeException("Undefined symbol: " + o);
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            Object[] oa = ((List<Object>) o).toArray();
            // Tradition states this is AOK, shush...
            if (oa.length == 0)
                return new MVMCExpr.Const(oa);
            // Call of some kind.
            // What we have to do here is compile the first value, and then retroactively work out if it's a macro.
            MVMCExpr oa1v = compile(oa[0]);
            if (oa1v instanceof Slot) {
                Object sv = ((Slot) oa1v).v;
                if (sv instanceof MVMMacro) {
                    // Macro compile tiiiiimmmeeeee
                    return ((MVMMacro) sv).compile(this, oa);
                }
            }
            final MVMCExpr[] exprs = new MVMCExpr[oa.length - 1];
            for (int i = 0; i < exprs.length; i++)
                exprs[i] = compile(oa[i + 1]);
            return MVMFnCallCompiler.compile(this, oa1v, exprs);
        }
        throw new RuntimeException("Couldn't compile " + o.getClass() + ": " + DatumWriter.objectToString(o));
    }

    public static final class Local {
        public final boolean isFastLocal;
        public final int id;
        public Local(boolean f, int id) {
            isFastLocal = f;
            this.id = id;
        }
        public MVMCExpr getter() {
            if (isFastLocal)
                return MVMCExpr.getL[id];
            return new MVMCExpr() {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    return ctx.get(id);
                }
            };
        }
        // setter deliberately omitted, it works differently between the kinds of local
    }
}
