/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import static gabien.datum.DatumTreeUtils.*;

import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.uslx.append.ISupplier;
import r48.minivm.MVMU;
import r48.minivm.MVMEnv;
import r48.minivm.MVMScope;
import r48.minivm.compiler.MVMCompileFrame;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.compiler.MVMSubScope;
import r48.minivm.compiler.MVMSubScope.LocalRoot;
import r48.minivm.expr.MVMCBegin;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCIf;

/**
 * MiniVM standard library.
 * Created 28th February 2023.
 */
public class MVMBasicsLibrary {
    public static void add(MVMEnv ctx) {
        // Scheme library
        ctx.defineSlot(sym("quote")).v = new Quote()
            .attachHelp("(quote A) | 'A : A is not evaluated. Allows expressing complex structures inline.");
        ctx.defineSlot(sym("begin")).v = new Begin()
            .attachHelp("(begin ...) : Runs a series of expressions and returns the result from the last.");
        ctx.defineSlot(sym("define")).v = new Define()
            .attachHelp("(define K V) | function define: (define (K ARG... [. VA]) STMT...) | bulk define: (define K V K V...) : Defines mutable variables or functions. Bulk define is an R48 extension.");
        ctx.defineSlot(sym("lambda")).v = new Lambda()
            .attachHelp("(lambda (ARG... [. VA]) STMT...) : Creates first-class functions. The symbol . splits main args from a var-arg list arg.");
        ctx.defineSlot(sym("if")).v = new If(false)
            .attachHelp(
                "(if C T [F]) : Conditional primitive.\n" +
                "If C is truthy (any value but #f), then T is executed and the result returned.\n" +
                "If C is falsy (not truthy), then F is executed and the result returned.\n" +
                "If F is not provided, the value provided by C is returned.\n" +
                "Rationale: R2RS, R5RS, and R7RS do not define if's return value in this situation.\n" +
                "Defining it this way makes (and A B C) into (if A (if B C)).\n" +
                "Theoretically, since #f is the only falsy value, this isn't always necessary here.\n" +
                "However, it's important for if-not (useful to implement cond, or...)"
            );
        ctx.defineSlot(sym("if-not")).v = new If(true)
            .attachHelp(
                "(if-not C F [T]) : Conditional primitive.\n" +
                "If C is truthy (any value but #f), then F is executed and the result returned.\n" +
                "If C is falsy (not truthy), then T is executed and the result returned.\n" +
                "If T is not provided, the value provided by C is returned.\n" +
                "Rationale: This is a custom extension to implement or with very little Java code.\n" +
                "So (or A B C) becomes (if-not A (if-not B C)).\n" +
                "In addition this is used for guard-only cond branches."
            );
        ctx.defineSlot(sym("set!")).v = new Set()
            .attachHelp("(set! VAR V) : Sets a variable.");
        ctx.defLib("eq?", MVMU::eqQ)
            .attachHelp("(eq? A B) : Checks two values for near-exact equality, except value types (Character, Long, Double, DatumSymbol).");
        ctx.defLib("eqv?", MVMU::eqvQ)
            .attachHelp("(eqv? A B) : Checks two values for near-exact equality, except value types and strings.");
        ctx.defLib("equal?", MVMU::equalQ)
            .attachHelp("(equal? A B) : Checks two values for deep equality.");
        // not strictly standard in Scheme, but is standard in Common Lisp, but exact details differ
        ctx.defLib("gensym", () -> ctx.gensym())
            .attachHelp("(gensym) : Creates a new uniqueish symbol.");
        // Technically implement just enough of R5RS environments that the parts we're cheating on don't stick out like sore thumbs.
        ctx.defLib("eval", (a0, a1) -> {
            return ((MVMEnv) a1).evalObject(a0, DatumSrcLoc.NONE);
        }).attachHelp("(eval EXPR ENV) : Evaluates EXPR in ENV. Note EXPR is unquoted, so you can dynamically generate it.");
        ctx.defLib("interaction-environment", () -> {
            return ctx;
        }).attachHelp("(interaction-environment) : To put it nicely, this is cheating. It returns the global environment it was defined in, to let eval work.");
    }

    /**
     * Creates a lambda expression.
     * The lambda expression must only be executed in scopes with the exact given compile scope.
     * This is because otherwise the local base changes and things start to go very wrong very quickly. 
     */
    public static MVMCExpr lambda(String hint, MVMCompileScope mcs, Object[] argsUC, Object[] call, int base) {
        MVMSubScope lambdaSc = mcs.extendWithFrame();
        LinkedList<LocalRoot> rootsX = new LinkedList<>();
        boolean isVA = false;
        boolean hasAddedVAList = false;
        for (Object arg : argsUC) {
            if (!(arg instanceof DatumSymbol))
                throw new RuntimeException("lambda " + hint + ": arg " + MVMU.userStr(arg) + " expected to be sym");
            DatumSymbol aSym = (DatumSymbol) arg;
            if (aSym.id.equals(".")) {
                if (isVA)
                    throw new RuntimeException("lambda " + hint + " can't be a VA twice!");
                isVA = true;
            } else {
                if (isVA) {
                    if (hasAddedVAList)
                        throw new RuntimeException("lambda " + hint + " can't have two VA lists!");
                    hasAddedVAList = true;
                }
                // actual arg logic
                rootsX.add(lambdaSc.newLocal(aSym));
            }
        }
        final LocalRoot[] roots = rootsX.toArray(new LocalRoot[0]);
        // compiled lambda code, but expects to be framed in an MVMFn in the context of the creation
        int exprs = call.length - base;
        final MVMCExpr compiledLambda = exprs == 1 ? lambdaSc.compile(call[base]) : new MVMCBegin(lambdaSc, call, base, exprs);
        final MVMCompileFrame rootFrame = lambdaSc.frame;
        // finally, confirm
        if (isVA) {
            if (!hasAddedVAList)
                throw new RuntimeException("lambda " + hint + ": VA, but no VA arg");
            return new MVMCExpr() {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    return new MVMLambdaVAFn(new MVMLambdaFn(hint, ctx, compiledLambda, roots, rootFrame));
                }
                @Override
                public Object disasm() {
                    return MVMU.l(sym("λva"), rootFrame.isExpectedToExist(), compiledLambda.disasm());
                }
            };
        }
        return new MVMCExpr() {
            @Override
            public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                return new MVMLambdaFn(hint, ctx, compiledLambda, roots, rootFrame);
            }
            @Override
            public Object disasm() {
                return MVMU.l(sym("λ"), rootFrame.isExpectedToExist(), compiledLambda.disasm());
            }
        };
    }

    public static MVMCExpr compileFnDefine(MVMCompileScope cs, Object[] call, IFnDefineConverter val) {
        // not just possible, but likely
        if (call.length < 1)
            throw new RuntimeException("define entered function decl handling but wasn't long enough");
        if (!(call[0] instanceof List))
            throw new RuntimeException("define entered function decl handling but wasn't a function decl");
        @SuppressWarnings("unchecked")
        List<Object> lo = (List<Object>) call[0];
        if (lo.size() == 0)
            throw new RuntimeException("define with what looked like a function decl but an empty list!");
        final Object head = lo.get(0);
        final Object[] args = new Object[lo.size() - 1];
        for (int i = 0; i < args.length; i++)
            args[i] = lo.get(i + 1);
        // creating lambda
        return compileIndividualDefine(cs, head, () -> val.convert(head, args));
    }

    public static MVMCExpr compileIndividualDefine(MVMCompileScope cs, Object k, ISupplier<MVMCExpr> v) {
        if (!(k instanceof DatumSymbol))
            throw new RuntimeException(MVMU.userStr(k) + " expected to be sym");
        DatumSymbol k2 = (DatumSymbol) k;
        try {
            return cs.compileDefine(k2, v);
        } catch (Exception ex) {
            throw new RuntimeException("during '" + k + "' definition", ex);
        }
    }

    public static interface IFnDefineConverter {
        MVMCExpr convert(Object head, Object[] args);
    }

    public static final class Quote extends MVMMacro {
        public Quote() {
            super("quote");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 1)
                throw new RuntimeException("quote expects exactly 1 arg");
            return new MVMCExpr.Const(call[0]);
        }
    }

    public static final class Begin extends MVMMacro {
        public Begin() {
            super("begin");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            return new MVMCBegin(cs, call, 0, call.length);
        }
    }

    public static final class Define extends MVMMacro {
        public Define() {
            super("define");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length >= 1) {
                // is function define possible?
                if (call[0] instanceof List) {
                    return compileFnDefine(cs, call, (head, args) -> lambda(head.toString(), cs, args, call, 1));
                }
            }
            int pairEntries = call.length / 2;
            if ((pairEntries * 2) != call.length)
                throw new RuntimeException("define does not match available define formats (CL " + call.length + ")");
            if (pairEntries == 0)
                return null;
            if (pairEntries == 1)
                return compileIndividualDefine(cs, call[0], () -> cs.compile(call[1]));
            MVMCExpr[] exprs = new MVMCExpr[pairEntries];
            int base = 0;
            for (int i = 0; i < exprs.length; i++) {
                final int thisBase = base;
                exprs[i] = compileIndividualDefine(cs, call[thisBase], () -> cs.compile(call[thisBase + 1]));
                base += 2;
            }
            return new MVMCBegin(exprs);
        }
    }

    public static final class Lambda extends MVMMacro {
        public Lambda() {
            super("lambda");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length < 1)
                throw new RuntimeException("Lambda needs at least the args list");
            if (!(call[0] instanceof List))
                throw new RuntimeException("Lambda args list needs to actually be an args list");
            @SuppressWarnings("unchecked")
            List<Object> args = (List<Object>) call[0];
            return lambda(MVMU.userStr(call), cs, args.toArray(), call, 1);
        }
    }

    public static final class If extends MVMMacro {
        public final boolean invert;
        public If(boolean inv) {
            super(inv ? "if-not" : "if");
            invert = inv;
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length < 2)
                throw new RuntimeException("If needs at least the condition and true branch");
            else if (call.length > 3)
                throw new RuntimeException("If cannot have too many parameters");
            else if (call.length == 2) {
                if (invert)
                    return new MVMCIf(cs.compile(call[0]), null, cs.compile(call[1]));
                return new MVMCIf(cs.compile(call[0]), cs.compile(call[1]), null);
            }
            if (invert)
                return new MVMCIf(cs.compile(call[0]), cs.compile(call[2]), cs.compile(call[1]));
            return new MVMCIf(cs.compile(call[0]), cs.compile(call[1]), cs.compile(call[2]));
        }
    }

    public static final class Set extends MVMMacro {
        public Set() {
            super("set!");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 2)
                throw new RuntimeException("Set needs variable name and value, no more or less");
            return cs.writeLookup((DatumSymbol) call[0], cs.compile(call[1]));
        }
    }
}
