/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import r48.io.data.DMChangeTracker;
import r48.io.data.IDM3Data;

/**
 * Allows the time machine to know what it should notify about changes.
 * Created 10th May, 2024.
 */
public abstract class TimeMachineChangeSource extends DMChangeTracker {
    private final TimeMachine tm;

    public TimeMachineChangeSource(TimeMachine machine) {
        tm = machine;
    }

    @Override
    public void modifying(IDM3Data modifiedData) {
        tm.record(modifiedData, this);
    }

    /**
     * The time machine changed something involving this source.
     */
    public abstract void onTimeTravel();
}
