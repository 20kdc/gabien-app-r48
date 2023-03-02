/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.Arrays;
import java.util.LinkedList;

import static gabien.datum.DatumTreeUtils.*;
import gabien.datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnv.Slot;
import r48.minivm.MVMU;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.compiler.MVMFnCallCompiler;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM standard library.
 * Created 28th February 2023.
 */
public class MVMExtensionsLibrary {
    public static void add(MVMEnv ctx) {
        // Custom: Clojureisms
        ctx.defineSlot(sym("string->class")).v = new Str2Class()
                .attachHelp("(string->class V) : Gets the given class, or null if unable.");
        ctx.defineSlot(sym("instance?")).v = new InstanceQ()
                .attachHelp("(instance? C V) : Class.isInstance(V)");
        // Custom: Macro facilities
        ctx.defineSlot(sym("procedure->macro")).v = new Macroify()
                .attachHelp("(procedure->macro V) : Wraps a procedure (ARG...) to make it a macro. The returned code is compiled normally.");
        // Custom: Debug
        ctx.defineSlot(sym("mvm-disasm")).v = new Disasm()
                .attachHelp("(mvm-disasm LAMBDA) : Disassembles the given lambda.");
        ctx.defineSlot(sym("help")).v = new Help(ctx)
                .attachHelp("(help [TOPIC]) : Helpful information on the given value (if any), or lists symbols in the root context.");
    }

    public static final class Disasm extends MVMFn.Fixed {
        public Disasm() {
            super("mvm-disasm");
        }

        @Override
        public Object callDirect(Object a0) {
            if (a0 instanceof MVMCExpr)
                return ((MVMCExpr) a0).disasm();
            if (a0 instanceof MVMLambdaFn) {
                MVMLambdaFn l = (MVMLambdaFn) a0;
                return Arrays.asList(sym("λi"), l.rootFrame.isExpectedToExist(), l.content.disasm());
            }
            throw new RuntimeException("Can't disassemble " + MVMU.userStr(a0));
        }
    }

    public static final class InstanceQ extends MVMFn.Fixed {
        public InstanceQ() {
            super("instance?");
        }

        @Override
        public Object callDirect(Object a0, Object a1) {
            return ((Class<?>) a0).isInstance(a1);
        }
    }

    public static final class Str2Class extends MVMFn.Fixed {
        public Str2Class() {
            super("string->class");
        }

        @Override
        public Object callDirect(Object a0) {
            try {
                return Class.forName((String) a0);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    public static final class Macroify extends MVMFn.Fixed {
        public Macroify() {
            super("procedure->macro");
        }

        @Override
        public Object callDirect(Object a0) {
            final MVMFn fn = MVMFnCallCompiler.asFn(a0);
            return new MVMMacro(fn.nameHint) {
                @Override
                public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
                    // for future compatibility
                    call[0] = cs;
                    return cs.compile(fn.callIndirect(call));
                }
            };
        }
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
}
