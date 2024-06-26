/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.compiler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.expr.MVMCLocal;

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

    protected MVMCompileFrame() {
        // Rejoice! The root scope doesn't have a frame ID these days, so ID 0 is usable.
        frameID = 0;
    }

    protected MVMCompileFrame(MVMCompileFrame par) {
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
    public MVMCLocal allocateLocal(@NonNull MVMType type) {
        markExpectedToExist();
        return new MVMCLocal(frameID, allocatedLocals++, type);
    }

    /**
     * Allocates a scope if necessary. Meant to go well with MVMSubScope.getFrameIfOwned.
     */
    public static MVMScope wrapRuntimeScope(@Nullable MVMCompileFrame frame, MVMScope scope) {
        if (frame == null)
            return scope;
        return frame.expectedToExist ? new MVMScope(scope, frame.frameID, frame.allocatedLocals) : scope;
    }

    /**
     * If true, this frame is expected to really exist.
     * This occurs if there are either locals in it or sub-frames exist.
     * In the former case, in the latter case it's because otherwise frame IDs are upset. 
     */
    public void markExpectedToExist() {
        expectedToExist = true;
    }

    /**
     * Used for debugging more than anything else. See the wrap* functions.
     */
    public boolean isExpectedToExist() {
        return expectedToExist;
    }
}
