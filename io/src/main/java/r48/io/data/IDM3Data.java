/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Data with savestates.
 * Created 7th May, 2024.
 */
public interface IDM3Data {
    /**
     * This indicates that the IDM3Context would like to be notified about the next update to this object.
     * All IDM3Data objects start clean by default.
     */
    void dm3MarkClean();

    /**
     * This saves the state of the IDM3Data.
     * Running the Runnable will set the IDM3Data to this exact state.
     * The following guarantees MUST be followed:
     * 1. Saving states of all IDM3Datas in a context at a given time will create a total snapshot of the data in that context.
     * 2. In practice, snapshots will only contain those IDM3Data objects that reported modifications. This must work.
     * 3. All involved IRIO objects that were valid at the time the state was taken must be valid and point to the semantically same objects upon reversion to that state.
     *    (This specifically includes if an IRIO becomes invalid via object deletion, but then the deletion is reverted.)
     * 4. IRIOs that were "theoretically accessible" via read-only operations when the state was taken must remain valid even if they didn't exist.
     *    This is to cover an edge case with DM2 unpacking; the user may have dialogs open to objects that still semantically exist.
     * 5. Performing a savestate, reverting to an older savestate, then going to the later savestate, must work properly. (Redo.)
     * 6. The golden rule: Code outside the datamodel should see the data in the IRIOs 'magically' change to pre-modification,
     *     as if the code had been recording modifications and undoing them via the IRIO interface.
     */
    @NonNull Runnable dm3SaveState();
}
