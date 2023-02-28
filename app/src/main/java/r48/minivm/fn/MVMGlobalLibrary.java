/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import gabien.datum.DatumSymbol;
import r48.app.InterlaunchGlobals;
import r48.minivm.MVMCompileScope;
import r48.minivm.MVMEnvironment;
import r48.minivm.MVMMacro;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM standard library.
 * Created 28th February 2023.
 */
public class MVMGlobalLibrary {
    public static void add(MVMEnvironment ctx, InterlaunchGlobals ilg) {
        ctx.defineSlot(new DatumSymbol("quote")).v = new Quote();
    }

    public static final class Quote extends MVMMacro {
        public Quote() {
            super("quote");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 2)
                throw new RuntimeException("quote expects exactly 1 arg");
            return new MVMCExpr.Const(call[1]);
        }
    }
}
