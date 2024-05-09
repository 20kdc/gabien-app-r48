/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.io.data;

/**
 * Convenience superclass of IRIOs that are also IDM3Data.
 * State saved via cloning.
 * (The distinction exists to allow "independent data" used by IRIOs.) 
 * Created 9th May, 2024.
 */
public abstract class IRIOData extends IRIO implements IDM3Data, Cloneable {
    private boolean clean = true;

    public IRIOData(DMContext context) {
        super(context);
    }

    @Override
    public void trackingMarkClean() {
        clean = true;
    }

    /**
     * Be sure to call this before you change any fields that matter for state.
     */
    public void trackingWillChange() {
        if (clean) {
            if (context.changes.getLicensesToUnpackData() != 0)
                return;
            clean = false;
            context.changes.modifying(this);
        }
    }
}
