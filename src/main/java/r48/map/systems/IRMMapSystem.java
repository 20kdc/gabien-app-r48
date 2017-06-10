/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
