/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

import gabien.IGrDriver;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

/**
 * Used because this changes a LOT
 * Created on 1/27/17.
 */
public interface ITileRenderer {
    int getTileSize();

    void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int ets);

    UITileGrid[] createATUIPlanes(UIMapView mv);

    String[] getPlaneNames(int layer);

    AutoTileTypeField[] indicateATs();

    // Used to sync the map view and playing animations.
    int getFrame();

    // The standardized tilemap width (8 for RXP, 6 for R2k)
    int getRecommendedWidth();
}
