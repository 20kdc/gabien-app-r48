/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSymbol;
import r48.minivm.MVMType;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCLocal;

/**
 * Forked from MVMCompileScope for reasons.
 * Created 1st March 2023.
 */
public class MVMSubScope extends MVMCompileScope {
    /**
     * Stack frame layout etc.
     */
    private final MVMCompileFrame frame;
    /**
     * If this instance originated frame.
     */
    private final boolean ownsFrame;
    /**
     * Changes as stuff is added to the scope. Locals that haven't been defined "yet" compile-time aren't in here yet.
     * Beware: if a local is *not* wrapped in an FV barrier, it cannot be "re-wrapped" later.
     * This is because the wrapping wouldn't apply to already compiled code.
     * The "good news" is that in all such cases, it can be "fixed" by forcibly deoptimizing the local.
     */
    protected final HashMap<DatumSymbol, Local> locals;

    public MVMSubScope(MVMToplevelScope tl) {
        super(tl);
        frame = new MVMCompileFrame();
        ownsFrame = true;
        locals = new HashMap<>();
    }
    private MVMSubScope(MVMSubScope cs, boolean hard) {
        super(cs);
        if (hard) {
            locals = new HashMap<>();
            for (Map.Entry<DatumSymbol, Local> entry : cs.locals.entrySet())
                locals.put(entry.getKey(), entry.getValue().wrapFVBarrier());
            frame = new MVMCompileFrame(cs.frame);
            ownsFrame = true;
        } else {
            locals = new HashMap<>(cs.locals);
            frame = cs.frame;
            ownsFrame = false;
        }
    }

    /**
     * Returns the stack frame if and only if this scope owns it.
     */
    public @Nullable MVMCompileFrame getFrameIfOwned() {
        return ownsFrame ? frame : null;
    }

    private int getFreeFastLocalSlot() {
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

    /**
     * Creates a new local, directly.
     */
    public MVMCLocal newLocal(@NonNull DatumSymbol aSym, @NonNull MVMType type) {
        LocalRoot lr = new LocalRoot(type);
        // Overwriting of existing locals dereferences them and frees them up for fast-local allocation.
        // That's fine. (We'll never see those locals again, so fast local reuse is just creative budgeting.)
        // Existing references are also fine because whatever changes those fast locals can't be retroactive.
        locals.put(aSym, lr);
        return lr.local;
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
    public MVMCExpr compileDefine(DatumSymbol sym, Supplier<MVMCExpr> value) {
        LocalRoot local = new LocalRoot(MVMType.ANY);
        locals.put(sym, local);
        MVMCExpr res = value.get();
        local.local.type = res.returnType;
        return local.setter(res);
    }

    @Override
    public MVMCExpr compileDefine(DatumSymbol sym, MVMType type, Supplier<MVMCExpr> value) {
        LocalRoot local = new LocalRoot(type);
        locals.put(sym, local);
        return local.setter(value.get());
    }

    @Override
    public MVMSubScope extendWithFrame() {
        return new MVMSubScope(this, true);
    }

    @Override
    public MVMSubScope extendMayFrame() {
        return new MVMSubScope(this, false);
    }

    /**
     * Compiler local structure.
     * This is more complicated than MVMCLocal because it has to work out fast-local usage.
     * In particular it needs to calculate deoptimization.
     * Another thing is there is a wrapped version of this value for when past barriers that remove FVs.
     */
    private abstract class Local {
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

    private final class LocalRoot extends Local {
        // Local.
        public final MVMCLocal local;
        public LocalRoot(@NonNull MVMType type) {
            // Perform initial allocation
            int fl = getFreeFastLocalSlot();
            if (fl != -1) {
                local = new MVMCLocal(fl, type);
            } else {
                local = frame.allocateLocal(type);
            }
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
