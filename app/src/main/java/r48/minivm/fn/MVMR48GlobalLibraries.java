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
public class MVMR48GlobalLibraries {
    public static void add(MVMEnv ctx, InterlaunchGlobals ilg) {
        MVMCoreLibraries.add(ctx);
        MVMIntegrationLibrary.add(ctx);
        // Data Model library
        ctx.defineSlot(new DatumSymbol("dm-at")).v = new DMAt(0)
                .attachHelp("(dm-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-add-at")).v = new DMAt(1)
                .attachHelp("(dm-add-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), adds entry if possible, #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-del-at")).v = new DMAt(2)
                .attachHelp("(dm-del-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), deletes entry, #nil on failure");
    }

    public static final class DMAt extends MVMMacro {
        public final int mode;
        private static String tName(int mode) {
            String name = "dm-at";
            if (mode == 1)
                name = "dm-add-at";
            if (mode == 2)
                name = "dm-del-at";
            return name;
        }
        public DMAt(int mode) {
            super(tName(mode));
            this.mode = mode;
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 3)
                throw new RuntimeException(nameHint + " expects exactly 2 args (target path)");
            PathSyntax ps = PathSyntax.compile(cs.context, cs.compile(call[1]), coerceToString(call[2]));
            if (mode == 1)
                return ps.addProgram;
            if (mode == 2)
                return ps.delProgram;
            return ps.getProgram;
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
