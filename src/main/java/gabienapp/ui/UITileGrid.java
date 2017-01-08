/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.ui;

import gabien.IGrInDriver;
import gabienapp.Application;
import gabienapp.StuffRenderer;
import gabienapp.map.UIMapView;

/**
 * Tile selection, now with more hax for the AutoTile button. (not-a-TM)
 * Created on 12/31/16.
 */
public class UITileGrid extends UIGrid {
    public final int tileStart;
    public final UIMapView map;
    private final boolean autoTile;
    public UITileGrid(UIMapView mv, int tStart, int tileCount, boolean aTile) {
        super(StuffRenderer.tileSize, tileCount);
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
                igd.blitImage(16, 36, 20, 20, x + 6, y + 6, Application.layerTabs);
                return;
            }
        }
        Application.stuffRenderer.drawTile((short) (t + tileStart), x, y, igd, StuffRenderer.tileSize);
    }
}
