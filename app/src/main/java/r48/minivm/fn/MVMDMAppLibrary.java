/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import gabien.datum.DatumSymbol;
import r48.App;
import r48.dbs.FormatSyntax;
import r48.io.data.RORIO;
import r48.minivm.MVMEnv;
import r48.minivm.MVMU;

/**
 * MiniVM standard library.
 * Created 8th March 2023.
 */
public class MVMDMAppLibrary {
    public static void add(MVMEnv ctx, App app) {
        ctx.defineSlot(new DatumSymbol("dm-fmt")).v = new DMFmt(app.fmt)
                .attachHelp("(dm-fmt TARGET [NAME/#nil [PREFIXENUMS]]) : Passes to FormatSyntax.interpretParameter. If the passed-in object is null (say, due to a PathSyntax failure) returns the empty string. Important: Because of schemas and stuff this doesn't exist in the static translation context.");
        ctx.defLib("dm-formatsyntax", (text) -> {
            return app.fmt.compile((String) text);
        }).attachHelp("(dm-formatsyntax TEXT) : Compiles FormatSyntax. This is a workaround to run FormatSyntax through the DynTrSlot stuff, so it counts as a compiled DynTrSlot value, but...");
    }
    public static final class DMFmt extends MVMFn.Fixed {
        public final FormatSyntax fmt;
        public DMFmt(FormatSyntax fmt) {
            super("dm-fmt");
            this.fmt = fmt;
        }
        @Override
        public Object callDirect(Object a0) {
            if (a0 == null)
                return "";
            return fmt.interpretParameter((RORIO) a0, (String) null, false);
        }

        @Override
        public Object callDirect(Object a0, Object a1) {
            if (a0 == null)
                return "";
            return fmt.interpretParameter((RORIO) a0, (String) a1, false);
        }

        @Override
        public Object callDirect(Object a0, Object a1, Object a2) {
            if (a0 == null)
                return "";
            return fmt.interpretParameter((RORIO) a0, (String) a1, MVMU.isTruthy(a2));
        }
    }
}
