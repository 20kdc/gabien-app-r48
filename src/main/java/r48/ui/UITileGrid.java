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
    public final boolean autoTile;
    public final int autoTileSpacing;

    public UITileGrid(UIMapView mv, int tStart, int tileCount, boolean aTile, int aTileSpacing) {
        super(mv.tileSize, tileCount);
        canMultiSelect = true;
        autoTile = aTile;
        autoTileSpacing = aTileSpacing;
        map = mv;
        tileStart = tStart;
        bkgR = 255;
        bkgB = 255;
    }

    @Override
    public int getSelected() {
        int r = super.getSelected();
        if (autoTile)
            r -= r / (autoTileSpacing + 1);
        return r + tileStart;
    }

    @Override
    public void setSelected(int i) {
        i -= tileStart;
        if (autoTile)
            i += i / autoTileSpacing;
        super.setSelected(i);
    }

    private boolean isAutoTile(int t) {
        if (!autoTile)
            return false;
        return (t % (autoTileSpacing + 1)) == autoTileSpacing;
    }

    @Override
    protected void drawTile(int t, int x, int y, IGrInDriver igd) {
        if (isAutoTile(t)) {
            int sz = 20;
            if (tileSize < sz)
                sz = tileSize;
            int margin = ((tileSize - sz) / 2);
            igd.blitImage(16, 36, sz, sz, x + margin, y + margin, AppMain.layerTabs);
            return;
        }
        if (autoTile)
            t -= t / (autoTileSpacing + 1);
        AppMain.stuffRenderer.tileRenderer.drawTile(map.currentLayer, (short) (t + tileStart), x, y, igd, tileSize);
    }

    public boolean selectedATB() {
        return isAutoTile(super.getSelected());
    }
}
