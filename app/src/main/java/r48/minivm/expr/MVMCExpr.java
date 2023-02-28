/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import org.eclipse.jdt.annotation.NonNull;

import r48.minivm.MVMContext;

/**
 * MiniVM compiled expression.
 * MiniVM throws around Objects.
 * Created 26th February 2023.
 */
public abstract class MVMCExpr {
    public final boolean isPure;
    public MVMCExpr(boolean pure) {
        isPure = pure;
    }
    public final Object exc(@NonNull MVMContext ctx) {
        return execute(ctx, null, null, null, null, null, null, null, null);
    }
    public final Object exc(@NonNull MVMContext ctx, Object l0) {
        return execute(ctx, l0, null, null, null, null, null, null, null);
    }
    public final Object exc(@NonNull MVMContext ctx, Object l0, Object l1) {
        return execute(ctx, l0, l1, null, null, null, null, null, null);
    }
    public final Object exc(@NonNull MVMContext ctx, Object l0, Object l1, Object l2) {
        return execute(ctx, l0, l1, l2, null, null, null, null, null);
    }
    public final Object exc(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3) {
        return execute(ctx, l0, l1, l2, l3, null, null, null, null);
    }
    public final Object exc(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4) {
        return execute(ctx, l0, l1, l2, l3, l4, null, null, null);
    }
    public final Object exc(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5) {
        return execute(ctx, l0, l1, l2, l3, l4, l5, null, null);
    }
    public final Object exc(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6) {
        return execute(ctx, l0, l1, l2, l3, l4, l5, l6, null);
    }
    public abstract Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7);

    /**
     * Constant.
     */
    public static class Const extends MVMCExpr {
        public final Object value;
        public Const(Object v) {
            super(true);
            value = v;
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return value;
        }
    }

    public static final MVMCExpr getL0 = new MVMCExpr(true) {
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l0;
        }
    };
    public static final MVMCExpr getL1 = new MVMCExpr(true) {
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l1;
        }
    };
    public static final MVMCExpr getL2 = new MVMCExpr(true) {
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l2;
        }
    };
    public static final MVMCExpr getL3 = new MVMCExpr(true) {
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l3;
        }
    };
    public static final MVMCExpr getL4 = new MVMCExpr(true) {
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l4;
        }
    };
    public static final MVMCExpr getL5 = new MVMCExpr(true) {
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l5;
        }
    };
    public static final MVMCExpr getL6 = new MVMCExpr(true) {
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l6;
        }
    };
    public static final MVMCExpr getL7 = new MVMCExpr(true) {
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return l7;
        }
    };
    public static final MVMCExpr[] getL = {getL0, getL1, getL2, getL3, getL4, getL5, getL6, getL7};

    public static MVMCExpr setL(int index, MVMCExpr value, MVMCExpr ret) {
        switch (index) {
        case 0:
            return new Set0(value, ret);
        case 1:
            return new Set1(value, ret);
        case 2:
            return new Set2(value, ret);
        case 3:
            return new Set3(value, ret);
        case 4:
            return new Set4(value, ret);
        case 5:
            return new Set5(value, ret);
        case 6:
            return new Set6(value, ret);
        case 7:
            return new Set7(value, ret);
        }
        throw new RuntimeException("Invalid local index " + index);
    }
    private static abstract class Set extends MVMCExpr {
        protected final MVMCExpr value, ret;
        Set(MVMCExpr value, MVMCExpr ret) {
            super(value.isPure && ret.isPure);
            this.value = value;
            this.ret = ret;
        }
    }
    private static final class Set0 extends Set {
        public Set0(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l0 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
    }
    private static final class Set1 extends Set {
        public Set1(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l1 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
    }
    private static final class Set2 extends Set {
        public Set2(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l2 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
    }
    private static final class Set3 extends Set {
        public Set3(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l3 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
    }
    private static final class Set4 extends Set {
        public Set4(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l4 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
    }
    private static final class Set5 extends Set {
        public Set5(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l5 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
    }
    private static final class Set6 extends Set {
        public Set6(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l6 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
    }
    private static final class Set7 extends Set {
        public Set7(MVMCExpr value, MVMCExpr ret) {
            super(value, ret);
        }
        @Override
        public Object execute(@NonNull MVMContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            l7 = value.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
            return ret.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        }
    }
}
