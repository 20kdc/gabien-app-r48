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
    public final boolean autoTile;
    public UITileGrid(UIMapView mv, int tStart, int tileCount, boolean aTile) {
        super(mv.tileSize, tileCount);
        autoTile = aTile;
        map = mv;
        tileStart = tStart;
        bkgR = 255;
        bkgB = 255;
    }

    @Override
    public int getSelected() {
        int r = super.getSelected();
        if (autoTile)
            r -= r / 49;
        return r + tileStart;
    }

    private boolean isAutoTile(int t) {
        if (!autoTile)
            return false;
        return (t % 49) == 48;
    }

    @Override
    protected void drawTile(int t, int x, int y, IGrInDriver igd) {
        if (isAutoTile(t)) {
            igd.blitImage(16, 36, 20, 20, x + 6, y + 6, AppMain.layerTabs);
            return;
        }
        if (autoTile)
            t -= t / 49;
        AppMain.stuffRenderer.tileRenderer.drawTile(map.getCurrentLayer(), (short) (t + tileStart), x, y, igd, AppMain.stuffRenderer.tileRenderer.getTileSize());
    }

    public boolean selectedATB() {
        return isAutoTile(super.getSelected());
    }
}
