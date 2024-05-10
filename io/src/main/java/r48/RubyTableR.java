/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import gabien.uslx.io.ByteArrayMemoryish;
import gabien.uslx.io.MemoryishR;

/**
 * Table class, kind of.
 * Note that the given Memoryish is manipulated by-reference.
 * Created on 12/27/16. Split into RubyTable/RubyTableR 9th May, 2024.
 */
public class RubyTableR {
    public final MemoryishR innerTable;
    // Can be 0, 1, or 2. Apparently does not affect actual function.
    // Nevermind the weird "support" for 4D tables, and the inconsistent... arggggghhhh.
    public final int dimensionCount;
    public final int width, height, planeCount;

    public RubyTableR(byte[] data) {
        this(new ByteArrayMemoryish(data));
    }

    public RubyTableR(MemoryishR data) {
        innerTable = data;
        dimensionCount = innerTable.getS32LE(0);
        planeCount = innerTable.getS32LE(12);
        width = innerTable.getS32LE(4);
        height = innerTable.getS32LE(8);
    }

    // inline notes on Table format:
    // first 4 bytes match plane count later on. giving up and checking mkxp gives no further detail.
    // next 4 bytes are a LE32-bit width
    // further 4 bytes are a LE32-bit textHeight
    // LE32-bit "depth" (plane count).
    // 4 bytes which seems to be w * h * d.
    // It seems to be consistent enough between files for now, in any case.
    // The data is based around 16-bit tile planes.

    public short getTiletype(int x, int y, int plane) {
        int p = 20 + ((x + (y * width)) * 2);
        p += width * height * 2 * plane;
        return innerTable.getS16LE(p);
    }

    public boolean outOfBounds(int x, int y) {
        if (x < 0)
            return true;
        if (x >= width)
            return true;
        if (y < 0)
            return true;
        if (y >= height)
            return true;
        return false;
    }

    public ByteArrayMemoryish resize(int w, int h, int[] defVals) {
        ByteArrayMemoryish bam = RubyTable.initNewTable(dimensionCount, w, h, planeCount, defVals);
        RubyTable n = new RubyTable(bam);
        for (int i = 0; i < width; i++) {
            if (w <= i)
                break;
            for (int j = 0; j < height; j++) {
                if (h <= j)
                    break;
                for (int k = 0; k < planeCount; k++)
                    n.setTiletype(i, j, k, getTiletype(i, j, k));
            }
        }
        return bam;
    }
}