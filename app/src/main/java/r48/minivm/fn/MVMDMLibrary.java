/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import gabien.datum.DatumSymbol;
import r48.dbs.PathSyntax;
import r48.io.data.RORIO;
import r48.minivm.MVMEnv;
import r48.minivm.MVMU;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM standard library.
 * Created 8th March 2023.
 */
public class MVMDMLibrary {
    public static void add(MVMEnv ctx) {
        ctx.defineSlot(new DatumSymbol("dm-at")).v = new DMAt(0)
            .attachHelp("(dm-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-add-at")).v = new DMAt(1)
            .attachHelp("(dm-add-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), adds entry if possible, #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-del-at")).v = new DMAt(2)
            .attachHelp("(dm-del-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), deletes entry, #nil on failure");
        ctx.defLib("dm-a-len", (a) -> {
            if (a == null)
                return null;
            if (((RORIO) a).getType() != '[')
                return null;
            return (long) ((RORIO) a).getALen();
        }).attachHelp("(dm-a-len TARGET) : Gets array length of TARGET. Returns #nil if not-an-array.");
        ctx.defLib("dm-a-ref", (a, i) -> {
            if (a == null)
                return null;
            RORIO ro = (RORIO) a;
            if (ro.getType() != '[')
                return null;
            int idx = MVMU.cInt(i);
            if (idx < 0 || idx >= ro.getALen())
                return null;
            return ro.getAElem(idx);
        }).attachHelp("(dm-a-ref TARGET INDEX) : Gets an array element of TARGET. Returns #nil if not-an-array or index invalid.");
        ctx.defLib("dm-decode", (a) -> {
            if (a == null)
                return null;
            RORIO ro = (RORIO) a;
            switch (ro.getType()) {
            case 'T':
                return Boolean.TRUE;
            case 'F':
                return Boolean.FALSE;
            case '"':
                return ro.decString();
            case ':':
                return ro.getSymbol();
            case 'i':
                return ro.getFX();
            }
            return null;
        }).attachHelp("(dm-decode TARGET) : Converts TARGET from DM terms into Datum terms. Returns #nil on failure.");
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
            if (call.length != 2)
                throw new RuntimeException(nameHint + " expects exactly 2 args (target path)");
            PathSyntax ps = PathSyntax.compile(cs.context, cs.compile(call[0]), MVMU.coerceToString(call[1]));
            if (mode == 1)
                return ps.addProgram;
            if (mode == 2)
                return ps.delProgram;
            return ps.getProgram;
        }
    }
}
