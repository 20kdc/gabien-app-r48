/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.LinkedList;

import gabien.datum.DatumSymbol;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMU;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;
import r48.tr.DynTrSlot;
import r48.tr.NLSTr;

/**
 * Translation library.
 * 
 * Created 13th March 2023.
 */
public class MVMTrLibrary {
    public static void add(MVMEnvR48 ctx) {
        ctx.defLib("tr-nls", (a0) -> new NLSTr(MVMU.coerceToString(a0)))
            .attachHelp("(tr-nls VALUE) : Returns a non-localizable constant that still looks like a translation entry.");
        ctx.defLib("tr-set!", (a0, a1) -> {
            ((DynTrSlot) a0).setValue(a1);
            return a1;
        }).attachHelp("(tr-set! DYNTR VALUE) : Compiles a value into a dynamic translation entry. Beware VALUE is unquoted, and tr-set! itself does it's own form of compilation, so writing code directly as VALUE may have unexpected effects.");
        ctx.defineSlot(new DatumSymbol("define-name")).v = new DefineName()
            .attachHelp("(define-name KEY CONTENT...) : Defines a name routine. This isn't a macro.");
    }
    public static class DefineName extends MVMMacro {
        public DefineName() {
            super("define-name");
        }
        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length < 1)
                throw new RuntimeException("define-name needs at least the name of the name");
            LinkedList<Object> l = new LinkedList<>();
            for (int i = 1; i < call.length; i++)
                l.add(call[i]);
            ((MVMEnvR48) cs.context).dTrName(cs.topLevelSrcLoc, ((DatumSymbol) call[0]).id, DynTrSlot.DYNTR_FF1, l);
            return null;
        }
    }
}
