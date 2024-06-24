/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.map.tiles;

import gabien.uslx.append.MathsX;
import gabien.uslx.append.Rect;
import r48.ITileAccess;
import r48.RubyTable;
import r48.RubyTableR;

/**
 * Tile looper
 * It is an API guarantee that this only changes X/Y/P bases; the resulting bases are compatible with the inner tile access
 * Created 24th June 2024
 */
public class LoopTileAccess implements ITileAccess.Bounded {
    private final ITileAccess.Bounded base;
    private final Rect bounds;
    private final int planes;
    private final boolean loopX, loopY;

    public LoopTileAccess(ITileAccess.Bounded ro, boolean loopX, boolean loopY) {
        base = ro;
        this.bounds = ro.getBounds();
        this.planes = ro.getPlanes();
        this.loopX = loopX && bounds.width != 0;
        this.loopY = loopY && bounds.height != 0;
    }

    @Override
    public int getXBase(int x) {
        if (loopX)
            x = MathsX.seqModulo(x - bounds.x, bounds.width) + bounds.x;
        return base.getXBase(x);
    }

    @Override
    public int getYBase(int y) {
        if (loopY)
            y = MathsX.seqModulo(y - bounds.y, bounds.height) + bounds.y;
        return base.getYBase(y);
    }

    @Override
    public int getPBase(int p) {
        return base.getPBase(p);
    }

    @Override
    public Rect getBounds() {
        return bounds;
    }

    @Override
    public int getPlanes() {
        return planes;
    }

    @Override
    public int getTiletypeRaw(int cellID) {
        return base.getTiletypeRaw(cellID);
    }

    public static class RW extends LoopTileAccess implements ITileAccess.RWBounded {
        public final ITileAccess.RWBounded baseRW;

        public RW(ITileAccess.RWBounded rw, boolean loopX, boolean loopY) {
            super(rw, loopX, loopY);
            baseRW = rw;
        }

        @Override
        public void setTiletypeRaw(int cellID, int value) {
            baseRW.setTiletypeRaw(cellID, value);
        }
    }

    public static ITileAccess.Bounded of(RubyTableR tbl, boolean tileLoopX, boolean tileLoopY) {
        if (tileLoopX || tileLoopY)
            return new LoopTileAccess(tbl, tileLoopX, tileLoopY);
        return tbl;
    }

    public static ITileAccess.RWBounded of(RubyTable tbl, boolean tileLoopX, boolean tileLoopY) {
        if (tileLoopX || tileLoopY)
            return new LoopTileAccess.RW(tbl, tileLoopX, tileLoopY);
        return tbl;
    }
}
