/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNull;

import gabien.uslx.append.Block;

/**
 * Data context.
 * This is for future DM3 stuff but was introduced in dummy form in v1.6-X to make it easier to share code between the branches. 
 * Created 7th May, 2024.
 */
public abstract class DMChangeTracker {

    private final AtomicInteger licensesToUnpackData = new AtomicInteger();

    /**
     * The one and only License To Unpack Data.
     */
    private final Block theLicenseToUnpackData = () -> {
        licensesToUnpackData.decrementAndGet();
    };

    /**
     * Acquires a 'license to unpack data'.
     * Be CAREFUL with this, as it can violate the assumptions of the undo/redo system!
     * It shuts up data modification notifications.
     */
    public final @NonNull Block openUnpackLicense() {
        licensesToUnpackData.incrementAndGet();
        return theLicenseToUnpackData;
    }

    public int getLicensesToUnpackData() {
        return licensesToUnpackData.get();
    }

    /**
     * Reports that a data object has been created.
     * The data object starts dirty.
     */
    public abstract void register(IDM3Data irioData);

    /**
     * Reports that a data object is about to be modified.
     * This should only fire once ever per IDM3Data unless markClean is called.
     * However, IDM3Contexts should be ready to handle an IDM3Data which calls this function multiple times in a row.
     * The idea here is that the IDM3Context records the state before the first modification.
     * It then marks the undo point when it finds it convenient to do so.
     */
    public abstract void modifying(@NonNull IDM3Data modifiedData);

    /**
     * The "null context" is for cases where DM3 context tracking is not required.
     * USE WITH CARE.
     * These references will need to be audited as things move further into DM3.
     * In practice MagicalBinders use this and that's not a great thing.
     * But it's also used by generics that are going to off-DB storage like clipboard or being deep-copied.
     * The instances are separated by "cause".
     */
    public static final class Null extends DMChangeTracker {
        // something that is cloned from
        public static final DMChangeTracker DISPOSABLE = new Null();
        // clipboard
        public static final DMChangeTracker CLIPBOARD = new Null();
        // AdHocSaveLoad & pals
        public static final DMChangeTracker ADHOC_IO = new Null();
        // key workspace editor
        public static final DMChangeTracker WORKSPACE = new Null();
        // DMKey embedded values
        public static final DMChangeTracker DMKEY_EMBEDDED = new Null();
        // operator config
        public static final DMChangeTracker OPERATOR_CONFIG = new Null();
        // Tests use this
        public static final DMChangeTracker TESTS = new Null();

        private Null() {
        }

        @Override
        public void register(IDM3Data irioData) {
        }

        @Override
        public void modifying(IDM3Data modifiedData) {
        }

        /**
         * Always return a license to unpack data; null change trackers don't need the reports
         */
        @Override
        public int getLicensesToUnpackData() {
            return 1;
        }
    }
}
