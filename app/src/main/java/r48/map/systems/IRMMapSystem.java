/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import java.util.Comparator;
import java.util.function.Supplier;

import r48.App;
import r48.dbs.ObjectInfo;
import r48.dbs.ObjectRootHandle;
import r48.io.data.IRIO;
import r48.toolsets.utils.RMTranscriptDumper;

/**
 * Membership implies being an RM engine and some of the universal constants associated with that.
 * Created on 10/06/17.
 */
public interface IRMMapSystem {
    /**
     * Expects `@events` containing events with `@pages` containing `@list`.
     * Pages can have null holes in it because R2k.
     */
    RMMapData[] getAllMaps();

    /**
     * The ILoadedObject that covers all getAllCommonEvents elements.
     */
    ObjectRootHandle getCommonEventRoot(); 

    /**
     * Expects `@name` and `@list`. Note these events have to be in order.
     */
    IRIO[] getAllCommonEvents();

    void dumpCustomData(RMTranscriptDumper dumper);

    // This identifies an actual RM map, not a GUM
    class RMMapData extends ObjectInfo {
        public final Supplier<String> nameSupplier;
        public final int id;

        public RMMapData(App app, Supplier<String> n, int i, String iN) {
            super(app, iN);
            nameSupplier = n;
            id = i;
        }

        public String toString() {
            return idName + ": " + getName();
        }

        public static final Comparator<RMMapData> COMPARATOR = (arg0, arg1) -> {
            if (arg0.id < arg1.id)
                return -1;
            if (arg0.id > arg1.id)
                return 1;
            return 0;
        };

        public String getName() {
            return nameSupplier.get();
        }
    }
}
