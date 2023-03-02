/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.compiler;

import java.util.HashMap;
import java.util.Map;

import gabien.datum.DatumSymbol;
import gabien.uslx.append.ISupplier;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCLocal;

/**
 * Forked from MVMCompileScope for reasons.
 * Created 1st March 2023.
 */
public class MVMSubScope extends MVMCompileScope {
    // Stack frame.
    public final MVMCompileFrame frame;
    // Changes as stuff is added to the scope. Locals that haven't been defined "yet" compile-time aren't in here yet.
    protected final HashMap<DatumSymbol, Local> locals;

    public MVMSubScope(MVMToplevelScope tl) {
        super(tl);
        frame = new MVMCompileFrame();
        locals = new HashMap<>();
    }
    private MVMSubScope(MVMSubScope cs) {
        super(cs);
        locals = new HashMap<>();
        // Wrap locals with the new frame barrier.
        for (Map.Entry<DatumSymbol, Local> entry : cs.locals.entrySet())
            locals.put(entry.getKey(), entry.getValue().wrapFVBarrier());
        frame = new MVMCompileFrame(cs.frame);
    }

    public int getFreeFastLocalSlot() {
        boolean[] slots = new boolean[8];
        for (Local v : locals.values()) {
            int slot = v.occupiesLocalFVSlot();
            if (slot != -1)
                slots[slot] = true;
        }
        for (int i = 0; i < 8; i++)
            if (!slots[i])
                return i;
        return -1;
    }

    public boolean isLocalSlotFree(int f) {
        for (Local v : locals.values())
            if (v.occupiesLocalFVSlot() == f)
                return false;
        return true;
    }

    /**
     * Creates a new local, directly.
     */
    public LocalRoot newLocal(DatumSymbol aSym) {
        LocalRoot lr = new LocalRoot();
        locals.put(aSym, lr);
        return lr;
    }

    @Override
    public MVMCExpr readLookup(DatumSymbol ds) {
        // Local
        Local lcl = locals.get(ds);
        if (lcl != null)
            return lcl.getter(false);
        return super.readLookup(ds);
    }

    @Override
    public MVMCExpr writeLookup(DatumSymbol ds, MVMCExpr compile) {
        // Local
        Local lcl = locals.get(ds);
        if (lcl != null)
            return lcl.setter(compile);
        return super.writeLookup(ds, compile);
    }

    @Override
    public MVMCExpr compileDefine(DatumSymbol sym, ISupplier<MVMCExpr> value) {
        LocalRoot local = new LocalRoot();
        locals.put(sym, local);
        return local.setter(value.get());
    }

    @Override
    public MVMSubScope extendMayFrame() {
        return new MVMSubScope(this);
    }

    @Override
    public MVMSubScope extendWithFrame() {
        return new MVMSubScope(this);
    }

    /**
     * Compiler local structure.
     * This is more complicated than MVMCLocal because it has to work out fast-local usage.
     * In particular it needs to calculate deoptimization.
     * Another thing is there is a wrapped version of this value for when past barriers that remove FVs.
     */
    public abstract class Local {
        /**
         * Returns the local FV slot this occupies.
         * Note that this returns -1 if past an FV barrier (and thus the difference is nil)
         */
        public abstract int occupiesLocalFVSlot();
        /**
         * Creates a version of the local that must always be deoptimized to access.
         * This can be used whenever an FV is overwritten.
         */
        public abstract Local wrapFVBarrier();
        /**
         * Getter. isPastFVBarrier implies that the local can't be accessed via FV, see wrapFVBarrier
         */
        public abstract MVMCExpr getter(boolean isPastFVBarrier);
        /**
         * Setter where the set value must outlive the local.
         * This requires a deoptimize.
         */
        public abstract MVMCExpr setter(MVMCExpr val);
    }

    public final class LocalRoot extends Local {
        // Local.
        public final MVMCLocal local;
        public LocalRoot() {
            // Perform initial allocation
            int fl = getFreeFastLocalSlot();
            if (fl != -1) {
                local = new MVMCLocal(fl);
            } else {
                local = frame.allocateLocal();
            }
        }
        public LocalRoot(int f) {
            assert isLocalSlotFree(f);
            local = new MVMCLocal(f);
        }

        @Override
        public int occupiesLocalFVSlot() {
            return local.getFastSlot();
        }

        @Override
        public Local wrapFVBarrier() {
            return new LocalFVBarrier(this);
        }

        /**
         * Deoptimizes the local.
         */
        public void deoptimize() {
            if (local.getFastSlot() != -1)
                frame.allocateLocal(local);
        }

        @Override
        public MVMCExpr getter(boolean isPastFVBarrier) {
            if (isPastFVBarrier)
                deoptimize();
            return local.read();
        }

        @Override
        public MVMCExpr setter(MVMCExpr val) {
            deoptimize();
            return local.write(val);
        }
    }

    /**
     * Compiler local structure.
     * This is more complicated than MVMCLocal because it has to work out fast-local usage.
     */
    public final class LocalFVBarrier extends Local {
        // Local.
        public final Local base;
        public LocalFVBarrier(Local b) {
            // Perform initial allocation
            base = b;
        }

        @Override
        public int occupiesLocalFVSlot() {
            return -1;
        }

        @Override
        public Local wrapFVBarrier() {
            return this;
        }

        public MVMCExpr getter(boolean isPastFVBarrier) {
            return base.getter(true);
        }

        @Override
        public MVMCExpr setter(MVMCExpr val) {
            return base.setter(val);
        }
    }
}
