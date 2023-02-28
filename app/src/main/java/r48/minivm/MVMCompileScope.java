/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import gabien.datum.DatumSymbol;
import gabien.datum.DatumWriter;
import r48.minivm.MVMContext.Slot;
import r48.minivm.expr.MVMCExpr;

/**
 * The compiler lives here!
 * Created 28th February 2023.
 */
public final class MVMCompileScope {
    public final MVMContext context;
    public final DatumSymbol[] locals = new DatumSymbol[8];
    public MVMCompileScope(MVMContext ctx) {
        context = ctx;
    }
    public MVMCompileScope(MVMCompileScope cs) {
        this(cs, cs.context);
    }
    public MVMCompileScope(MVMCompileScope cs, MVMContext ctx) {
        context = ctx;
        System.arraycopy(cs.locals, 0, locals, 0, 8);
    }

    public MVMCExpr compile(Object o) {
        if (o instanceof String || o instanceof Boolean || o instanceof Number || o == null) {
            return new MVMCExpr.Const(o);
        } else if (o instanceof DatumSymbol) {
            // Local
            for (int i = 0; i < 8; i++)
                if (locals[i] != null && o.equals(locals[i]))
                    return MVMCExpr.getL[i];
            // Context
            Slot s = context.getSlot((DatumSymbol) o);
            if (s != null)
                return s;
            throw new RuntimeException("Undefined symbol: " + o);
        } else if (o instanceof Object[]) {
            Object[] oa = (Object[]) o;
            // Tradition states this is AOK, shush...
            if (oa.length == 0)
                return new MVMCExpr.Const(oa);
            // Call of some kind.
            // What we have to do here is compile the first value, and then retroactively work out if it's a macro.
            Object oa1v = compile(oa[0]);
            if (oa1v instanceof Slot) {
                Object sv = ((Slot) oa1v).v;
                if (sv instanceof MVMMacro) {
                    // Macro compile tiiiiimmmeeeee
                    return ((MVMMacro) sv).compile(this, oa);
                }
            }
        }
        throw new RuntimeException("Couldn't compile: " + DatumWriter.objectToString(o));
    }
}
