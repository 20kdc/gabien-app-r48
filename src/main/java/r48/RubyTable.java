/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
    public final int dataOffset;
    private final int[] dimensions;

    public RubyTable(byte[] data) {
        innerBytes = data;
        innerTable = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        innerTable.position(0);
        dimensions = new int[innerTable.getInt()];
        for (int i = 0; i < dimensions.length; i++)
            dimensions[i] = innerTable.getInt();
        dataOffset = innerTable.position() + 4;
        innerTable.position(0);
    }

    public RubyTable(int w, int h, int[] defVals) {
        dimensions = new int[] {
                w,
                h,
                defVals.length
        };
        dataOffset = 20;
        innerBytes = new byte[20 + (w * h * defVals.length * 2)];
        innerTable = ByteBuffer.wrap(innerBytes).order(ByteOrder.LITTLE_ENDIAN);
        innerTable.putInt(0, 3);
        innerTable.putInt(4, w);
        innerTable.putInt(8, h);
        innerTable.putInt(12, defVals.length);
        innerTable.putInt(16, w * h * defVals.length);
        for (int j = 0; j < (w * h); j++)
            for (int l = 0; l < defVals.length; l++)
                innerTable.putShort(20 + ((j + (l * (w * h))) * 2), (short) defVals[l]);
    }

    public boolean isNormalized() {
        return dimensions.length == 3;
    }

    public int getDimensions() {
        return dimensions.length;
    }

    public int getDimension(int d) {
        return dimensions[d];
    }

    private int decodeCoodinate(int[] c) {
        int multiplier = 1;
        int p = 0;
        for (int i = 0; i < c.length; i++) {
            p += c[i] * multiplier;
            multiplier *= dimensions[i];
        }
        return (p * 2) + dataOffset;
    }

    // for N-dimensional tables
    public short getTiletypeN(int[] coordinate) {
        return innerTable.getShort(decodeCoodinate(coordinate));
    }

    public void setTiletypeN(int[] coordinate, short type) {
        innerTable.putShort(decodeCoodinate(coordinate), type);
    }

    public short getTiletype(int i, int i1, int i2) {
        if (dimensions.length != 3)
            throw new RuntimeException("Attempted 3D access on un-normalized table.");
        return innerTable.getShort(dataOffset + ((i + (dimensions[0] * (i1 + (dimensions[1] * i2)))) * 2));
    }

    public void setTiletype(int i, int i1, int i2, short i3) {
        if (dimensions.length != 3)
            throw new RuntimeException("Attempted 3D access on un-normalized table.");
        innerTable.putShort(dataOffset + ((i + (dimensions[0] * (i1 + (dimensions[1] * i2)))) * 2), i3);
    }

    // This can and should restrict itself to only the numbers given.
    // If too many numbers are given, throwing an exception is fine,
    //  if too few are given, consider that just a wildcard.
    public boolean outOfBounds(int[] coordinate) {
        for (int i = 0; i < coordinate.length; i++) {
            if (coordinate[i] < 0)
                return true;
            if (coordinate[i] >= dimensions[i])
                return true;
        }
        return false;
    }

    public RubyTable resize(int w, int h, int[] defVals) {
        if (getDimensions() != 3)
            throw new RuntimeException("Attempted to resize " + getDimensions() + "-dimensional table");
        RubyTable n = new RubyTable(w, h, defVals);
        for (int i = 0; i < dimensions[0]; i++) {
            if (w <= i)
                break;
            for (int j = 0; j < dimensions[1]; j++) {
                if (h <= j)
                    break;
                for (int k = 0; k < dimensions[2]; k++)
                    n.setTiletype(i, j, k, getTiletype(i, j, k));
            }
        }
        return n;
    }
}
