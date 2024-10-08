/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import static datum.DatumTreeUtils.*;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSymbol;
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
        ctx.defineSlot(sym("define"), new Define())
                .help("(define K [TYPE] V) | function define: (define (K ARG... [. VA]) STMT...) : Defines mutable variables or functions. See lambda.");
        ctx.defineSlot(sym("let"), new Let())
                .help("(let ((K V)...) CODE...) : Creates variables. For constants, more efficient than define.");
        ctx.defineSlot(sym("lambda"), new Lambda())
            .help("(lambda (ARG... [. VA]) STMT...) : Creates first-class functions. The symbol . splits main args from a var-arg list arg. Args can be symbols or (SYM TYPE) for type-checking.");
        ctx.defineSlot(sym("set!"), new Set())
            .help("(set! VAR V) : Sets a variable.");
    }

    /**
     * Creates a lambda expression.
     * The lambda expression must only be executed in scopes with the exact given compile scope.
     * This is because otherwise the local base changes and things start to go very wrong very quickly. 
     */
    public static MVMCExpr lambda(String hint, MVMCompileScope mcs, Object[] argsUC, Object[] call, int base) {
        MVMSubScope lambdaSc = mcs.extendWithFrame();
        LinkedList<MVMCLocal> rootsX = new LinkedList<>();
        LinkedList<MVMType> argTypes = new LinkedList<>();
        boolean isVA = false;
        MVMType vaType = null;
        for (Object arg : argsUC) {
            MVMType argType = null;
            DatumSymbol argName = null;
            if (arg instanceof DatumSymbol) {
                DatumSymbol aSym = (DatumSymbol) arg;
                if (aSym.id.equals(".")) {
                    if (isVA)
                        throw new RuntimeException("lambda " + hint + " can't be a VA twice!");
                    isVA = true;
                    continue;
                }
                argType = MVMType.ANY;
                argName = aSym;
            } else if (arg instanceof List) {
                List<?> la = (List<?>) arg;
                if (la.size() != 2)
                    throw new RuntimeException("lambda " + hint + ": arg " + MVMU.userStr(arg) + " looks typed but is weird");
                argType = mcs.context.getType(la.get(1), "lambda " + hint + " arg");
                Object name = la.get(0);
                if (!(name instanceof DatumSymbol))
                    throw new RuntimeException("lambda " + hint + ": arg " + MVMU.userStr(arg) + " looks typed but name isn't a symbol");
                argName = (DatumSymbol) name;
            } else {
                throw new RuntimeException("lambda " + hint + ": arg " + MVMU.userStr(arg) + " expected to be sym or (SYM TYPE)");
            }
            if (isVA) {
                if (vaType != null)
                    throw new RuntimeException("lambda " + hint + " can't have two VA lists!");
                vaType = argType;
                rootsX.add(lambdaSc.newLocal(argName, new MVMType.TypedList(argType)));
            } else {
                argTypes.add(argType);
                rootsX.add(lambdaSc.newLocal(argName, argType));
            }
        }
        final MVMType[] argTypesArray = argTypes.toArray(new MVMType[0]);
        final MVMCLocal[] roots = rootsX.toArray(new MVMCLocal[0]);
        // compiled lambda code, but expects to be framed in an MVMFn in the context of the creation
        int exprs = call.length - base;
        final MVMCExpr compiledLambda = exprs == 1 ? lambdaSc.compile(call[base]) : MVMCBegin.of(lambdaSc, call, base, exprs);
        final @Nullable MVMCompileFrame rootFrame = lambdaSc.getFrameIfOwned();
        // finally, confirm
        MVMType.Fn fnType = new MVMType.Fn(compiledLambda.returnType, argTypesArray.length, argTypesArray, vaType);
        if (isVA) {
            if (vaType == null)
                throw new RuntimeException("lambda " + hint + ": VA, but no VA arg");
            return new MVMCExpr(fnType) {
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
        return new MVMCExpr(fnType) {
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

    public static MVMCExpr compileIndividualDefine(MVMCompileScope cs, Object k, Object t, Supplier<MVMCExpr> v) {
        if (!(k instanceof DatumSymbol))
            throw new RuntimeException(MVMU.userStr(k) + " expected to be sym");
        DatumSymbol k2 = (DatumSymbol) k;
        try {
            return cs.compileDefine(k2, cs.context.getType(t, "typeof-" + k2.id), v);
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
            if (call.length == 0)
                throw new RuntimeException("can't have empty global define");
            if (call.length == 1)
                throw new RuntimeException("can't have global define with only one arg");
            if (call.length == 2)
                return compileIndividualDefine(cs, call[0], () -> cs.compile(call[1]));
            if (call.length == 3)
                return compileIndividualDefine(cs, call[0], call[1], () -> cs.compile(call[2]));
            throw new RuntimeException("invalid define format");
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
