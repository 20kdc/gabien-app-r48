/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import org.eclipse.jdt.annotation.NonNull;

import gabien.datum.DatumWriter;

import static gabien.datum.DatumTreeUtils.*;

import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;

/**
 * MiniVM compiled expression.
 * MiniVM throws around Objects.
 * Created 26th February 2023.
 */
public abstract class MVMCExpr {
    public final MVMType returnType;
    public MVMCExpr(MVMType type) {
        this.returnType = type;
    }

    public final Object exc(@NonNull MVMScope ctx) {
        return execute(ctx, null, null, null, null, null, null, null, null);
    }
    public final Object exc(@NonNull MVMScope ctx, Object l0) {
        return execute(ctx, l0, null, null, null, null, null, null, null);
    }
    public abstract Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7);
    /**
     * Disassembles the expression for user analysis.
     * This is NOT a safe round-trip.
     */
    public abstract Object disasm();

    @Override
    public String toString() {
        return DatumWriter.objectToString(disasm());
    }

    /**
     * Constant.
     */
    public static class Const extends MVMCExpr {
        public final Object value;
        public Const(Object v, MVMType type) {
            super(type);
            value = v;
        }
        @Override
        public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return value;
        }
        @Override
        public Object disasm() {
            return MVMU.userStr(value);
        }
    }

    // These two manipulate the L0 slot. This is used by "hand-written" code using the VM like PathSyntax.
    public static final MVMCExpr getL0 = new MVMCExpr(MVMType.ANY) {
        @Override
        public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l0;
        }
        @Override
        public Object disasm() {
            return sym("getL0");
        }
    };
    private static abstract class Set extends MVMCExpr {
        protected final MVMCExpr value, ret;
        Set(MVMCExpr value, MVMCExpr ret) {
            super(ret.returnType);
            this.value = value;
            this.ret = ret;
        }
    }
    public static final class Set0 extends Set {
        public Set0(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l0 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
        @Override
        public Object disasm() {
            return MVMU.l(sym("setL0"), value.disasm(), ret.disasm());
        }
    }
}
