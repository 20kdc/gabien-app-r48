/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabienapp.map.tiles;

import gabien.IGrInDriver;
import gabien.ui.UILabel;
import gabienapp.dbs.ATDB;
import gabienapp.map.UIMapView;
import gabienapp.ui.UITileGrid;

/**
 * Used because this changes a LOT
 * Created on 1/27/17.
 */
public interface ITileRenderer {
    static final int tileSize = 32;
    void drawTile(short tidx, int px, int py, IGrInDriver igd, int ets);
    String getPanorama();

    UITileGrid[] createATUIPlanes(UIMapView mv);

    String[] getPlaneNames();
}
