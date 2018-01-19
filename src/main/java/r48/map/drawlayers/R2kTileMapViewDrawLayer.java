/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.map.tiles.ITileRenderer;

/**
 * Created on 09/06/17, made to extend TMVDL on January 18th 2018.
 */
public class R2kTileMapViewDrawLayer extends TileMapViewDrawLayer {
    public final boolean upper;
    public final RubyIO tileset;

    public R2kTileMapViewDrawLayer(RubyTable tbl, ITileRenderer tr, int targLayer, boolean targUpper, RubyIO ts) {
        super(tbl, targLayer, tr);
        upper = targUpper;
        tileset = ts;
    }

    @Override
    public String getName() {
        return FormatSyntax.formatExtended(TXDB.get("Tile L#A ({B=T='upper'/'wall' tileset flags|general})"), new RubyIO().setFX(tileLayer), new RubyIO().setBool(upper));
    }

    @Override
    public boolean shouldDraw(int x, int y, int layer, short tidx) {
        // Work out upper/lower.
        int val = getTileFlags(tidx, tileset);
        // 0x10: Above. 0x20: Wall. I tested a Wall on L1 on ERPG, did not render over player,
        // Wall only acts as implicit upper for L0.
        boolean r = (val & ((layer == 0) ? 0x30 : 0x10)) != 0;
        return r == upper;
    }

    public static int getTileFlags(short tidx, RubyIO tileset) {
        int flags = 0;
        flags |= checkUpperRange(tidx, tileset, 0, 1000, 0, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 1000, 2000, 1, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 2000, 3000, 2, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 3000, 3050, 3, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 3050, 3100, 4, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 3100, 4000, 5, "@lowpass_data");

        flags |= checkUpperRange(tidx, tileset, 4000, 4050, 6, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4050, 4100, 7, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4100, 4150, 8, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4150, 4200, 9, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4200, 4250, 10, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4250, 4300, 11, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4300, 4350, 12, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4350, 4400, 13, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4400, 4450, 14, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4450, 4500, 15, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4500, 4550, 16, "@lowpass_data");
        flags |= checkUpperRange(tidx, tileset, 4550, 4600, 17, "@lowpass_data");
        for (int k = 0; k < 144; k++)
            flags |= checkUpperRange(tidx, tileset, 5000 + k, 5000 + k + 1, 18 + k, "@lowpass_data");
        for (int k = 0; k < 144; k++)
            flags |= checkUpperRange(tidx, tileset, 10000 + k, 10000 + k + 1, k, "@highpass_data");
        return flags;
    }

    private static int checkUpperRange(short tidx, RubyIO tileset, int rangeS, int rangeE, int group, String s) {
        if (tidx >= rangeS)
            if (tidx < rangeE) {
                RubyTable rt = new RubyTable(tileset.getInstVarBySymbol(s).userVal);
                short val = rt.getTiletype(group, 0, 0);
                return val & 0xFFFF;
            }
        return 0;
    }
}
