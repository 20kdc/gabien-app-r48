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
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;
import r48.tr.DynTrBase;
import r48.tr.DynTrSlot;

/**
 * Translation library.
 * 
 * Created 13th March 2023.
 */
public class MVMTrLibrary {
    public static void add(MVMEnvR48 ctx) {
        ctx.defLib("tr-set!", (a0, a1) -> {
            ((DynTrSlot) a0).setValue(a1);
            return a1;
        }).attachHelp("(tr-set! DYNTR VALUE) : Compiles a value into a dynamic translation entry. Beware VALUE is unquoted, and tr-set! itself does it's own form of compilation, so writing code directly as VALUE may have unexpected effects.");

        ctx.defineSlot(new DatumSymbol("tr")).v = new Tr()
                .attachHelp("(tr VAL ARGS...) : Runs a translation routine.");

        ctx.defineSlot(new DatumSymbol("define-name")).v = new DefineName("define-name", false)
                .attachHelp("(define-name KEY CONTENT...) : Defines a name routine.");
        ctx.defineSlot(new DatumSymbol("define-name-nls")).v = new DefineName("define-name-nls", true)
                .attachHelp("(define-name-nls KEY CONTENT...) : Defines a non-localizable name routine.");

        ctx.defineSlot(new DatumSymbol("define-tr")).v = new DefineTr("define-tr", null, false)
                .attachHelp("(define-name KEY EXPR) : Defines a DynTrSlot.");
        ctx.defineSlot(new DatumSymbol("define-tr-nls")).v = new DefineTr("define-tr-nls", null, true)
                .attachHelp("(define-name-nls KEY EXPR) : Defines a non-localizable DynTr 'sort of slot'.");
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
            ((MVMEnvR48) cs.context).dTrName(cs.topLevelSrcLoc, ((DatumSymbol) call[0]).id, DynTrSlot.DYNTR_FF1, l, isNLS);
            return null;
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
            ((MVMEnvR48) cs.context).dynTrBase(cs.topLevelSrcLoc, ((DatumSymbol) call[0]).id, mode, call[1], null, isNLS);
            return null;
        }
    }
    public static class Tr extends MVMFn {
        public Tr() {
            super("tr");
        }
        @Override
        protected Object callDirect() {
            throw new RuntimeException("Invalid arg count");
        }
        @Override
        public Object callDirect(Object a0) {
            return ((DynTrBase) a0).r();
        }
        @Override
        public Object callDirect(Object a0, Object a1) {
            return ((DynTrBase) a0).r(a1);
        }
        @Override
        public Object callDirect(Object a0, Object a1, Object a2) {
            return ((DynTrBase) a0).r(a1, a2);
        }
        @Override
        public Object callDirect(Object a0, Object a1, Object a2, Object a3) {
            return ((DynTrBase) a0).r(a1, a2, a3);
        }
        @Override
        protected Object callIndirect(Object[] args) {
            switch (args.length) {
            case 1:
                return ((DynTrBase) args[0]).r();
            case 2:
                return ((DynTrBase) args[0]).r(args[1]);
            case 3:
                return ((DynTrBase) args[0]).r(args[1], args[2]);
            case 4:
                return ((DynTrBase) args[0]).r(args[1], args[2], args[3]);
            case 5:
                return ((DynTrBase) args[0]).r(args[1], args[2], args[3], args[4]);
            }
            throw new RuntimeException("Invalid arg count");
        }
    }
}
