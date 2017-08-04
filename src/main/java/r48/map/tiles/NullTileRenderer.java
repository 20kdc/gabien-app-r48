/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

import gabien.IGrDriver;
import r48.dbs.TXDB;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

/**
 * Created on 1/27/17.
 */
public class NullTileRenderer implements ITileRenderer {
    @Override
    public int getTileSize() {
        return 32;
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int ets) {

    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        return new UITileGrid[] {
                new UITileGrid(mv, 0, 1, 0, null)
        };
    }

    @Override
    public String[] getPlaneNames(int layer) {
        return new String[] {
                TXDB.get("Tiles Unsupported")
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        return new AutoTileTypeField[0];
    }

    @Override
    public int getFrame() {
        return 0;
    }

    @Override
    public int getRecommendedWidth() {
        return 8;
    }
}
