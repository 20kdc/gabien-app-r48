/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.LinkedList;

import gabien.datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnv.Slot;
import r48.minivm.MVMEnvR48;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM standard library.
 * Created 28th February 2023.
 */
public class MVMIntegrationLibrary {
    public static void add(MVMEnv ctx) {
        ctx.defineSlot(new DatumSymbol("help")).v = new Help(ctx)
                .attachHelp("(help [TOPIC]) : Helpful information on the given value (if any), or lists symbols in the root context.");
        ctx.defineSlot(new DatumSymbol("include")).v = new Include()
                .attachHelp("(include FILE) : Includes the given file-path. This occurs at compile-time and magically counts as top-level even if it shouldn't.");
        ctx.defineSlot(new DatumSymbol("log")).v = new Log()
                .attachHelp("(log V...) : Logs the given values.");
        ctx.defineSlot(new DatumSymbol("mvm-disasm")).v = new Disasm()
                .attachHelp("(mvm-disasm LAMBDA) : Disassembles the given lambda.");
    }
    public static final class Help extends MVMFn.Fixed {
        final MVMEnv ctx;
        public Help(MVMEnv ctx) {
            super("help");
            this.ctx = ctx;
        }

        @Override
        public Object callDirect() {
            LinkedList<DatumSymbol> ds = new LinkedList<>();
            for (Slot s : ctx.listSlots())
                ds.add(s.s);
            return ds;
        }

        @Override
        public Object callDirect(Object a0) {
            if (a0 instanceof MVMHelpable)
                return ((MVMHelpable) a0).help;
            return null;
        }
    }
    public static final class Include extends MVMMacro {
        public Include() {
            super("include");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            MVMEnvR48 r48 = (MVMEnvR48) cs.context;
            for (int i = 1; i < call.length; i++) {
                String s = (String) call[i];
                r48.include(s);
            }
            return null;
        }
    }
    public static final class Log extends MVMFn.VA {
        public Log() {
            super("log");
        }

        @Override
        public Object callIndirect(Object[] args) {
            for (Object arg : args)
                System.out.println("MVM Log: " + MVMFn.asUserReadableString(arg));
            return null;
        }
    }
    public static final class Disasm extends MVMFn.Fixed {
        public Disasm() {
            super("mvm-disasm");
        }

        @Override
        public Object callDirect(Object a0) {
            if (a0 instanceof MVMCExpr)
                return ((MVMCExpr) a0).disasm();
            if (a0 instanceof MVMLambdaFn)
                return ((MVMLambdaFn) a0).content.disasm();
            throw new RuntimeException("Can't disassemble " + MVMFn.asUserReadableString(a0));
        }
    }
}
