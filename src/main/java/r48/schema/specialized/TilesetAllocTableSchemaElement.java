/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.IGrInDriver;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.TSDB;
import r48.map.StuffRenderer;
import r48.schema.specialized.tbleditors.ITableCellEditor;

/**
 * Tables, now with a tileset background!
 * Created on 1/4/17.
 */
public class TilesetAllocTableSchemaElement extends RubyTableSchemaElement<StuffRenderer> {
    public TSDB allocSource;

    public TilesetAllocTableSchemaElement(TSDB source, String iVar, String wVar, String hVar, int dw, int dh, int defL, ITableCellEditor itce, int[] defVal) {
        super(iVar, wVar, hVar, dw, dh, defL, itce, defVal);
        allocSource = source;
    }

    @Override
    public StuffRenderer baseTileDraw(RubyIO target, int t, int x, int y, IGrInDriver igd, StuffRenderer osr) {
        // The whole "variable in, variable out" thing is a safe leak-proof way of caching the helper object.
        if (osr == null)
            osr = AppMain.system.rendererFromTso(target);
        int ts = osr.tileRenderer.getTileSize();
        osr.tileRenderer.drawTile(0, (short) allocSource.mapping[t], x, y + (32 - ts), igd, ts);
        return osr;
    }
}
