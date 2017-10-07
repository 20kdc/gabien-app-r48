/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.IGrInDriver;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.map.StuffRenderer;
import r48.schema.specialized.tbleditors.ITableCellEditor;

/**
 * Tables, now with a tileset background!
 * Created on 1/4/17.
 */
public class TilesetTableSchemaElement extends RubyTableSchemaElement<StuffRenderer> {
    public TilesetTableSchemaElement(String iVar, String wVar, String hVar, int dw, int dh, int defL, ITableCellEditor itce, int[] defVal) {
        super(iVar, wVar, hVar, dw, dh, defL, itce, defVal);
    }

    @Override
    public StuffRenderer baseTileDraw(RubyIO target, int t, int x, int y, IGrInDriver igd, StuffRenderer osr) {
        // The whole "variable in, variable out" thing is a safe leak-proof way of caching the helper object.
        if (osr == null)
            osr = AppMain.system.rendererFromMap(null);
        int ts = osr.tileRenderer.getTileSize();
        osr.tileRenderer.drawTile(0, (short) t, x, y + (32 - ts), igd, ts, FontSizes.getSpriteScale());
        return osr;
    }
}
