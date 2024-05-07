/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

/**
 * Data context.
 * This is for future DM3 stuff but was introduced in dummy form in v1.6-X to make it easier to share code between the branches. 
 * Created 7th May, 2024.
 */
public interface IDM3Context {
    /**
     * The "null context" is for cases where DM3 context tracking is not required.
     * USE WITH CARE.
     * These references will need to be audited as things move further into DM3.
     * In practice MagicalBinders use this and that's not a great thing.
     * But it's also used by generics that are going to off-DB storage like clipboard or being deep-copied.
     * The instances are separated by "cause".
     */
    public enum Null implements IDM3Context {
        // something that is cloned from
        DISPOSABLE,
        // clipboard
        CLIPBOARD,
        // this needs to be addressed
        DELETE_ME,
        // AdHocSaveLoad & pals
        ADHOC_IO,
        // key workspace editor
        WORKSPACE,
        // DMKey embedded values
        DMKEY_EMBEDDED,
        // Tests use this
        TESTS;
    }
}
