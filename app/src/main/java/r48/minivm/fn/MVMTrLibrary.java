/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.Date;
import java.util.LinkedList;

import datum.DatumSymbol;
import r48.io.data.RORIO;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMType;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;
import r48.tr.DynTrBase;
import r48.tr.DynTrSlot;
import r48.tr.TrPage.FF1;

/**
 * Translation library.
 * 
 * Created 13th March 2023.
 */
public class MVMTrLibrary {
    public static void add(MVMEnvR48 ctx) {
        ctx.defLib("tr-set!", MVMType.ANY, MVMEnvR48.DYNTRSLOT_TYPE, MVMType.ANY, (a0, a1) -> {
            ((DynTrSlot) a0).setValue(a1);
            return a1;
        }, "(tr-set! DYNTR VALUE) : Compiles a value into a dynamic translation entry. Beware VALUE is unquoted, and tr-set! itself does it's own form of compilation, so writing code directly as VALUE may have unexpected effects.");

        ctx.defineSlot(new DatumSymbol("define-name"), new DefineName("define-name", false))
                .help("(define-name KEY CONTENT...) : Defines a name routine.");
        ctx.defineSlot(new DatumSymbol("define-name-nls"), new DefineName("define-name-nls", true))
                .help("(define-name-nls KEY CONTENT...) : Defines a non-localizable name routine.");

        ctx.defineSlot(new DatumSymbol("define-tr"), new DefineTr("define-tr", null, false))
                .help("(define-tr KEY EXPR) : Defines a DynTrSlot.");
        ctx.defineSlot(new DatumSymbol("define-tr-nls"), new DefineTr("define-tr-nls", null, true))
                .help("(define-tr-nls KEY EXPR) : Defines a non-localizable DynTr 'sort of slot'.");

        ctx.defLib("r2kts->string", MVMType.STR, MVMEnvR48.RORIO_TYPE, (x) -> {
            RORIO rubyIO = (RORIO) x;
            double d = Double.parseDouble(rubyIO.decString());
            // WARNING: THIS IS MADNESS, and could be off by a few seconds.
            // In practice I tested it and it somehow wasn't off at all.
            // Command used given here:
            // date --date="12/30/1899 12:00 am" +%s
            // -2209161600
            // since we want ms, 3 more 0s have been added
            long v = -2209161600000L;
            long dayLen = 24L * 60L * 60L * 1000L;
            // Ok, so, firstly, fractional part is considered completely separately and absolutely.
            double fractional = Math.abs(d);
            fractional -= Math.floor(fractional);
            // Now get rid of fractional in the "right way" (round towards 0)
            if (d < 0) {
                d += fractional;
            } else {
                d -= fractional;
            }
            v += ((long) d) * dayLen;
            v += (long) (fractional * dayLen);

            // NOTE: This converts to local time zone.
            return new Date(v).toString();
        }, "(r2kts->string OBJ) : Converts an R2K save timestamp, as a RORIO, to a human-readable local time zone string.");
    }
    public static class DefineName extends MVMMacro {
        public final boolean isNLS;
        public DefineName(String id, boolean nls) {
            super(id);
            isNLS = nls;
        }
        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length < 1)
                throw new RuntimeException("define-name needs at least the name of the name");
            LinkedList<Object> l = new LinkedList<>();
            for (int i = 1; i < call.length; i++)
                l.add(call[i]);
            FF1 res = ((MVMEnvR48) cs.context).dTrName(cs.topLevelSrcLoc, ((DatumSymbol) call[0]).id, l, isNLS);
            return new MVMCExpr.Const(res, MVMType.typeOfClass(FF1.class));
        }
    }
    public static class DefineTr extends MVMMacro {
        public final DatumSymbol mode;
        public final boolean isNLS;
        public DefineTr(String id, DatumSymbol mode, boolean nls) {
            super(id);
            this.mode = mode;
            isNLS = nls;
        }
        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 2)
                throw new RuntimeException("define-tr has a name and a value");
            DynTrBase res = ((MVMEnvR48) cs.context).dynTrBase(cs.topLevelSrcLoc, ((DatumSymbol) call[0]).id, mode, call[1], isNLS);
            return new MVMCExpr.Const(res, MVMType.typeOfClass(DynTrBase.class));
        }
    }
}
