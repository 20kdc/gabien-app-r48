/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos;

/**
 * Passed to UICellEditingPanel in order to integrate higher-level tweening functionality.
 * Created on October 10, 2018.
 */
public interface IGenposTweeningManagement {
    // NOTE: This is cached. The cache is flushed at incomingModification.
    KeyTrack propertyKeytrack(int prop);

    boolean propertyKeyed(int prop, KeyTrack track);

    void disablePropertyKey(int prop, KeyTrack track);

    class KeyTrack {
        public boolean[] track;
        public double[] values;
        public boolean shouldFloor;

        public KeyTrack(boolean[] t, double[] v, boolean shouldFlor) {
            track = t;
            values = v;
            shouldFloor = shouldFlor;
        }
    }
}
