/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.render.IGrDriver;
import gabien.uslx.append.Size;
import r48.R48;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.schema.specialized.tbleditors.ITableCellEditor;

/**
 * Tables, now with a tileset background!
 * Created on 1/4/17.
 */
public class TilesetTableSchemaElement extends RubyTableSchemaElement<StuffRenderer> {
    public TilesetTableSchemaElement(R48 app, PathSyntax iVar, PathSyntax wVar, PathSyntax hVar, int dc, int dw, int dh, int defL, ITableCellEditor itce, int[] defVal) {
        super(app, iVar, wVar, hVar, dc, dw, dh, defL, itce, defVal);
    }

    public StuffRenderer baseInitializeHelper(IRIO target) {
        return app.system.rendererFromTso(target);
    }

    @Override
    public Size getGridSize(StuffRenderer th) {
        int ts = th.tileRenderer.tileSize * app.f.getSpriteScale();
        return new Size(ts, ts);
    }

    public StuffRenderer baseTileDraw(IRIO target, int t, int x, int y, IGrDriver igd, StuffRenderer osr) {
        int ts = osr.tileRenderer.tileSize * app.f.getSpriteScale();
        Size sz = getGridSize(osr);
        int xx = (sz.width - ts) / 2;
        int xy = (sz.height - ts) / 2;
        osr.tileRenderer.drawTile(0, (short) t, x + xx, y + xy, igd, app.f.getSpriteScale());
        return osr;
    }
}
