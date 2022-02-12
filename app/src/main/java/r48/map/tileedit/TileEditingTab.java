package r48.map.tileedit;

import r48.AppMain;

/**
 * Created on December 29, 2018.
 */
public final class TileEditingTab {
    public final String localizedText;
    public final boolean atProcessing, doNotUse;
    public final int[] visTilesNormal;
    public final int[] visTilesHover;
    public final int[] actTiles;

    public TileEditingTab(String text, boolean dnu, int[] type) {
        atProcessing = false;
        doNotUse = dnu;
        localizedText = text;
        actTiles = type;
        visTilesNormal = type;
        visTilesHover = type;
    }

    public TileEditingTab(String text, boolean dnu, int[] typea, AutoTileTypeField[] attf) {
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
                        typec[i] += AppMain.autoTiles[at.databaseId].inverseMap[0xFF];
                        break;
                    }
                }
            }
        }
        atProcessing = true;
        doNotUse = dnu;
        localizedText = text;
        actTiles = typea;
        visTilesNormal = typeb;
        visTilesHover = typec;
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
}
