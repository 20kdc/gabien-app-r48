/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.map.tiles;

import gabien.uslx.append.MathsX;
import r48.ITileAccess;
import r48.RubyTable;
import r48.RubyTableR;

/**
 * Tile looper
 * It is an API guarantee that this only changes X/Y/P bases; the resulting bases are compatible with the inner tile access
 * Created 24th June 2024
 */
public class LoopTileAccess implements ITileAccess {
    public final ITileAccess base;
    public final int width, height;

    public LoopTileAccess(ITileAccess ro, int width, int height) {
        base = ro;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getXBase(int x) {
        if (width != 0)
            x = MathsX.seqModulo(x, width);
        return base.getXBase(x);
    }

    @Override
    public int getYBase(int y) {
        if (height != 0)
            y = MathsX.seqModulo(y, height);
        return base.getYBase(y);
    }

    @Override
    public int getPBase(int p) {
        return base.getPBase(p);
    }

    @Override
    public int getTiletypeRaw(int cellID) {
        return base.getTiletypeRaw(cellID);
    }

    public static class RW extends LoopTileAccess implements ITileAccess.RW {
        public final ITileAccess.RW baseRW;

        public RW(ITileAccess.RW rw, int width, int height) {
            super(rw, width, height);
            baseRW = rw;
        }

        @Override
        public void setTiletypeRaw(int cellID, int value) {
            baseRW.setTiletypeRaw(cellID, value);
        }
    }

    public static ITileAccess of(RubyTableR tbl, boolean tileLoopX, boolean tileLoopY) {
        if (tileLoopX || tileLoopY)
            return new LoopTileAccess(tbl, tileLoopX ? tbl.width : 0, tileLoopY ? tbl.height : 0);
        return tbl;
    }

    public static ITileAccess.RW of(RubyTable tbl, boolean tileLoopX, boolean tileLoopY) {
        if (tileLoopX || tileLoopY)
            return new LoopTileAccess.RW(tbl, tileLoopX ? tbl.width : 0, tileLoopY ? tbl.height : 0);
        return tbl;
    }
}
