/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map2d.tiles;

import r48.gameinfo.ATDB;

/**
 * Created on December 29, 2018.
 */
public final class TileEditingTab {
    /**
     * This tab isn't really supposed to be valid in this layer.
     */
    public static final int DONOTUSE = 1;
    /**
     * Do not process AT fields by default.
     * This should generally be reserved for 'explicit AT write' fields or things known to be put into ATs as 'decoration'.
     */
    public static final int AT_NOPROC = 2;
    /**
     * Tiles represent AT groups.
     */
    public static final int ATGROUP = 4;

    public final String localizedText;
    public final int flags;
    public final int[] visTilesNormal;
    public final int[] visTilesHover;
    public final int[] actTiles;

    public TileEditingTab(String text, int flags, int[] type) {
        this.flags = flags;
        localizedText = text;
        actTiles = type;
        visTilesNormal = type;
        visTilesHover = type;
    }

    public TileEditingTab(ATDB[] appAutoTiles, String text, int flags, int[] typea, AutoTileTypeField[] attf) {
        int[] typeb = new int[typea.length];
        int[] typec = new int[typea.length];
        for (int i = 0; i < typea.length; i++) {
            int t = typea[i];
            typeb[i] = t;
            typec[i] = t;
            for (AutoTileTypeField at : attf) {
                if (t >= at.start) {
                    if (t < (at.length + at.start)) {
                        typeb[i] += at.represent;
                        typec[i] += appAutoTiles[at.databaseId].inverseMap[0xFF];
                        break;
                    }
                }
            }
        }
        localizedText = text;
        this.flags = flags;
        actTiles = typea;
        visTilesNormal = typeb;
        visTilesHover = typec;
    }

    public boolean hasFlag(int flag) {
        return (flags & flag) != 0;
    }

    public static int[] range(int low, int count) {
        int[] res = new int[count];
        for (int i = 0; i < count; i++)
            res[i] = low + i;
        return res;
    }

    public boolean compatibleWith(TileEditingTab lTM) {
        if (lTM.actTiles.length != actTiles.length)
            return false;
        for (int i = 0; i < actTiles.length; i++)
            if (actTiles[i] != lTM.actTiles[i])
                return false;
        return true;
    }

    public boolean inActTilesRange(int i) {
        return (i >= 0) && (i < actTiles.length);
    }
}
