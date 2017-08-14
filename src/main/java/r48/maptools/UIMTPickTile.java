/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.maptools;

import gabien.IGrDriver;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import gabien.ui.UIPanel;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;

/**
 * Actual tool is just a label saying what to do, these callbacks piggy back on it
 * Created on 09/06/17.
 */
public class UIMTPickTile extends UIMTBase implements IMapViewCallbacks {
    public UILabel innerLabel = new UILabel(TXDB.get("Click on a tile to pick it."), FontSizes.dialogWindowTextHeight);
    public final UIMapView map;

    public UIMTPickTile(IMapToolContext m) {
        super(m, true);
        map = m.getMapView();
        changeInner(innerLabel);
    }

    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 0;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {

    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

    }

    @Override
    public void confirmAt(int x, int y, int layer) {
        if (!map.mapTable.outOfBounds(x, y))
            map.pickTileHelper.accept(map.mapTable.getTiletype(x, y, layer));
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return true;
    }
}
