/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import static datum.DatumTreeUtils.*;

import java.util.LinkedList;

import datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMScope;
import r48.minivm.MVMU;
import r48.minivm.MVMSlot;
import r48.minivm.MVMType;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCMacroify;

/**
 * MiniVM standard library.
 * Created 28th February 2023.
 */
public class MVMExtensionsLibrary {
    public static void add(MVMEnv ctx) {
        // Custom: Typed Racketisms
        ctx.defineSlot(sym("define-type"), new DefineType())
            .help("(define-type NAME TYPE) : Defines a name for a type. Should be top-level only as this is not scoped.");
        // Custom: Clojureisms
        ctx.defLib("string->class", MVMType.typeOfClass(Class.class), MVMType.STR, (a0) -> {
            try {
                return Class.forName((String) a0);
            } catch (ClassNotFoundException e) {
                // After what happened with the Datum extraction...
                throw new RuntimeException(e);
            }
        }, "(string->class V) : Gets the given class, or null if unable.");
        ctx.defLib("instance?", MVMType.BOOL, MVMType.typeOfClass(Class.class), MVMType.ANY, (a0, a1) -> {
            return ((Class<?>) a0).isInstance(a1);
        }, "(instance? C V) : Class.isInstance(V)");
        // Custom: Macro facilities
        // This should be roughly compatible with the Scheme 9 from Empty Space implementation.
        ctx.defineSlot(sym("define-syntax"), new Macroify())
            .help("(define-syntax (NAME ARG... [. VA]) PROC) : Defines a macro, given tree elements and assembles the replacement tree element. Only really makes sense at top-level.");
        // Custom: Debug
        ctx.defLib("mvm-disasm", MVMType.ANY, MVMType.ANY, (a0) -> {
            if (a0 instanceof MVMCExpr)
                return ((MVMCExpr) a0).disasm();
            if (a0 instanceof MVMLambdaFn) {
                MVMLambdaFn l = (MVMLambdaFn) a0;
                return MVMU.l(sym("λi"), l.rootFrame.isExpectedToExist(), l.content.disasm());
            }
            throw new RuntimeException("Can't disassemble " + MVMU.userStr(a0));
        }, "(mvm-disasm LAMBDA) : Disassembles the given lambda.");
        ctx.defLib("mvm-typeof", MVMType.STR, MVMType.SYM, (a0) -> {
            return ctx.getSlot((DatumSymbol) a0).type.toString();
        }, "(mvm-typeof X) : Gets type of slot symbol X as a string.");
        ctx.defineSlot(sym("help"), new Help())
            .help("(help [TOPIC]) : Helpful information on the given value (if any), or lists helpable symbols in the root context.\nUsed to list all symbols, then crashed.");
        ctx.defineSlot(sym("help-set!"), new HelpSet())
            .help("(help-set! TOPIC VALUE) : Sets information on the given value.");
        ctx.defineSlot(sym("cast"), new Cast()).help("(cast [TYPE] V) : Casts V to TYPE, or otherwise the 'any' type.");
    }

    public static final class Macroify extends MVMMacro {
        public Macroify() {
            super("define-syntax");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            return MVMScopingLibrary.compileFnDefine(cs, call, (head, args) -> {
                final MVMCExpr fnx = MVMScopingLibrary.lambda(head.toString(), cs, args, call, 1);
                return new MVMCMacroify(fnx);
            });
        }
    }

    public static final class Help extends MVMMacro {
        public Help() {
            super("help");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length == 0) {
                LinkedList<DatumSymbol> ds = new LinkedList<>();
                for (MVMSlot s : cs.context.listSlots())
                    if (s.help != null)
                        ds.add(s.s);
                return new MVMCExpr.Const(ds, new MVMType.TypedList(MVMType.SYM));
            } else if (call.length == 1) {
                MVMSlot ds = cs.context.getSlot(new DatumSymbol(MVMU.coerceToString(call[1])));
                if (ds != null)
                    return new MVMCExpr.Const(ds.help, MVMType.STR);
                return new MVMCExpr.Const(null, MVMType.STR);
            } else {
                throw new RuntimeException("(help) must be (help) or (help symbol)");
            }
        }
    }

    public static final class HelpSet extends MVMMacro {
        public HelpSet() {
            super("help-set!");
        }
        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 2)
                throw new RuntimeException("(help-set! SYMBOL HELP)");
            DatumSymbol sym = (DatumSymbol) call[0];
            MVMSlot ensuredSlot = cs.context.ensureSlot(sym);
            MVMCExpr helpInfo = cs.compile(call[1]);
            helpInfo.returnType.assertCanImplicitlyCastTo(MVMType.STR, "help-set!");
            return new MVMCExpr(MVMType.STR) {
                @Override
                public Object execute(MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    String res = (String) helpInfo.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    ensuredSlot.help = res;
                    return res;
                }
                @Override
                public Object disasm() {
                    return sym("help-set");
                }
            };
        }
    }

    public static final class Cast extends MVMMacro {
        public Cast() {
            super("cast");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            MVMCExpr res;
            MVMType type;
            if (call.length == 1) {
                type = MVMType.ANY;
                res = cs.compile(call[0]);
            } else if (call.length == 2) {
                type = cs.context.getType(call[0], "cast-type");
                res = cs.compile(call[1]);
            } else {
                throw new RuntimeException("cast expected only one or two args");
            }
            return new MVMCExpr(type) {
                @Override
                public Object execute(MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6,
                        Object l7) {
                    return res.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                }
                
                @Override
                public Object disasm() {
                    return MVMU.l(sym("cast"), type.toString(), res.disasm());
                }
            };
        }
    }

    public static final class DefineType extends MVMMacro {
        public DefineType() {
            super("define-type");
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 2)
                throw new RuntimeException("define-type NAME TYPE");
            DatumSymbol name = (DatumSymbol) call[0];
            cs.context.defineType(name, cs.context.getType(call[1], name.id));
            return null;
        }
    }
}
