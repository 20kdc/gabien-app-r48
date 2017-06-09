/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
    public R2kPassabilitySource(RubyTable rt, RubyIO ts) {
        mapTable = rt;
        tileset = ts;
    }
    @Override
    public int getPassability(int x, int y) {
        if (mapTable.outOfBounds(x, y))
            return -1;
        // Outward passability is AND of lower and upper layer state.
        int f0 = R2kTileMapViewDrawLayer.getTileFlags(mapTable.getTiletype(x, y, 0), tileset);
        int f1 = R2kTileMapViewDrawLayer.getTileFlags(mapTable.getTiletype(x, y, 1), tileset);
        // Somewhere along the line this might have gotten the wrong way around...
        boolean down = merge(f0, f1, 0x08, 0x01, x, y + 1);
        boolean right = merge(f0, f1, 0x04, 0x02, x + 1, y);
        boolean left = merge(f0, f1, 0x02, 0x04, x - 1, y);
        boolean up = merge(f0, f1, 0x01, 0x08, x, y - 1);
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

    private boolean merge(int f0, int f1, int flag, int flagInv, int oX, int oY) {
        if (mapTable.outOfBounds(oX, oY))
            return false;
        if ((f0 & flag) != 0)
            if ((f1 & flag) != 0) {
                // might be possible, check for target wall
                int fT0 = R2kTileMapViewDrawLayer.getTileFlags(mapTable.getTiletype(oX, oY, 0), tileset);
                int fT1 = R2kTileMapViewDrawLayer.getTileFlags(mapTable.getTiletype(oX, oY, 1), tileset);
                if ((fT0 & flagInv) == 0)
                    return false;
                if ((fT1 & flagInv) == 0)
                    return false;
                return true;
            }
        return false;
    }
}
