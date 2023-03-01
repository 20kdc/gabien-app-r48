/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import org.eclipse.jdt.annotation.Nullable;

import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCLocal;
import r48.minivm.expr.MVMCScopeFrame;

/**
 * A "physical frame".
 * Migrated out of MVMSubScope 1st March 2023.
 */
public final class MVMCompileFrame {
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
    private boolean expectedToExist;

    /**
     * Parent frame.
     */
    private @Nullable MVMCompileFrame parent;

    protected MVMCompileFrame() {
        // The first frame ID is always 1, because 0 is reserved for the true (empty) root scope.
        frameID = 1;
    }

    protected MVMCompileFrame(MVMCompileFrame par) {
        parent = par;
        frameID = par.frameID + 1;
    }

    /**
     * Allocates a local.
     */
    public void allocateLocal(MVMCLocal local) {
        markExpectedToExist();
        local.deoptimizeInto(frameID, allocatedLocals++);
    }

    /**
     * Allocates a local.
     */
    public MVMCLocal allocateLocal() {
        markExpectedToExist();
        return new MVMCLocal(frameID, allocatedLocals++);
    }

    /**
     * Ensures that, assuming the given expression is a root, the frame exists.
     */
    public MVMCExpr wrapRoot(MVMCExpr base) {
        return expectedToExist ? new MVMCScopeFrame(base, allocatedLocals) : base;
    }

    /**
     * MVMCScopeFrame but "outside".
     */
    public MVMScope wrapRuntimeScope(MVMScope scope) {
        return expectedToExist ? new MVMScope(scope, allocatedLocals) : scope;
    }

    /**
     * If true, this frame is expected to really exist.
     * This occurs if there are either locals in it or sub-frames exist.
     * In the former case, in the latter case it's because otherwise frame IDs are upset. 
     */
    public void markExpectedToExist() {
        if (parent != null)
            parent.markExpectedToExist();
        expectedToExist = true;
    }

    /**
     * Used for debugging more than anything else. See the wrap* functions.
     */
    public boolean isExpectedToExist() {
        return expectedToExist;
    }
}
