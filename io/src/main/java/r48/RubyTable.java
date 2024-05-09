/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import gabien.uslx.io.ByteArrayMemoryish;
import gabien.uslx.io.MemoryishRW;

/**
 * Table class, kind of.
 * Note that the given Memoryish is manipulated by-reference.
 * Created on 12/27/16. Split into RubyTable/RubyTableR 9th May, 2024.
 */
public class RubyTable extends RubyTableR {
    public final MemoryishRW innerTableW;

    public RubyTable(MemoryishRW data) {
        super(data);
        this.innerTableW = data;
    }

    public static ByteArrayMemoryish initNewTable(int dc, int w, int h, int i, int[] defVals) {
        ByteArrayMemoryish bam = new ByteArrayMemoryish(new byte[20 + (w * h * i * 2)]);
        bam.set32LE(0, dc);
        bam.set32LE(4, w);
        bam.set32LE(8, h);
        bam.set32LE(12, i);
        bam.set32LE(16, w * h * i);
        for (int j = 0; j < (w * h); j++)
            for (int l = 0; l < i; l++)
                bam.set16LE(20 + ((j + (l * (w * h))) * 2), (short) defVals[l]);
        return bam;
    }

    public RubyTable(int dc, int w, int h, int i, int[] defVals) {
        this(initNewTable(dc, w, h, i, defVals));
    }

    public void setTiletype(int x, int y, int plane, short type) {
        int p = 20 + ((x + (y * width)) * 2);
        p += width * height * 2 * plane;
        innerTableW.set16LE(p, type);
    }
}
