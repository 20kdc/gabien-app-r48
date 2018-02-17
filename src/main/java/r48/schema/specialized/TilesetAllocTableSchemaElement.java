/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.IGrDriver;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TSDB;
import r48.map.StuffRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.schema.specialized.tbleditors.ITableCellEditor;
import r48.ui.Art;

/**
 * Tables, now with a tileset background!
 * Created on 1/4/17.
 */
public class TilesetAllocTableSchemaElement extends RubyTableSchemaElement<StuffRenderer> {
    public TSDB allocSource;

    public TilesetAllocTableSchemaElement(TSDB source, String iVar, String wVar, String hVar, int dc, int dw, int dh, int defL, ITableCellEditor itce, int[] defVal) {
        super(iVar, wVar, hVar, dc, dw, dh, defL, itce, defVal);
        allocSource = source;
        allowResize = allocSource.mapping == null;
        allowTextdraw = !allocSource.disableHex;
    }

    @Override
    public int getGridSize() {
        return 32 * FontSizes.getSpriteScale();
    }

    @Override
    public StuffRenderer baseTileDraw(RubyIO target, int t, int x, int y, IGrDriver igd, StuffRenderer osr) {
        if (allocSource.mapping != null)
            if (t > allocSource.mapping.length)
                return osr; // :(

        final RubyIO targV = iVar == null ? target : target.getInstVarBySymbol(iVar);
        final RubyTable targ = new RubyTable(targV.userVal);
        // The whole "variable in, variable out" thing is a safe leak-proof way of caching the helper object.
        if (osr == null)
            osr = AppMain.system.rendererFromTso(target);
        int ts = osr.tileRenderer.getTileSize();
        int sprScale = FontSizes.getSpriteScale();
        int height = 32 * sprScale;
        if (allocSource.mapping != null) {
            osr.tileRenderer.drawTile(0, (short) allocSource.mapping[t], x, y + (height - (ts * sprScale)), igd, sprScale);
        } else {
            osr.tileRenderer.drawTile(0, (short) t, x, y + (height - ts), igd, sprScale);
        }
        for (TSDB.TSPicture tsp : allocSource.pictures) {
            if (!tsp.acceptable.apply(t))
                continue;
            boolean flagValid = (targ.getTiletype(t % targ.width, t / targ.width, 0) & tsp.flag) != 0;
            int rtX = flagValid ? tsp.layertabAX : tsp.layertabIX;
            int rtY = flagValid ? tsp.layertabAY : tsp.layertabIY;
            RMEventGraphicRenderer.flexibleSpriteDraw(rtX, rtY, tsp.w, tsp.h, x + (tsp.x * sprScale), y + (tsp.y * sprScale), tsp.w * sprScale, tsp.h * sprScale, 0, Art.layerTabs, 0, igd);
        }
        return osr;
    }

    @Override
    public short baseFlipBits(short p) {
        int i = p & 0xFFFF;
        i ^= allocSource.xorDoubleclick;
        return (short) i;
    }

}
