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
    void trackingMarkClean();

    /**
     * This saves the state of the IDM3Data.
     * Running the Runnable will set the IDM3Data to this exact state.
     * See datamodel.md for specific requirements because this gets LONG.
     */
    @NonNull Runnable saveState();
}
