/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.IGrInDriver;
import r48.map.tiles.ITileRenderer;
import r48.RubyIO;
import r48.map.StuffRenderer;

/**
 * Tables, now with a tileset background!
 * Created on 1/4/17.
 */
public class TilesetTableSchemaElement extends RubyTableSchemaElement<StuffRenderer> {
    public TilesetTableSchemaElement(String iVar, String wVar, String hVar, int dw, int dh, int defL) {
        super(iVar, wVar, hVar, dw, dh, defL);
    }

    @Override
    public StuffRenderer baseTileDraw(RubyIO target, int t, int x, int y, IGrInDriver igd, StuffRenderer osr) {
        // The whole "variable in, variable out" thing is a safe leak-proof way of caching the helper object.
        if (osr == null)
            osr = new StuffRenderer(target, "");
        osr.tileRenderer.drawTile(0, (short) t, x, y, igd, osr.tileRenderer.getTileSize());
        return osr;
    }
}
