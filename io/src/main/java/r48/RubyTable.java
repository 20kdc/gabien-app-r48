/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Table class, kind of.
 * Note that the given byte[] is manipulated by-reference.
 * Created on 12/27/16.
 */
public class RubyTable {
    public final byte[] innerBytes;
    public final ByteBuffer innerTable;
    // Can be 0, 1, or 2. Apparently does not affect actual function.
    // Nevermind the weird "support" for 4D tables, and the inconsistent... arggggghhhh.
    public final int dimensionCount;
    public final int width, height, planeCount;

    public RubyTable(byte[] data) {
        innerBytes = data;
        innerTable = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        dimensionCount = innerTable.getInt(0);
        planeCount = innerTable.getInt(12);
        width = innerTable.getInt(4);
        height = innerTable.getInt(8);
    }

    public RubyTable(int dc, int w, int h, int i, int[] defVals) {
        innerBytes = new byte[20 + (w * h * i * 2)];
        innerTable = ByteBuffer.wrap(innerBytes).order(ByteOrder.LITTLE_ENDIAN);
        width = w;
        height = h;
        planeCount = i;
        dimensionCount = dc;
        innerTable.putInt(0, dc);
        innerTable.putInt(4, w);
        innerTable.putInt(8, h);
        innerTable.putInt(12, i);
        innerTable.putInt(16, w * h * i);
        for (int j = 0; j < (w * h); j++)
            for (int l = 0; l < i; l++)
                innerTable.putShort(20 + ((j + (l * (w * h))) * 2), (short) defVals[l]);
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
        return innerTable.getShort(p);
    }

    public void setTiletype(int x, int y, int plane, short type) {
        int p = 20 + ((x + (y * width)) * 2);
        p += width * height * 2 * plane;
        innerTable.putShort(p, type);
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

    public RubyTable resize(int w, int h, int[] defVals) {
        RubyTable n = new RubyTable(dimensionCount, w, h, planeCount, defVals);
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
        return n;
    }
}
