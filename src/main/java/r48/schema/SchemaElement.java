/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.FontSizes;
import r48.RubyIO;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Acts as a bridge so that backbone code can be switched to IRIOs.
 * (Oh dear it had to be merged to prevent typing issues systemwide)
 * Created on November 21, 2018.
 */
public abstract class SchemaElement {
    // For lack of a better place.
    public static boolean checkType(IRIO tgt, int t, String objType, boolean setDefault) {
        if (tgt.getType() != t)
            return true;
        if (objType != null)
            if (!tgt.getSymbol().equals(objType))
                return true;
        return setDefault;
    }
    public static boolean ensureType(RubyIO tgt, int t, boolean setDefault) {
        if (tgt.type != t) {
            tgt.setNull();
            tgt.type = t;
            return true;
        }
        return setDefault;
    }

    public abstract UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path);

    public abstract void modifyVal(RubyIO target, SchemaPath path, boolean setDefault);

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
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        if (target instanceof RubyIO) {
            return buildHoldingEditor((RubyIO) target, launcher, path);
        } else {
            return new UILabel("DO NOT TRANSLATE; COULDN'T MODIFY VALUE, INVOLVED SCHEMA ELEMENT INVOLVED IRIOS\nSUBSYSTEM: " + getClass() + " @ " + this, FontSizes.schemaFieldTextHeight);
        }
    }

    // Modify target to approach the default value, or to correct errors.
    // The type starts as 0 (not '0', but actual numeric 0) and needs to be modified by something to result in a valid object.
    // Rules in general are documented on buildHoldingEditor.
    // -- Additional notes as of NYE
    // "Primary" types will completely wipe the slate if they're invalid.
    // This means any "annotations" (IVars) will be destroyed, so ensure those are *after* the primary in an aggregate.
    // Hopefully this situation should never affect anything.
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (target instanceof RubyIO) {
            modifyVal((RubyIO) target, path, setDefault);
        } else {
            System.err.println("Couldn't modify value in " + path + " with " + this + " ; Involved schema element involved IRIOs");
        }
    }
}
