/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import gabien.datum.DatumSymbol;
import r48.app.InterlaunchGlobals;
import r48.dbs.PathSyntax;
import r48.minivm.MVMEnv;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM standard library.
 * Created 28th February 2023.
 */
public class MVMGlobalLibrary {
    public static void add(MVMEnv ctx, InterlaunchGlobals ilg) {
        MVMBasicsLibrary.add(ctx);
        MVMIntegrationLibrary.add(ctx);
        // Data Model library
        ctx.defineSlot(new DatumSymbol("dm-at")).v = new DMAt()
                .attachHelp("(dm-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or null)");
    }

    public static final class DMAt extends MVMMacro {
        public DMAt() {
            super("dm-at");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 3)
                throw new RuntimeException("dm-at expects exactly 2 args (target path)");
            return PathSyntax.compile(cs.context, cs.compile(call[1]), coerceToString(call[2])).getProgram;
        }
    }

    public static String coerceToString(Object object) {
        if (object instanceof String)
            return (String) object;
        if (object instanceof DatumSymbol)
            return ((DatumSymbol) object).id;
        return object.toString();
    }
}
