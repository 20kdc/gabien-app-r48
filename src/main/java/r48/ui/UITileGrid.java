/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.IGrInDriver;
import r48.AppMain;
import r48.map.tiles.ITileRenderer;
import r48.map.UIMapView;

/**
 * Tile selection, now with more hax for the AutoTile button. (not-a-TM)
 * Created on 12/31/16.
 */
public class UITileGrid extends UIGrid {
    public final int tileStart;
    public final UIMapView map;
    private final boolean autoTile;
    public UITileGrid(UIMapView mv, int tStart, int tileCount, boolean aTile) {
        super(ITileRenderer.tileSize, tileCount);
        autoTile = aTile;
        map = mv;
        tileStart = tStart;
        bkgR = 255;
        bkgB = 255;
    }

    @Override
    public int getSelected() {
        return super.getSelected() + tileStart;
    }

    @Override
    protected void drawTile(int t, int x, int y, IGrInDriver igd) {
        if (t == tileCount - 1) {
            if (autoTile) {
                igd.blitImage(16, 36, 20, 20, x + 6, y + 6, AppMain.layerTabs);
                return;
            }
        }
        AppMain.stuffRenderer.tileRenderer.drawTile((short) (t + tileStart), x, y, igd, ITileRenderer.tileSize);
    }
}
