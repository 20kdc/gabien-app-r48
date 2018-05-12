/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.IGrDriver;
import gabien.ui.Size;
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
    public TilesetTableSchemaElement(String iVar, String wVar, String hVar, int dc, int dw, int dh, int defL, ITableCellEditor itce, int[] defVal) {
        super(iVar, wVar, hVar, dc, dw, dh, defL, itce, defVal);
    }

    @Override
    public StuffRenderer baseInitializeHelper(RubyIO target) {
        return AppMain.system.rendererFromTso(target);
    }

    @Override
    public Size getGridSize(StuffRenderer th) {
        int ts = th.tileRenderer.getTileSize() * FontSizes.getSpriteScale();
        return new Size(ts, ts);
    }

    @Override
    public StuffRenderer baseTileDraw(RubyIO target, int t, int x, int y, IGrDriver igd, StuffRenderer osr) {
        int ts = osr.tileRenderer.getTileSize();
        osr.tileRenderer.drawTile(0, (short) t, x, y + (getGridSize(osr).height - ts), igd, FontSizes.getSpriteScale());
        return osr;
    }
}
