/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import static gabien.datum.DatumTreeUtils.*;
import gabien.datum.DatumSymbol;
import gabien.uslx.append.ISupplier;
import r48.minivm.MVMEnv;
import r48.minivm.MVMScope;
import r48.minivm.compiler.MVMCompileFrame;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.compiler.MVMSubScope;
import r48.minivm.compiler.MVMSubScope.LocalRoot;
import r48.minivm.expr.MVMCBegin;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM standard library.
 * Created 28th February 2023.
 */
public class MVMBasicsLibrary {
    public static void add(MVMEnv ctx) {
        // Scheme library
        ctx.defineSlot(sym("quote")).v = new Quote()
                .attachHelp("(quote A) | 'A : A is not evaluated. Allows expressing complex structures inline.");
        ctx.defineSlot(sym("list")).v = new ListFn()
                .attachHelp("(list V...) : Creates a list of values.");
        ctx.defineSlot(sym("define")).v = new Define()
                .attachHelp("(define K V) | function define: (define (K ARG...) STMT...) | bulk define: (define K V K V...) : Defines mutable variables or functions. Bulk define is an R48 extension.");
        ctx.defineSlot(sym("lambda")).v = new Lambda()
                .attachHelp("(lambda (ARG...) STMT...) : Creates first-class functions.");
        // not strictly standard in Scheme, but is standard in Common Lisp, but exact details differ
        ctx.defineSlot(sym("gensym")).v = new Gensym(ctx)
                .attachHelp("(gensym) : Creates a new uniqueish symbol.");
    }

    /**
     * Creates a lambda expression.
     * The lambda expression must only be executed in scopes with the exact given compile scope.
     * This is because otherwise the local base changes and things start to go very wrong very quickly. 
     */
    public static MVMCExpr lambda(String hint, MVMCompileScope mcs, Object[] argsUC, Object[] call, int base) {
        MVMSubScope lambdaSc = mcs.extendWithFrame();
        final LocalRoot[] roots = new LocalRoot[argsUC.length];
        for (int i = 0; i < argsUC.length; i++) {
            Object arg = argsUC[i];
            if (!(arg instanceof DatumSymbol))
                throw new RuntimeException("arg " + MVMFn.asUserReadableString(arg) + " expected to be sym");
            DatumSymbol aSym = (DatumSymbol) arg;
            // actual arg logic
            roots[i] = lambdaSc.newLocal(aSym);
        }
        // compiled lambda code, but expects to be framed in an MVMFn in the context of the creation
        int exprs = call.length - base;
        final MVMCExpr compiledLambda = exprs == 1 ? lambdaSc.compile(call[base]) : new MVMCBegin(lambdaSc, call, base, exprs);
        final MVMCompileFrame rootFrame = lambdaSc.frame;
        return new MVMCExpr() {
            @Override
            public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                return new MVMLambdaFn(hint, ctx, compiledLambda, roots, rootFrame);
            }
            @Override
            public Object disasm() {
                return Arrays.asList(sym("λ"), rootFrame.isExpectedToExist(), compiledLambda.disasm());
            }
        };
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

    public static final class ListFn extends MVMFn.VA {
        public ListFn() {
            super("list");
        }

        @Override
        public Object callIndirect(Object[] args) {
            return Arrays.asList(args);
        }
    }

    public static final class Define extends MVMMacro {
        public Define() {
            super("define");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length >= 2) {
                // is function define possible?
                if (call[1] instanceof List) {
                    // not just possible, but likely
                    @SuppressWarnings("unchecked")
                    List<Object> lo = (List<Object>) call[1];
                    if (lo.size() == 0)
                        throw new RuntimeException("define with what looked like a function decl but an empty list!");
                    final Object head = lo.get(0);
                    final Object[] args = new Object[lo.size() - 1];
                    for (int i = 0; i < args.length; i++)
                        args[i] = lo.get(i + 1);
                    // creating lambda
                    return compileIndividualDefine(cs, head, () -> lambda(head.toString(), cs, args, call, 2));
                }
            }
            int pairEntries = (call.length - 1) / 2;
            if ((pairEntries * 2) + 1 != call.length)
                throw new RuntimeException("define does not match available define formats (CL " + call.length + ")");
            if (pairEntries == 0)
                return null;
            if (pairEntries == 1)
                return compileIndividualDefine(cs, call[1], () -> cs.compile(call[2]));
            MVMCExpr[] exprs = new MVMCExpr[pairEntries];
            int base = 1;
            for (int i = 0; i < exprs.length; i++) {
                final int thisBase = base;
                exprs[i] = compileIndividualDefine(cs, call[thisBase], () -> cs.compile(call[thisBase + 1]));
                base += 2;
            }
            return new MVMCBegin(exprs);
        }

        private MVMCExpr compileIndividualDefine(MVMCompileScope cs, Object k, ISupplier<MVMCExpr> v) {
            if (!(k instanceof DatumSymbol))
                throw new RuntimeException(MVMFn.asUserReadableString(k) + " expected to be sym");
            DatumSymbol k2 = (DatumSymbol) k;
            try {
                return cs.compileDefine(k2, v);
            } catch (Exception ex) {
                throw new RuntimeException("during '" + k + "' definition", ex);
            }
        }
    }

    public static final class Lambda extends MVMMacro {
        public Lambda() {
            super("lambda");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length < 2)
                throw new RuntimeException("Lambda needs at least the args list");
            if (!(call[1] instanceof List))
                throw new RuntimeException("Lambda args list needs to actually be an args list");
            @SuppressWarnings("unchecked")
            List<Object> args = (List<Object>) call[1];
            return lambda(MVMFn.asUserReadableString(call), cs, args.toArray(), call, 2);
        }
    }

    public static final class Gensym extends MVMFn.Fixed {
        public final MVMEnv env;
        public Gensym(MVMEnv env) {
            super("gensym");
            this.env = env;
        }

        @Override
        public Object callDirect() {
            return env.gensym();
        }
    }
}
