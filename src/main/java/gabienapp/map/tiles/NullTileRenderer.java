/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabienapp.map.tiles;

import gabien.IGrInDriver;
import gabienapp.map.UIMapView;
import gabienapp.ui.UITileGrid;

/**
 * Created on 1/27/17.
 */
public class NullTileRenderer implements ITileRenderer {
    @Override
    public void drawTile(short tidx, int px, int py, IGrInDriver igd, int ets) {

    }

    @Override
    public String getPanorama() {
        return "";
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        return new UITileGrid[] {
                new UITileGrid(mv, 0, 1, false)
        };
    }

    @Override
    public String[] getPlaneNames() {
        return new String[] {
                "Tiles Unsupported"
        };
    }
}
