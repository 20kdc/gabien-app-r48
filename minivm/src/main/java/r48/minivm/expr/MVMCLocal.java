/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import static datum.DatumTreeUtils.sym;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;

/**
 * MiniVM local handle.
 * This is mutable as a local may need to be deoptimized.
 * Created 1st March 2023.
 */
public final class MVMCLocal {
    /**
     * Frame ID, or -1 for none (fast local)
     */
    private int frameID = -1;
    /**
     * Local ID. within frame.
     */
    private int localID;
    /**
     * Local's type.
     * This has to be overwritable because the local's type isn't necessarily "perfect" yet
     */
    public @NonNull MVMType type;

    public MVMCLocal(int fl, @NonNull MVMType t) {
        localID = fl;
        type = t;
    }

    public MVMCLocal(int fID, int lID, @NonNull MVMType t) {
        frameID = fID;
        localID = lID;
        type = t;
    }

    public int getFastSlot() {
        return frameID == -1 ? localID : -1;
    }

    public void deoptimizeInto(int fID, int lID) {
        if (frameID != -1)
            throw new RuntimeException("Can't deoptimize twice!");
        if (fID == -1)
            throw new RuntimeException("Can't deoptimize into fast-local!");
        frameID = fID;
        localID = lID;
    }

    public Read read() {
        return new Read();
    }

    public final class Read extends MVMCExpr {
        public Read() {
            super(type);
        }

        @Override
        public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            if (frameID == -1) {
                switch (localID) {
                case 0:
                    return l0;
                case 1:
                    return l1;
                case 2:
                    return l2;
                case 3:
                    return l3;
                case 4:
                    return l4;
                case 5:
                    return l5;
                case 6:
                    return l6;
                case 7:
                    return l7;
                default:
                    throw new RuntimeException("Invalid fast-local ID");
                }
            } else {
                return ctx.get(frameID, localID);
            }
        }

        @Override
        public Object disasm() {
            return MVMU.l(sym("localRead"), frameID, localID);
        }
    }

    public MVMCExpr write(final MVMCExpr val) {
        if (frameID == -1)
            throw new RuntimeException("A local being written cannot be fast. Deoptimize it.");
        val.returnType.assertCanImplicitlyCastTo(type, val);
        final int fID = frameID;
        final int lID = localID;
        return new MVMCExpr(val.returnType) {
            @Override
            public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                Object v = val.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                ctx.set(fID, lID, v);
                return v;
            }

            @Override
            public Object disasm() {
                return Arrays.asList(sym("localWrite"), frameID, localID, val.disasm());
            }
        };
    }

    /**
     * Only works for deoptimized locals!
     */
    public void directWrite(MVMScope scope, Object aV) {
        if (frameID == -1)
            throw new RuntimeException("A local being directly written cannot be fast, check your caller.");
        scope.set(frameID, localID, aV);
    }
}
