/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.pass;

import r48.RubyIO;
import r48.RubyTable;
import r48.map.drawlayers.R2kTileMapViewDrawLayer;

/**
 * Calculates the passability of a given tile.
 * Note that I checked the "bidirectional" stuff.
 * Basically, a disabled flag acts as the creation of a wall there.
 * You can't make one-way traps *cough*pokemon*cough*.
 * Created on 09/06/17.
 */
public class R2kPassabilitySource implements IPassabilitySource {
    public final RubyTable mapTable;
    public final RubyIO tileset;
    public final boolean scrollW, scrollH;

    public R2kPassabilitySource(RubyTable rt, RubyIO ts, boolean w, boolean h) {
        mapTable = rt;
        tileset = ts;
        scrollW = w;
        scrollH = h;
    }

    @Override
    public int getPassability(int x, int y) {
        if (mapTable.outOfBounds(x, y))
            return -1;

        short f0id = mapTable.getTiletype(x, y, 0);
        int f0 = R2kTileMapViewDrawLayer.getTileFlags(f0id, tileset);
        int f1 = R2kTileMapViewDrawLayer.getTileFlags(mapTable.getTiletype(x, y, 1), tileset);

        // Somewhere along the line this might have gotten the wrong way around...
        // I believe it is fixed now. Testing on the green road on which the squid sits, in a diary of dreams, confirms this.
        boolean down = merge(f0id, f0, f1, 0x01, 0x08, x, y + 1);
        boolean right = merge(f0id, f0, f1, 0x02, 0x04, x + 1, y);
        boolean left = merge(f0id, f0, f1, 0x04, 0x02, x - 1, y);
        boolean up = merge(f0id, f0, f1, 0x08, 0x01, x, y - 1);
        int r = 0;
        if (down)
            r |= 1;
        if (right)
            r |= 2;
        if (left)
            r |= 4;
        if (up)
            r |= 8;
        return r;
    }

    private boolean merge(int f0id, int f0, int f1, int flag, int flagInv, int oX, int oY) {
        if (scrollW) {
            while (oX < 0)
                oX += mapTable.width;
            oX %= mapTable.width;
        }
        if (scrollH) {
            while (oY < 0)
                oY += mapTable.height;
            oY %= mapTable.height;
        }
        if (mapTable.outOfBounds(oX, oY))
            return false;

        short b0id = mapTable.getTiletype(oX, oY, 0);
        int b0 = R2kTileMapViewDrawLayer.getTileFlags(b0id, tileset);
        int b1 = R2kTileMapViewDrawLayer.getTileFlags(mapTable.getTiletype(oX, oY, 1), tileset);

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
