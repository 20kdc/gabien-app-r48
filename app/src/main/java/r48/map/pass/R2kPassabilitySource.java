/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.pass;

import r48.ITileAccess;
import r48.RubyTableR;
import r48.io.data.IRIO;
import r48.map.drawlayers.R2kTileMapViewDrawLayer;

/**
 * Calculates the passability of a given tile.
 * Note that I checked the "bidirectional" stuff.
 * Basically, a disabled flag acts as the creation of a wall there.
 * You can't make one-way traps *cough*pokemon*cough*.
 * Created on 09/06/17.
 */
public class R2kPassabilitySource implements IPassabilitySource {
    public final ITileAccess mapTable;
    public final RubyTableR tsLow, tsHigh;

    public R2kPassabilitySource(ITileAccess rt, IRIO tileset) {
        mapTable = rt;
        tsLow = new RubyTableR(tileset.getIVar("@lowpass_data").getBuffer());
        tsHigh = new RubyTableR(tileset.getIVar("@highpass_data").getBuffer());
    }

    @Override
    public int getPassability(int x, int y) {
        if (!mapTable.coordAccessible(x, y))
            return -1;

        int f0id = mapTable.getTiletype(x, y, 0);
        int f0 = R2kTileMapViewDrawLayer.getTileFlags(f0id, tsLow, tsHigh);
        int f1 = R2kTileMapViewDrawLayer.getTileFlags(mapTable.getTiletype(x, y, 1), tsLow, tsHigh);

        // Somewhere along the line this might have gotten the wrong way around...
        // I believe it is fixed now. Testing on the green road on which the squid sits, in a diary of dreams, confirms this.
        // ...It was not fixed - left/right was reversed, see #64. Oops.
        boolean down = merge(f0id, f0, f1, 0x01, 0x08, x, y + 1);
        boolean left = merge(f0id, f0, f1, 0x02, 0x04, x - 1, y);
        boolean right = merge(f0id, f0, f1, 0x04, 0x02, x + 1, y);
        boolean up = merge(f0id, f0, f1, 0x08, 0x01, x, y - 1);
        int r = 0;
        if (down)
            r |= PASS_DOWN;
        if (right)
            r |= PASS_RIGHT;
        if (left)
            r |= PASS_LEFT;
        if (up)
            r |= PASS_UP;
        return r;
    }

    private boolean merge(int f0id, int f0, int f1, int flag, int flagInv, int oX, int oY) {
        if (!mapTable.coordAccessible(oX, oY))
            return false;

        int b0id = mapTable.getTiletype(oX, oY, 0);
        int b0 = R2kTileMapViewDrawLayer.getTileFlags(b0id, tsLow, tsHigh);
        int b1 = R2kTileMapViewDrawLayer.getTileFlags(mapTable.getTiletype(oX, oY, 1), tsLow, tsHigh);

        return mergeCore(f0id, f0, f1, flag) & mergeCore(b0id, b0, b1, flagInv);
    }

    private boolean mergeCore(int f0id, int f0, int f1, int flag) {
        // Upper can veto lower
        if ((f1 & flag) == 0)
            return false;
        // And if upper is actually on lower layer it takes precedence
        if ((f1 & 0x10) == 0)
            return true;
        // This applies sometimes, but not all the time.
        // In particular, it applies on a 11e8 2710 in EasyRPG TestGame,
        // attributes decimal 48 31.
        // The implication... is clear, but... why?
        // Still. This is the closest to accurate I can get.
        if ((f0 & 0x30) == 0x30) {
            int n = f0id % 50;
            if ((n >= 20) && (n < 24)) {
                return true;
            } else if ((n >= 33) && (n < 38)) {
                return true;
            } else if ((n == 42) || (n == 43) || (n == 45) || (n == 46)) {
                return true;
            }
        }
        return (f0 & flag) != 0;
    }
}
