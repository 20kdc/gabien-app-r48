/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import r48.RubyIO;
import r48.toolsets.RMTranscriptDumper;

/**
 * Membership implies being an RM engine and some of the universal constants associated with that.
 * Created on 10/06/17.
 */
public interface IRMMapSystem {
    // Expects @events containing events with @pages containing @list.
    // Pages can have null holes in it because R2k.
    RMMapData[] getAllMaps();

    // Expects @name and @list. Note these have to be in order.
    RubyIO[] getAllCommonEvents();

    void dumpCustomData(RMTranscriptDumper dumper);

    class RMMapData {
        public final String name;
        public final RubyIO map;
        public final int id;
        public final String idName;

        public RMMapData(String n, RubyIO m, int i, String iN) {
            name = n;
            map = m;
            id = i;
            idName = iN;
        }
    }
}
