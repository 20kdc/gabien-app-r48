/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.tiles;

import gabien.atlas.AtlasDrawable;
import gabien.atlas.SimpleAtlasBuilder;
import gabien.render.IGrDriver;
import gabien.render.ITexRegion;
import r48.gameinfo.ATDB;

/**
 * Created 22nd July 2023.
 */
public class ATFieldAtlasDrawable extends AtlasDrawable {
    public final ITexRegion source;
    public final ATDB.Autotile details;
    public final int tileSize;
    // This is awful, really...
    public final boolean vxaAdjust;

    public ATFieldAtlasDrawable(int tileSize, ITexRegion src, ATDB.Autotile at, boolean vxa) {
        super(tileSize, tileSize);
        this.tileSize = tileSize;
        details = at;
        source = src;
        this.vxaAdjust = vxa;
    }

    public static ITexRegion[] addToSimpleAtlasBuilder(int tileSize, ATDB db, SimpleAtlasBuilder sab, ITexRegion src, boolean vxa) {
        ITexRegion[] out = new ITexRegion[db.entries.length];
        for (int i = 0; i < db.entries.length; i++) {
            final int fi = i;
            ATDB.Autotile entry = db.entries[i];
            if (entry != null)
                sab.add((res) -> out[fi] = res, new ATFieldAtlasDrawable(tileSize, src, entry, vxa));
        }
        return out;
    }

    @Override
    public void drawTo(IGrDriver ap, int px, int py) {
        int cSize = tileSize / 2;
        for (int sA = 0; sA < 2; sA++)
            for (int sB = 0; sB < 2; sB++) {
                int ti = details.corners[sA + (sB * 2)];
                int tx = ti % 3;
                int ty = ti / 3;
                if (vxaAdjust) {
                    // So this is a legacy decision that survives to this day.
                    // VXA tables were based off of RXP tables despite not really being the same format.
                    // To make that actually work, these maths had to be implemented.
                    // They subtly adjust the tile positions to "compress" them into the 2x3 space.
                    if (ti == 2) {
                        tx = tileSize;
                    } else if (ti > 2) {
                        ty -= tileSize;
                        tx /= 2;
                        ty /= 2;
                        ty += tileSize;
                    }
                }
                int sX = (sA * cSize);
                int sY = (sB * cSize);
                ap.blitImage((tx * tileSize) + sX, (ty * tileSize) + sY, cSize, cSize, px + sX, py + sY, source, IGrDriver.BLEND_NONE, 0);
            }
    }
}
