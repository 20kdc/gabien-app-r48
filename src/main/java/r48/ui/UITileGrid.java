/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.IGrInDriver;
import r48.AppMain;
import r48.map.UIMapView;

/**
 * Tile selection, now with more hax for the AutoTile button. (not-a-TM)
 * Created on 12/31/16.
 */
public class UITileGrid extends UIGrid {
    public final int tileStart;
    public final UIMapView map;

    // If 0, not an AT Group. Otherwise, this is the size of the AT group.
    public final int atGroup;
    public final int[] viewMap;

    public UITileGrid(UIMapView mv, int tStart, int tileCount, int aTile, int[] remap) {
        super(mv.tileSize, tileCount);
        canMultiSelect = aTile == 0;
        map = mv;
        tileStart = tStart;
        bkgR = 255;
        bkgB = 255;
        atGroup = aTile;
        viewMap = remap;
    }

    public int getTCSelected() {
        int r = getSelected();
        if (viewMap != null)
            return viewMap[r] + tileStart;
        return r + tileStart;
    }

    public boolean tryTCSetSelected(int wanted) {
        wanted -= tileStart;
        if (viewMap == null) {
            if (wanted >= 0)
                if (wanted < tileCount) {
                    setSelected(wanted);
                    return true;
                }
            return false;
        }
        int fuzzLen = atGroup;
        if (fuzzLen == 0)
            fuzzLen = 1;
        for (int i = 0; i < viewMap.length; i++) {
            if ((wanted >= viewMap[i]) && (wanted < (viewMap[i] + fuzzLen))) {
                setSelected(i);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void drawTile(int t, boolean hover, int x, int y, IGrInDriver igd) {
        if (viewMap != null)
            t = viewMap[t];
        if (atGroup != 0)
            if (!hover)
                t += 15; // Hardcoded offset. Not good, but it covers all cases right now.
        AppMain.stuffRenderer.tileRenderer.drawTile(map.currentLayer, (short) (t + tileStart), x, y, igd, tileSize);
    }
}
