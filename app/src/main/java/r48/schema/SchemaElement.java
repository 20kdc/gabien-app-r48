/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Acts as a bridge so that backbone code can be switched to IRIOs.
 * (Oh dear it had to be merged to prevent typing issues systemwide)
 * Created on November 21, 2018.
 */
public abstract class SchemaElement extends App.Svc {
    public SchemaElement(App app) {
        super(app);
    }

    // For lack of a better place.
    public static boolean checkType(RORIO tgt, int t, String objType, boolean setDefault) {
        if (tgt.getType() != t)
            return true;
        if (objType != null)
            if (!tgt.getSymbol().equals(objType))
                return true;
        return setDefault;
    }

    // Creates the editor control.
    // Ground rules:
    // 1. If the control changes the value, path.changeOccurred(false) MUST be called.
    //    However, it must NOT be called if the value does not change.
    //    (This also applies to modifyVal, but true should be passed, not false.)
    //    Ordering is Modify Value/path.changeOccurred/Update UI (just in case)
    // 2. If this element monitors subelements, use the tagSEMonitor!
    //    (This also applies to modifyVal.)
    // 3. If you do any error checking at all,
    //     make it fail fast.
    //    (This does not apply to modifyVal :
    //      modifyVal's rule is
    //      "Fix if possible, but failing that, nuke it.")
    // 4. Always, ALWAYS give the element being returned the correct vertical size.
    //    Horizontal does not matter.
    //    (Obviously doesn't apply to modifyVal.)
    // 5. When possible, write a generic element that
    //     conveniently covers the other cases you'll find,
    //     which you can put into the SDB language.
    //    Failing that, throw it in specialized.
    // And finally, a reminder.
    // These rules were determined by trial and error over 5 and a half days.
    // Before the system was even *completed.*
    // Probably best not to break them.
    public abstract UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path);

    /**
     * Gets the window title suffix for this schema element.
     * This is done by schema element as it allows specifying custom logic for specific elements.
     * Note that this should NOT generate sub-paths.
     * But going "sideways" (passing the element to parts of an aggregate/etc. in the same object) is fine.
     * It's important to return null on failure.
     */
    public @Nullable String windowTitleSuffix(SchemaPath path) {
        return null;
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
     * Visits each SchemaPath.
     */
    public interface Visitor {
        /**
         * Called from SchemaElement.visit.
         * If this returns true, the children are visited.
         */
        boolean visit(SchemaElement element, IRIO target, SchemaPath path);
    }

    /**
     * Has no sub-paths.
     */
    public static abstract class Leaf extends SchemaElement {
        public Leaf(App app) {
            super(app);
        }

        @Override
        public final void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        }
    }
}
