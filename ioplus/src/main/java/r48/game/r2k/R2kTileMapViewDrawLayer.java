/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.game.r2k;

import r48.ITileAccess;
import r48.RubyTableR;
import r48.io.data.IRIO;
import r48.map2d.layers.TileMapViewDrawLayer;
import r48.map2d.tiles.TileRenderer;
import r48.tr.pages.TrRoot;

/**
 * Created on 09/06/17, made to extend TMVDL on January 18th 2018.
 */
public class R2kTileMapViewDrawLayer extends TileMapViewDrawLayer {
    public final boolean upper;
    public final RubyTableR lowpass;
    public final RubyTableR highpass;

    public R2kTileMapViewDrawLayer(TrRoot t, ITileAccess tbl, TileRenderer tr, int targLayer, boolean targUpper, IRIO ts, String post) {
        super(t, tbl, new int[] {targLayer}, tr, post);
        upper = targUpper;
        lowpass = new RubyTableR(ts.getIVar("@lowpass_data").getBuffer());
        highpass = new RubyTableR(ts.getIVar("@highpass_data").getBuffer());
    }

    @Override
    public boolean shouldDraw(int x, int y, int layer, int tidx) {
        // Work out upper/lower.
        int val = getTileFlags(tidx, lowpass, highpass);
        // 0x10: Above. 0x20: Wall. I tested a Wall on L1 on ERPG, did not render over player,
        // Wall only acts as implicit upper for L0.
        boolean r = (val & ((layer == 0) ? 0x30 : 0x10)) != 0;
        return r == upper;
    }

    public static int getTileFlags(int tidx, RubyTableR lowpass, RubyTableR highpass) {
        int flags = 0;

        flags |= checkUpperRange(tidx, lowpass, 0, 1000, 0);
        flags |= checkUpperRange(tidx, lowpass, 1000, 2000, 1);
        flags |= checkUpperRange(tidx, lowpass, 2000, 3000, 2);
        flags |= checkUpperRange(tidx, lowpass, 3000, 3050, 3);
        flags |= checkUpperRange(tidx, lowpass, 3050, 3100, 4);
        flags |= checkUpperRange(tidx, lowpass, 3100, 4000, 5);

        flags |= checkUpperRange(tidx, lowpass, 4000, 4050, 6);
        flags |= checkUpperRange(tidx, lowpass, 4050, 4100, 7);
        flags |= checkUpperRange(tidx, lowpass, 4100, 4150, 8);
        flags |= checkUpperRange(tidx, lowpass, 4150, 4200, 9);
        flags |= checkUpperRange(tidx, lowpass, 4200, 4250, 10);
        flags |= checkUpperRange(tidx, lowpass, 4250, 4300, 11);
        flags |= checkUpperRange(tidx, lowpass, 4300, 4350, 12);
        flags |= checkUpperRange(tidx, lowpass, 4350, 4400, 13);
        flags |= checkUpperRange(tidx, lowpass, 4400, 4450, 14);
        flags |= checkUpperRange(tidx, lowpass, 4450, 4500, 15);
        flags |= checkUpperRange(tidx, lowpass, 4500, 4550, 16);
        flags |= checkUpperRange(tidx, lowpass, 4550, 4600, 17);
        for (int k = 0; k < 144; k++)
            flags |= checkUpperRange(tidx, lowpass, 5000 + k, 5000 + k + 1, 18 + k);
        for (int k = 0; k < 144; k++)
            flags |= checkUpperRange(tidx, highpass, 10000 + k, 10000 + k + 1, k);
        return flags;
    }

    private static int checkUpperRange(int tidx, RubyTableR tileset, int rangeS, int rangeE, int group) {
        if (tidx >= rangeS)
            if (tidx < rangeE)
                return tileset.getTiletype(group, 0, 0) & 0xFFFF;
        return 0;
    }
}
