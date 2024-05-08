/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Data context.
 * This is for future DM3 stuff but was introduced in dummy form in v1.6-X to make it easier to share code between the branches. 
 * Created 7th May, 2024.
 */
public interface IDMChangeTracker {
    /**
     * Reports that a data object is about to be modified.
     * This should only fire once ever per IDM3Data unless markClean is called.
     * However, IDM3Contexts should be ready to handle an IDM3Data which calls this function multiple times in a row.
     * The idea here is that the IDM3Context records the state before the first modification.
     * It then marks the undo point when it finds it convenient to do so.
     */
    void modifying(@NonNull IDM3Data modifiedData);

    /**
     * The "null context" is for cases where DM3 context tracking is not required.
     * USE WITH CARE.
     * These references will need to be audited as things move further into DM3.
     * In practice MagicalBinders use this and that's not a great thing.
     * But it's also used by generics that are going to off-DB storage like clipboard or being deep-copied.
     * The instances are separated by "cause".
     */
    public enum Null implements IDMChangeTracker {
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

        @Override
        public void modifying(IDM3Data modifiedData) {
        }
    }
}
