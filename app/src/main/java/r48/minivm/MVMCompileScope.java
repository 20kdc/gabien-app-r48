/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import gabien.datum.DatumSymbol;
import gabien.uslx.append.ISupplier;
import r48.minivm.MVMEnvironment.Slot;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCScopeFrame;

/**
 * The compiler lives here!
 * Created 28th February 2023.
 */
public abstract class MVMCompileScope {
    public final MVMEnvironment context;
    // Changes as stuff is added to the scope. Locals that haven't been defined "yet" compile-time aren't in here yet.
    protected final HashMap<DatumSymbol, Local> locals;
    protected final boolean[] fastLocalsAlloc = new boolean[8];

    public MVMCompileScope(MVMEnvironment ctx) {
        context = ctx;
        locals = new HashMap<>();
    }
    public MVMCompileScope(MVMCompileScope cs) {
        context = cs.context;
        locals = new HashMap<>(cs.locals);
        System.arraycopy(cs.fastLocalsAlloc, 0, fastLocalsAlloc, 0, 8);
    }

    /**
     * Compiles a define in this scope.
     * Note the supplier. The define/local must be in place before the expression is given.
     */
    public abstract MVMCExpr compileDefine(DatumSymbol sym, ISupplier<MVMCExpr> value);

    /**
     * Extends with a formal frame boundary.
     * This means you're responsible for frame.wrapRoot!
     */
    public abstract MVMSubScope extendWithFrame();

    /**
     * Extends in a chill manner.
     */
    public abstract MVMCompileScope extendNoFrame();

    /**
     * Just assume a fast local comes from somewhere.
     */
    public void forceFastLocal(DatumSymbol aSym, int i) {
        locals.put(aSym, new Local(null, i));
    }

    /**
     * Compiles an object.
     */
    public final MVMCExpr compile(Object o) {
        if (o instanceof DatumSymbol) {
            // Local
            Local lcl = locals.get(o);
            if (lcl != null)
                return lcl.getter();
            // Context
            Slot s = context.getSlot((DatumSymbol) o);
            if (s != null)
                return s;
            throw new RuntimeException("Undefined symbol: " + o);
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            Object[] oa = ((List<Object>) o).toArray();
            // Tradition states this is AOK, shush...
            if (oa.length == 0)
                return new MVMCExpr.Const(oa);
            // Call of some kind.
            // What we have to do here is compile the first value, and then retroactively work out if it's a macro.
            MVMCExpr oa1v = compile(oa[0]);
            if (oa1v instanceof Slot) {
                Object sv = ((Slot) oa1v).v;
                if (sv instanceof MVMMacro) {
                    // Macro compile tiiiiimmmeeeee
                    MVMCExpr macroRes = ((MVMMacro) sv).compile(this, oa);
                    if (macroRes == null)
                        return new MVMCExpr.Const(null);
                    return macroRes;
                }
            }
            final MVMCExpr[] exprs = new MVMCExpr[oa.length - 1];
            for (int i = 0; i < exprs.length; i++)
                exprs[i] = compile(oa[i + 1]);
            return MVMFnCallCompiler.compile(this, oa1v, exprs);
        } else {
            return new MVMCExpr.Const(o);
        }
    }

    /**
     * A "physical frame".
     */
    public static final class Frame {
        /**
         * The actual, real frame ID as supplied to MVMScope functions.
         */
        public final int frameID;

        /**
         * Amount of allocated locals.
         */
        private int allocatedLocals;

        /**
         * If child frames exist.
         */
        private boolean hasChildren;

        protected Frame() {
            // The first frame ID is always 1, because 0 is reserved for the true (empty) root scope.
            frameID = 1;
        }

        protected Frame(Frame par) {
            frameID = par.frameID + 1;
            par.hasChildren = true;
        }

        /**
         * Allocates a local.
         */
        public Local allocateLocal() {
            return new Local(this, allocatedLocals++);
        }

        /**
         * Ensures that, assuming the given expression is a root, the frame exists.
         */
        public MVMCExpr wrapRoot(MVMCExpr base) {
            return isExpectedToReallyExist() ? new MVMCScopeFrame(base, allocatedLocals) : base;
        }

        /**
         * If true, this frame is expected to really exist.
         * This occurs if there are either locals in it or sub-frames exist.
         * In the former case, in the latter case it's because otherwise frame IDs are upset. 
         */
        public boolean isExpectedToReallyExist() {
            return hasChildren && (allocatedLocals > 0);
        }
    }

    public static final class Local {
        // Frame. If null, this is a fast local.
        public final Frame parent;
        // Local ID 
        public final int localID;
        public Local(Frame f, int id) {
            parent = f;
            localID = id;
        }
        public MVMCExpr getter() {
            if (parent == null)
                return MVMCExpr.getL[localID];
            final int parentFrameID = parent.frameID;
            return new MVMCExpr() {
                @Override
                public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    return ctx.get(parentFrameID, localID);
                }
            };
        }
        // setter deliberately omitted, it works differently between the kinds of local
    }
}
