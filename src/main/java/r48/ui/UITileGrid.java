/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrDriver;
import r48.AppMain;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.map.tiles.AutoTileTypeField;

/**
 * Tile selection, now with more hax for the AutoTile button. (not-a-TM)
 * Created on 12/31/16.
 */
public class UITileGrid extends UIGrid {
    public final int tileStart, layer;
    public final int spriteScale;
    public final StuffRenderer renderer;
    // Used to implement a feature Demetrius recommended: borders around AT field elements.
    public final int borderWidth;

    // If 0, not an AT Group. Otherwise, this is the size of the AT group.
    public final int atGroup;
    public final int[] viewMap;

    // Avoid keeping this tab selected when arriving here from another layer
    public final boolean recommendAvoid;
    private final String toStringRes;

    public UITileGrid(UIMapView mv, int tStart, int tileCount, int aTile, int[] remap, String tiles, boolean doNotUse, int sprScale) {
        this(mv.mapTable.renderer, mv.currentLayer, tStart, tileCount, aTile, remap, tiles, doNotUse, sprScale);
    }

    public UITileGrid(StuffRenderer sr, int l, int tStart, int tileCount, int aTile, int[] remap, String tiles, boolean doNotUse, int sprScale) {
        this(sr, sr.tileRenderer.getTileSize() * sprScale, (aTile != 0) ? sprScale : 0, l, tStart, tileCount, aTile, remap, tiles, doNotUse, sprScale);
    }

    private UITileGrid(StuffRenderer sr, int ets, int bs, int l, int tStart, int tileCount, int aTile, int[] remap, String tiles, boolean doNotUse, int sprScale) {
        super(ets + (bs * 2), ets + (bs * 2), tileCount);
        borderWidth = bs;
        // Padding for readability. As this padding is on the left, it's last to get removed.
        recommendAvoid = doNotUse;
        if (doNotUse) {
            toStringRes = " " + tiles + "<X>";
        } else {
            toStringRes = " " + tiles;
        }
        canMultiSelect = aTile == 0;
        renderer = sr;
        layer = l;
        tileStart = tStart;
        bkgR = 128;
        bkgB = 128;
        atGroup = aTile;
        viewMap = remap;
        spriteScale = sprScale;
    }

    @Override
    public String toString() {
        return toStringRes;
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
    protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
        if (viewMap != null)
            t = viewMap[t];
        t += tileStart;
        if (atGroup != 0) {
            AutoTileTypeField[] attf = renderer.tileRenderer.indicateATs();
            int def = hover ? 0 : atGroup - 1; // Fallback
            for (AutoTileTypeField at : attf) {
                if (t >= at.start) {
                    if (t < (at.length + at.start)) {
                        if (hover) {
                            def = AppMain.autoTiles[at.databaseId].inverseMap[0xFF];
                        } else {
                            def = at.represent;
                        }
                        break;
                    }
                }
            }
            t += def;
        }
        renderer.tileRenderer.drawTile(layer, (short) t, x + borderWidth, y + borderWidth, igd, spriteScale, true);
    }

    public boolean compatibleWith(UITileGrid lTM) {
        if (tileStart != lTM.tileStart)
            return false;
        if (atGroup != lTM.atGroup)
            return false;
        if (viewMap != null) {
            if (lTM.viewMap == null)
                return false;
            if (viewMap.length != lTM.viewMap.length)
                return false;
            for (int i = 0; i < viewMap.length; i++)
                if (viewMap[i] != lTM.viewMap[i])
                    return false;
        }
        return true;
    }
}
