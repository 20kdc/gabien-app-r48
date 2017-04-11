/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
    public final int width, height, planeCount;

    public RubyTable(byte[] data) {
        innerBytes = data;
        innerTable = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        planeCount = innerTable.getInt(12);
        width = innerTable.getInt(4);
        height = innerTable.getInt(8);
    }

    public RubyTable(int w, int h, int i) {
        innerBytes = new byte[20 + (w * h * i * 2)];
        innerTable = ByteBuffer.wrap(innerBytes).order(ByteOrder.LITTLE_ENDIAN);
        width = w;
        height = h;
        planeCount = i;
        innerTable.putInt(0, i);
        innerTable.putInt(4, w);
        innerTable.putInt(8, h);
        innerTable.putInt(12, i);
        innerTable.putInt(16, w * h * i);
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

    public RubyTable resize(int w, int h) {
        RubyTable n = new RubyTable(w, h, planeCount);
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
