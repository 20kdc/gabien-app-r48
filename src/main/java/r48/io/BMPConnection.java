/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Ported from the OC-KittenOS code.
 * Way I see it, if I'm doing this I'm making sure it's reusable for a "later" if ever there is one.
 * Created on June 13th 2018.
 */
public class BMPConnection {
    public final byte[] targetBuffer;
    // All public functions should set endianness
    private final ByteBuffer data;
    public final int bmpFieldOffset;
    public final int paletteZoneBase, dataZoneBase, scanStride;
    public final int paletteCol;
    public final int width, height, bpp;
    public final boolean upDown, ignoresPalette;

    public BMPConnection(byte[] buffer, CMode mode, int bfo, boolean packed) throws IOException {
        data = ByteBuffer.wrap(buffer);
        data.order(ByteOrder.LITTLE_ENDIAN);
        targetBuffer = buffer;
        bmpFieldOffset = bfo;
        if (!packed) {
            if (data.get(bmpFieldOffset) != 'B')
                throw new IOException("Bad 'B'");
            if (data.get(bmpFieldOffset + 1) != 'M')
                throw new IOException("Bad 'M'");
        }
        int hdrSize = data.getInt(0x0E + bmpFieldOffset);
        if (hdrSize < 0x28)
            throw new IOException("OS/2 Bitmaps Incompatible");
        int compression = data.getInt(0x1E + bmpFieldOffset);
        int epc = data.getInt(0x2E + bmpFieldOffset);
        width = data.getInt(0x12 + bmpFieldOffset);
        int h = data.getInt(0x16 + bmpFieldOffset);
        upDown = h >= 0;
        if (!upDown)
            h = -h;
        if (data.getShort(0x1A + bmpFieldOffset) > 1)
            throw new IOException("Nope, we're pretending planes don't exist, none of that nonsense here.");

        int ebpp = data.getShort(0x1C + bmpFieldOffset);
        // Read out the various bits and pieces, but now for the mportant stuff.
        int basePtr;
        if (packed) {
            basePtr = 0x0E + hdrSize + (epc * 4) + bmpFieldOffset;
        } else {
            basePtr = data.getInt(0x0A + bmpFieldOffset) + bmpFieldOffset;
        }

        int scanWB = (((ebpp * width) + 31) / 32) * 4;
        int monoWB = ((width + 31) / 32) * 4;

        if (mode != CMode.Normal)
            h /= 2;
        if (mode == CMode.Mask) {
            if (upDown)
                basePtr += scanWB * h;
            bpp = 1;
            scanWB = monoWB;
            epc = 0;
            compression = 3;
        } else if (mode == CMode.Colour) {
            bpp = ebpp;
            if (!upDown)
                basePtr += monoWB * h;
        } else {
            bpp = ebpp;
        }

        if (compression != 0)
            if (compression != 3)
                if (compression != 6)
                    throw new IOException("Unsupported compression " + compression);

        // When all else is said and done, account for a palette colour count of 0
        if ((bpp <= 8) && (epc == 0) && (compression == 0))
            epc = 1 << bpp;

        paletteZoneBase = 14 + hdrSize + bmpFieldOffset;
        dataZoneBase = basePtr;
        scanStride = scanWB;
        height = h;
        paletteCol = epc;
        ignoresPalette = (compression != 0) || (epc == 0);
        if (bpp > 32)
            throw new IOException("The bitfield routines cannot work with >32-bit values.");
    }

    private long getAreaAt(int base, int lenBytes) {
        data.order(ByteOrder.BIG_ENDIAN);
        long v = 0;
        long vBase = (lenBytes - 1) * 8;
        for (int i = 0; i < lenBytes; i++) {
            v |= (data.get(base + i) & 0xFF) << vBase;
            vBase -= 8;
        }
        return v;
    }

    private long putAreaAt(int base, int lenBytes, long v) {
        data.order(ByteOrder.BIG_ENDIAN);
        long vBase = (lenBytes - 1) * 8;
        for (int i = 0; i < lenBytes; i++) {
            data.put(base + i, (byte) ((v >> vBase) & 0xFF));
            vBase -= 8;
        }
        return v;
    }

    private int getAt(int base, int bitNumber, int len) {
        data.order(ByteOrder.BIG_ENDIAN);
        int adv = bitNumber / 8;
        base += adv;
        bitNumber -= adv * 8;
        int byteCount = (len + 7) / 8;
        if ((bitNumber + len) > (byteCount * 8))
            byteCount++;
        long maskUS = (1L << len) - 1;
        long shift = (byteCount * 8) - (len + bitNumber);
        // --
        long data = getAreaAt(base, byteCount);
        return (int) ((data >> shift) & maskUS);
    }

    private void putAt(int base, int bitNumber, int len, int val) {
        data.order(ByteOrder.BIG_ENDIAN);
        int adv = bitNumber / 8;
        base += adv;
        bitNumber -= adv * 8;
        int byteCount = (len + 7) / 8;
        if ((bitNumber + len) > (byteCount * 8))
            byteCount++;
        long maskUS = (1L << len) - 1;
        long shift = (byteCount * 8) - (len + bitNumber);
        // --
        long data = getAreaAt(base, byteCount);
        data &= ~(maskUS << shift);
        data |= (val & maskUS) << shift;
        putAreaAt(base, byteCount, data);
    }

    public int getPixel(int x, int y) {
        if (upDown)
            y = height - (y + 1);
        return getAt(dataZoneBase + (scanStride * y), x * bpp, bpp);
    }

    public void putPixel(int x, int y, int v) {
        if (upDown)
            y = height - (y + 1);
        putAt(dataZoneBase + (scanStride * y), x * bpp, bpp, v);
    }

    // NOTE: "Alpha" here may not be accurate.
    public int getPalette(int col) {
        data.order(ByteOrder.LITTLE_ENDIAN);
        return data.getInt(paletteZoneBase + (col * 4));
    }

    public void putPalette(int col, int v) {
        data.order(ByteOrder.LITTLE_ENDIAN);
        data.putInt(paletteZoneBase + (col * 4), v);
    }

    // cMode is icon/cursor mode. In this case, you should throw away the BMP header. It's not really valid.
    public static byte[] prepareBMP(int w, int h, int bpp, int paletteSize, boolean topDown, boolean cMode) {
        if (bpp <= 8)
            if (paletteSize == 0)
                throw new RuntimeException("paletteSize = 0 is only really valid for 16-bit, 24-bit, or 32-bit.");
        int scanWB = (((bpp * w) + 31) / 32) * 4;
        int monoWB = ((w + 31) / 32) * 4;
        int palSize = paletteSize * 4;
        int bufSize = scanWB * h;
        int aH = h;
        if (cMode) {
            aH *= 2;
            bufSize += monoWB * h;
        }
        if (topDown)
            aH = -aH;
        byte[] data = new byte[0x36 + palSize + bufSize];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(0x00, (byte) 'B');
        bb.put(0x01, (byte) 'M');
        bb.putInt(0x02, data.length);
        bb.put(0x06, (byte) 'm');
        bb.put(0x07, (byte) 'R');
        bb.put(0x08, (byte) 'W');
        bb.put(0x09, (byte) 'H');
        bb.putInt(0x0A, 0x36 + palSize);
        // TD
        bb.putInt(0x0E, 0x28);
        bb.putInt(0x12, w);
        bb.putInt(0x16, aH);
        bb.putInt(0x1A, 1);
        bb.putInt(0x1C, bpp);
        bb.putInt(0x1E, 0);
        bb.putInt(0x22, bufSize);
        bb.putInt(0x26, 0);
        bb.putInt(0x2A, 0);
        bb.putInt(0x2E, paletteSize);
        bb.putInt(0x32, paletteSize);
        return data;
    }

    public enum CMode {
        Normal,
        Mask,
        Colour
    }
}
