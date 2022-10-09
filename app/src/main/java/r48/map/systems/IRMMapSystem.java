/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import java.util.Comparator;

import r48.dbs.ObjectInfo;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.map.systems.IRMMapSystem.RMMapData;
import r48.toolsets.utils.RMTranscriptDumper;

/**
 * Membership implies being an RM engine and some of the universal constants associated with that.
 * Created on 10/06/17.
 */
public interface IRMMapSystem extends IDynobjMapSystem {
    // Expects @events containing events with @pages containing @list.
    // Pages can have null holes in it because R2k.
    RMMapData[] getAllMaps();

    /**
     * The ILoadedObject that covers all getAllCommonEvents elements.
     */
    IObjectBackend.ILoadedObject getCommonEventRoot(); 

    /**
     * Expects @name and @list. Note these have to be in order.
     */
    IRIO[] getAllCommonEvents();

    void dumpCustomData(RMTranscriptDumper dumper);

    // This identifies an actual RM map, not a GUM
    class RMMapData extends ObjectInfo {
        public final String name;
        public final int id;

        public RMMapData(String n, int i, String iN, String sN) {
            super(iN, sN);
            name = n;
            id = i;
        }

        public String toString() {
            return idName + ": " + name;
        }

        public static final Comparator<RMMapData> COMPARATOR = new Comparator<RMMapData>() {
            @Override
            public int compare(RMMapData arg0, RMMapData arg1) {
                if (arg0.id < arg1.id)
                    return -1;
                if (arg0.id > arg1.id)
                    return 1;
                return 0;
            }
        };
    }
}
