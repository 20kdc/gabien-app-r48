/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.render.IGrDriver;
import gabien.uslx.append.Size;
import r48.App;
import r48.RubyTableR;
import r48.dbs.PathSyntax;
import r48.dbs.TSDB;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.schema.specialized.tbleditors.ITableCellEditor;

/**
 * Tables, now with a tileset background!
 * Created on 1/4/17.
 */
public class TilesetAllocTableSchemaElement extends RubyTableSchemaElement<StuffRenderer> {
    public final TSDB allocSource;

    public TilesetAllocTableSchemaElement(App app, TSDB source, PathSyntax iVar, PathSyntax wVar, PathSyntax hVar, int dc, int dw, int dh, int defL, ITableCellEditor itce, int[] defVal) {
        super(app, iVar, wVar, hVar, dc, dw, dh, defL, itce, defVal);
        allocSource = source;
        allowResize = allocSource.mapping == null;
        allowTextdraw = !allocSource.disableHex;
    }

    @Override
    public Size getGridSize(StuffRenderer sr) {
        int tw = sr.tileRenderer.tileSize;
        int th = tw;
        if (allocSource.tileWHSet) {
            tw = allocSource.tileW;
            th = allocSource.tileH;
        }
        int ss = app.f.getSpriteScale();
        return new Size(tw * ss, th * ss);
    }

    @Override
    public StuffRenderer baseInitializeHelper(IRIO target) {
        return app.system.rendererFromTso(target);
    }

    @Override
    public StuffRenderer baseTileDraw(IRIO target, int t, int x, int y, IGrDriver igd, StuffRenderer osr) {
        if (allocSource.mapping != null)
            if (t > allocSource.mapping.length)
                return osr; // :(

        final IRIO targV = iVar == null ? target : iVar.getRW(target);
        final RubyTableR targ = new RubyTableR(targV.getBuffer());
        int sprScale = app.f.getSpriteScale();
        int ts = osr.tileRenderer.tileSize * sprScale;
        Size sz = getGridSize(osr);
        int xx = (sz.width - ts) / 2;
        int xy = (sz.height - ts) / 2;
        int mapped = allocSource.mapping != null ? allocSource.mapping[t] : t;
        osr.tileRenderer.drawTile(0, (short) mapped, x + xx, y + xy, igd, sprScale);
        allocSource.draw(x, y, t, targ.getTiletype(t % targ.width, t / targ.width, 0), sprScale, igd);
        return osr;
    }

    @Override
    public short baseFlipBits(short p) {
        int i = p & 0xFFFF;
        i ^= allocSource.xorDoubleclick;
        return (short) i;
    }

}
