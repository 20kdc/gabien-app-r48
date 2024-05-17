/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import static gabien.datum.DatumTreeUtils.*;

import gabien.datum.DatumSymbol;
import r48.minivm.MVMU;
import r48.minivm.MVMEnv;
import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.compiler.MVMCompileFrame;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.compiler.MVMSubScope;
import r48.minivm.expr.MVMCBegin;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCLet;
import r48.minivm.expr.MVMCLocal;

/**
 * MiniVM standard library.
 * Created 18th March 2023, separated from MVMBasicsLibrary
 */
public class MVMScopingLibrary {
    public static void add(MVMEnv ctx) {
        // Scheme library
        ctx.defineSlot(sym("define"), new Define()
                .attachHelp("(define K V) | function define: (define (K ARG... [. VA]) STMT...) | bulk define: (define K V K V...) : Defines mutable variables or functions. Bulk define is an R48 extension."));
        ctx.defineSlot(sym("let"), new Let()
                .attachHelp("(let ((K V)...) CODE...) : Creates variables. For constants, more efficient than define."));
        ctx.defineSlot(sym("lambda"), new Lambda()
            .attachHelp("(lambda (ARG... [. VA]) STMT...) : Creates first-class functions. The symbol . splits main args from a var-arg list arg."));
        ctx.defineSlot(sym("set!"), new Set()
            .attachHelp("(set! VAR V) : Sets a variable."));
    }

    /**
     * Creates a lambda expression.
     * The lambda expression must only be executed in scopes with the exact given compile scope.
     * This is because otherwise the local base changes and things start to go very wrong very quickly. 
     */
    public static MVMCExpr lambda(String hint, MVMCompileScope mcs, Object[] argsUC, Object[] call, int base) {
        MVMSubScope lambdaSc = mcs.extendWithFrame();
        LinkedList<MVMCLocal> rootsX = new LinkedList<>();
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
                rootsX.add(lambdaSc.newLocal(aSym, MVMType.ANY));
            }
        }
        final MVMCLocal[] roots = rootsX.toArray(new MVMCLocal[0]);
        // compiled lambda code, but expects to be framed in an MVMFn in the context of the creation
        int exprs = call.length - base;
        final MVMCExpr compiledLambda = exprs == 1 ? lambdaSc.compile(call[base]) : MVMCBegin.of(lambdaSc, call, base, exprs);
        final @Nullable MVMCompileFrame rootFrame = lambdaSc.getFrameIfOwned();
        // finally, confirm
        if (isVA) {
            if (!hasAddedVAList)
                throw new RuntimeException("lambda " + hint + ": VA, but no VA arg");
            return new MVMCExpr(new MVMType.Fn(compiledLambda.returnType)) {
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
        return new MVMCExpr(new MVMType.Fn(compiledLambda.returnType)) {
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

    public static MVMCExpr compileIndividualDefine(MVMCompileScope cs, Object k, Supplier<MVMCExpr> v) {
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
            return MVMCBegin.of(exprs);
        }
    }

    public static final class Let extends MVMMacro {
        public Let() {
            super("let");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length < 1)
                throw new RuntimeException("let needs at least the list of bindings");
            List<Object> bindings = MVMU.cList(call[0]);
            LinkedList<MVMCExpr> letExprs = new LinkedList<>();
            LinkedList<MVMCLocal> letLocals = new LinkedList<>();
            MVMSubScope innerScope = cs.extendMayFrame();
            // compile the bindings
            for (Object binding : bindings) {
                List<Object> b = MVMU.cList(binding);
                if (b.size() != 2)
                    throw new RuntimeException("let binding must be of 2 elements, name and expression");
                Object name = b.get(0);
                Object expr = b.get(1);
                MVMCExpr exprC = cs.compile(expr);
                letLocals.add(innerScope.newLocal((DatumSymbol) name, exprC.returnType));
                letExprs.add(exprC);
            }
            // compile the "begin" contents
            MVMCExpr inner = MVMCBegin.of(innerScope, call, 1, call.length - 1);
            return new MVMCLet(letExprs.toArray(new MVMCExpr[0]), letLocals.toArray(new MVMCLocal[0]), innerScope.getFrameIfOwned(), inner);
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
