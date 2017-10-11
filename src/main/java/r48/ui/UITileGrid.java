/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrInDriver;
import r48.AppMain;
import r48.FontSizes;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.map.tiles.AutoTileTypeField;

/**
 * Tile selection, now with more hax for the AutoTile button. (not-a-TM)
 * Created on 12/31/16.
 */
public class UITileGrid extends UIGrid {
    public final int tileStart, layer;
    public final int spriteScale = FontSizes.getSpriteScale();
    public final StuffRenderer renderer;

    // If 0, not an AT Group. Otherwise, this is the size of the AT group.
    public final int atGroup;
    public final int[] viewMap;

    public UITileGrid(UIMapView mv, int tStart, int tileCount, int aTile, int[] remap) {
        this(mv.renderer, mv.currentLayer, tStart, tileCount, aTile, remap);
    }

    public UITileGrid(StuffRenderer sr, int l, int tStart, int tileCount, int aTile, int[] remap) {
        super(sr.tileRenderer.getTileSize() * FontSizes.getSpriteScale(), sr.tileRenderer.getTileSize() * FontSizes.getSpriteScale(), tileCount);
        canMultiSelect = aTile == 0;
        renderer = sr;
        layer = l;
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
        t += tileStart;
        if (atGroup != 0) {
            AutoTileTypeField[] attf = renderer.tileRenderer.indicateATs();
            int target = hover ? 0xFF : 0;
            int def = hover ? 0 : atGroup - 1; // fallback
            for (AutoTileTypeField at : attf) {
                if (t >= at.start) {
                    if (t < (at.length + at.start)) {
                        def = AppMain.autoTiles[at.databaseId].inverseMap[target];
                        break;
                    }
                }
            }
            t += def;
        }
        renderer.tileRenderer.drawTile(layer, (short) t, x, y, igd, tileSizeW / spriteScale, FontSizes.getSpriteScale());
    }
}
