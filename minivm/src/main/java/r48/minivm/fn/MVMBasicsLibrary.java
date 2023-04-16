/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import static gabien.datum.DatumTreeUtils.*;

import gabien.datum.DatumSrcLoc;
import r48.minivm.MVMU;
import r48.minivm.MVMEnv;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCBegin;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCIf;
import r48.minivm.expr.MVMCWhile;

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
            try {
                return ((MVMEnv) a1).evalObject(a0, DatumSrcLoc.NONE);
            } catch (Exception ex) {
                throw new RuntimeException("During eval: " + a0, ex);
            }
        }).attachHelp("(eval EXPR ENV) : Evaluates EXPR in ENV. Note EXPR is unquoted, so you can dynamically generate it.");
        ctx.defLib("interaction-environment", () -> {
            return ctx;
        }).attachHelp("(interaction-environment) : To put it nicely, this is cheating. It returns the global environment it was defined in, to let eval work.");
        ctx.defineSlot(sym("error")).v = new Errorer()
            .attachHelp("(error MSG V...) : Throws an exception. No, there's no way to catch these in MVM...");
        ctx.defLib("apply", (a0, a1) -> {
            return ((MVMFn) a0).callIndirect(MVMU.cList(a1).toArray());
        }).attachHelp("(apply FN ARGS) : Runs FN with the list of args as ARGS.");
        // Both S9FES and Guile implement this to enough of a degree that I feel comfortable adding it.
        ctx.defineSlot(sym("while")).v = new While()
                .attachHelp("(while EXPR CODE...) : Repeats CODE until EXPR returns false.");
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
            return MVMCBegin.of(cs, call, 0, call.length);
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

    public static final class Errorer extends MVMFn {
        public Errorer() {
            super("error");
        }
        @Override
        protected Object callDirect() {
            throw new RuntimeException("error needs at least a message");
        }
        @Override
        protected Object callDirect(Object a0) {
            throw new MVMUserException((String) a0);
        }
        @Override
        protected Object callDirect(Object a0, Object a1) {
            throw new MVMUserException((String) a0, a1);
        }
        @Override
        protected Object callDirect(Object a0, Object a1, Object a2) {
            throw new MVMUserException((String) a0, a1, a2);
        }
        @Override
        protected Object callDirect(Object a0, Object a1, Object a2, Object a3) {
            throw new MVMUserException((String) a0, a1, a2, a3);
        }
        @Override
        protected Object callIndirect(Object[] args) {
            Object[] reduced = new Object[args.length - 1];
            System.arraycopy(args, 1, reduced, 0, reduced.length);
            throw new MVMUserException((String) args[0], reduced);
        }
    }

    public static final class While extends MVMMacro {
        public While() {
            super("while");
        }
        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length < 1)
                throw new RuntimeException("While needs at least the expression");
            return new MVMCWhile(cs.compile(call[0]), MVMCBegin.of(cs, call, 1, call.length - 1));
        }
    }
}
