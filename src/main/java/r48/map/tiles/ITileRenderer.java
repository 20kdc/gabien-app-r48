/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

import gabien.IGrInDriver;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

/**
 * Used because this changes a LOT
 * Created on 1/27/17.
 */
public interface ITileRenderer {
    int getTileSize();

    void drawTile(int layer, short tidx, int px, int py, IGrInDriver igd, int ets);
    String getPanorama();

    UITileGrid[] createATUIPlanes(UIMapView mv);

    String[] getPlaneNames(int layer);

    int[] indicateATs();
}
