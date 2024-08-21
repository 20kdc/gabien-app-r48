/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import org.eclipse.jdt.annotation.NonNull;

import r48.minivm.IMVMTypable;
import r48.minivm.MVMType;

/**
 * MiniVM function bound into a neat little package.
 * Created 28th February 2023.
 */
public abstract class MVMFn extends MVMHelpable implements IMVMTypable {
    public final MVMType.Fn myFunctionType;

    /**
     * Return type set but nothing else (freeform)
     */
    public MVMFn(@NonNull MVMType.Fn fnt, String nh) {
        super(nh);
        myFunctionType = fnt;
    }

    @Override
    public MVMType getMVMType() {
        return myFunctionType;
    }

    protected RuntimeException cError(Exception ex2) {
        RuntimeException ex = new RuntimeException("@" + nameHint, ex2);
        // try to avoid spamming the console please
        ex.setStackTrace(new StackTraceElement[0]);
        return ex;
    }

    public final Object clDirect() {
        try {
            return callDirect();
        } catch (Exception ex) {
            throw cError(ex);
        }
    }
    public final Object clDirect(Object a0) {
        try {
            return callDirect(a0);
        } catch (Exception ex) {
            throw cError(ex);
        }
    }
    public final Object clDirect(Object a0, Object a1) {
        try {
            return callDirect(a0, a1);
        } catch (Exception ex) {
            throw cError(ex);
        }
    }
    public final Object clDirect(Object a0, Object a1, Object a2) {
        try {
            return callDirect(a0, a1, a2);
        } catch (Exception ex) {
            throw cError(ex);
        }
    }
    public final Object clDirect(Object a0, Object a1, Object a2, Object a3) {
        try {
            return callDirect(a0, a1, a2, a3);
        } catch (Exception ex) {
            throw cError(ex);
        }
    }
    /**
     * Indirect call. Arguments array mutably borrowed by the callee for arbitrary shenanigans.
     * But the reference mustn't leak. It's done this way for for-each's sake.
     */
    public final Object clIndirect(Object[] args) {
        try {
            return callIndirect(args);
        } catch (Exception ex) {
            throw cError(ex);
        }
    }

    protected abstract Object callDirect();
    protected abstract Object callDirect(Object a0);
    protected abstract Object callDirect(Object a0, Object a1);
    protected abstract Object callDirect(Object a0, Object a1, Object a2);
    protected abstract Object callDirect(Object a0, Object a1, Object a2, Object a3);
    /**
     * Indirect call. Arguments array mutably borrowed by the callee for arbitrary shenanigans.
     * But the reference mustn't leak. It's done this way for for-each's sake.
     */
    protected abstract Object callIndirect(Object[] args);

    @Override
    public String toString() {
        return "function: " + nameHint;
    }

    /**
     * Always uses callIndirect.
     */
    public static abstract class VATyped extends MVMFn {
        public VATyped(MVMType.Fn type, String nh) {
            super(type, nh);
        }

        public final Object callDirect() {
            return callIndirect(new Object[] {});
        }
        public final Object callDirect(Object a0) {
            return callIndirect(new Object[] {a0});
        }
        public final Object callDirect(Object a0, Object a1) {
            return callIndirect(new Object[] {a0, a1});
        }
        public final Object callDirect(Object a0, Object a1, Object a2) {
            return callIndirect(new Object[] {a0, a1, a2});
        }
        public final Object callDirect(Object a0, Object a1, Object a2, Object a3) {
            return callIndirect(new Object[] {a0, a1, a2, a3});
        }
    }

    /**
     * Always uses callIndirect.
     */
    public static abstract class VA extends VATyped {
        public VA(String nh) {
            super(new MVMType.Fn(MVMType.ANY), nh);
        }
    }

    /**
     * Always uses callDirect.
     */
    public static abstract class Fixed extends MVMFn {
        public Fixed(MVMType.Fn fn, String nh) {
            super(fn, nh);
        }

        public Object callDirect() {
            throw new RuntimeException(this + " supplied no args");
        }
        public Object callDirect(Object a0) {
            throw new RuntimeException(this + " supplied 1 arg");
        }
        public Object callDirect(Object a0, Object a1) {
            throw new RuntimeException(this + " supplied 2 args");
        }
        public Object callDirect(Object a0, Object a1, Object a2) {
            throw new RuntimeException(this + " supplied 3 args");
        }
        public Object callDirect(Object a0, Object a1, Object a2, Object a3) {
            throw new RuntimeException(this + " supplied 4 args");
        }

        @Override
        public final Object callIndirect(Object[] args) {
            switch (args.length) {
            case 0:
                return callDirect();
            case 1:
                return callDirect(args[0]);
            case 2:
                return callDirect(args[0], args[1]);
            case 3:
                return callDirect(args[0], args[1], args[2]);
            case 4:
                return callDirect(args[0], args[1], args[2], args[3]);
            }
            throw new RuntimeException(this + " supplied " + args.length + " args");
        }
    }

    /**
     * For mathematical operations and such.
     */
    public static abstract class ChainOp<V> extends MVMFn {
        public ChainOp(MVMType res, String nh, MVMType participant) {
            super(new MVMType.Fn(res, 1, new MVMType[] {participant}, participant), nh);
        }

        public abstract V checkParticipant(Object v);

        public abstract Object loneOp(V a);
        public abstract V twoOp(V a, V b);

        public final Object callDirect(Object a0) {
            V v0 = checkParticipant(a0);
            return loneOp(v0);
        }
        public final Object callDirect(Object a0, Object a1) {
            V v0 = checkParticipant(a0);
            V v1 = checkParticipant(a1);
            return twoOp(v0, v1);
        }
        public final Object callDirect(Object a0, Object a1, Object a2) {
            V v0 = checkParticipant(a0);
            V v1 = checkParticipant(a1);
            V v2 = checkParticipant(a2);
            return twoOp(twoOp(v0, v1), v2);
        }
        public final Object callDirect(Object a0, Object a1, Object a2, Object a3) {
            V v0 = checkParticipant(a0);
            V v1 = checkParticipant(a1);
            V v2 = checkParticipant(a2);
            V v3 = checkParticipant(a3);
            return twoOp(twoOp(twoOp(v0, v1), v2), v3);
        }

        @Override
        public final Object callIndirect(Object[] args) {
            if (args.length == 0)
                return callDirect();
            V ongoing = checkParticipant(args[0]);
            if (args.length == 1)
                return loneOp(ongoing);
            for (int i = 1; i < args.length; i++)
                ongoing = twoOp(ongoing, checkParticipant(args[i]));
            return ongoing;
        }
    }
}
