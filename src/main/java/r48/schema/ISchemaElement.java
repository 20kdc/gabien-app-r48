/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Used as part of the schema-building framework for object editing.
 * In a way, inspired by a game from Reflexive Entertainment (Rebound : Lost Worlds), of all things.
 * In that game, the editor seemed to use some sort of internal reflection data to allow editing all sorts of game objects.
 * This is a similar case, except we don't have a repository of objects to work with, just a single non-recursive tree.
 * Created on 12/28/16.
 */
public interface ISchemaElement {
    // Creates the editor control.
    // Ground rules:
    // 1. If the control changes the value, path.changeOccurred(false) MUST be called.
    //    However, it must NOT be called if the value does not change.
    //    (This also applies to modifyVal, but true should be passed, not false.)
    //    Ordering is Modify Value/path.changeOccurred/Update UI (just in case)
    // 2. If this element directly represents an Array/Hash,
    //     then the first thing that must be done is
    //     to wrap path with a path.arrayEntry call.
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
    UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path);

    // Maximum textHeight of the element out of buildHoldingEditor.
    // Can throw an error, in which case this should be encapsulated with a SubwindowSchemaElement.
    int maxHoldingHeight();

    // Modify target to approach the default value, or to correct errors.
    // The type starts as 0 (not '0', but actual numeric 0) and needs to be modified by something to result in a valid object.
    // Rules in general are documented on buildHoldingEditor.
    // -- Additional notes as of NYE
    // "Primary" types will completely wipe the slate if they're invalid.
    // This means any "annotations" (IVars) will be destroyed, so ensure those are *after* the primary in an aggregate.
    // Hopefully this situation should never affect anything.
    void modifyVal(RubyIO target, SchemaPath path, boolean setDefault);
}
