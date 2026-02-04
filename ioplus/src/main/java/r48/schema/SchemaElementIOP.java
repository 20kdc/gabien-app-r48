/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.util.SchemaPath;

/**
 * Headless schema element.
 * DO NOT EXTEND OUTSIDE OF SchemaElement!
 * Created February 1st, 2026.
 */
public abstract class SchemaElementIOP {

    /**
     * Some elements have an assigned RORIO they're looking for and "really" edit, but they work in the context of a 'wider' object.
     * This function allows those objects to self-report for use by the path trace logic.
     * This also allows those objects to disown their editing status of their parent for that logic.
     * 'target' is the target of the invocation.
     * 'check' is what is actually being checked.
     */
    public boolean declaresSelfEditorOf(RORIO target, RORIO check) {
        return target == check;
    }

    // Modify target to approach the default value, or to correct errors.
    // The type starts as 0 (not '0', but actual numeric 0) and needs to be modified by something to result in a valid object.
    // Rules in general are documented on buildHoldingEditor.
    // -- Additional notes as of NYE
    // "Primary" types will completely wipe the slate if they're invalid.
    // This means any "annotations" (IVars) will be destroyed, so ensure those are *after* the primary in an aggregate.
    // Hopefully this situation should never affect anything.
    public abstract void modifyVal(IRIO target, SchemaPath path, boolean setDefault);

    /**
     * Visits everything.
     * This is to be used in global operations.
     */
    public final void visit(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        if (v.visit(this, target, path))
            visitChildren(target, path, v, detailedPaths);
    }

    /**
     * Visits all sub-paths of this path.
     * This must only have one reference (visit above).
     * detailedPaths controls if the path should be generating newWindow elements and descriptives or not.
     */
    public abstract void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths);

    /**
     * Gets the window title suffix for this schema element.
     * This is done by schema element as it allows specifying custom logic for specific elements.
     * Note that this should NOT generate sub-paths.
     * But going "sideways" (passing the element to parts of an aggregate/etc. in the same object) is fine.
     * It's important to return null on failure.
     */
    public @Nullable String windowTitleSuffix(SchemaPath.Page path) {
        return null;
    }

    /**
     * Overridden by TempDialogSchemaChoice
     */
    public boolean isTempDialog() {
        return false;
    }

    /**
     * Visits each SchemaPath.
     */
    public interface Visitor {
        /**
         * Called from SchemaElement.visit.
         * If this returns true, the children are visited.
         */
        boolean visit(@NonNull SchemaElementIOP element, IRIO target, SchemaPath path);
    }
}
