/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import r48.io.data.IRIO;

/**
 * Helps cache tileset objects to reduce disk thrashing
 *  (this gets important for large 2k/2k3 games)
 * Created on 14th February 2018.
 */
public class RTilesetCacheHelper {
    private IRIO lastTileset = null;
    private long lastTsId = -1;
    private int lastMapId = -1;
    private final String tilesetStorage;

    public RTilesetCacheHelper(String s) {
        tilesetStorage = s;
    }

    public void updateMapId(int m) {
        if (lastMapId != m)
            lastTileset = null;
        lastMapId = m;
    }

    public IRIO receivedChanged(String changed, long currentTsId) {
        if (lastTsId != currentTsId)
            lastTileset = null;
        if (changed == null) {
            lastTileset = null;
        } else if (changed.equals(tilesetStorage)) {
            lastTileset = null;
        }
        return lastTileset;
    }

    public void insertTileset(long id, IRIO ts) {
        if (lastTileset == null) {
            lastTileset = ts;
            lastTsId = id;
        }
    }
}
