package r48.map.tiles;

import gabien.IGrDriver;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

/**
 * Created on May 11th 2018.
 */
public class IndirectTileRenderer implements ITileRenderer {
    public final RubyTable indirection;
    public final ITileRenderer inner;

    public IndirectTileRenderer(RubyTable indirect, ITileRenderer in) {
        indirection = indirect;
        inner = in;
    }

    @Override
    public int getTileSize() {
        return inner.getTileSize();
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int spriteScale) {
        int tx = (tidx & 0xFFFF) % indirection.width;
        int ty = (tidx & 0xFFFF) / indirection.width;
        if (indirection.outOfBounds(tx, ty))
            return;
        tidx = indirection.getTiletype(tx, ty, 0);
        inner.drawTile(layer, tidx, px, py, igd, spriteScale);
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv, int sprScale) {
        return new UITileGrid[] {
                new UITileGrid(mv, 0, indirection.width * indirection.height, 0, null, TXDB.get("Tiles"), sprScale)
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
        return indirection.width;
    }
}
