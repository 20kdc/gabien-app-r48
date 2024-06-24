/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.map.tiles;

import r48.ITileAccess;

/**
 * Read only via force
 * Created 24th June 2024
 */
public class NOPWriteTileAccess implements ITileAccess.RW {
    public final ITileAccess base;

    public NOPWriteTileAccess(ITileAccess ro) {
        base = ro;
    }

    @Override
    public int getXBase(int x) {
        return base.getXBase(x);
    }

    @Override
    public int getYBase(int y) {
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

    @Override
    public void setTiletypeRaw(int cellID, int value) {
    }
}
