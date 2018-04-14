/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.imaging;

import gabien.GaBIEn;
import gabien.IImage;
import r48.io.PathUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * You know what I mentioned previously about "Here goes nothing"?
 * Well, now here goes even more nothing.
 * Basically this uses a complicated mouse-trap design to eventually get a useful colour-keyed image.
 * Now, of course, I have the new problem of changing Gabien API to fix the fact it's clear the "tagged blits" stuff just makes life worse.
 * Also, for reference, I tried implementing a full 8-bit-depth-locked (for sanity) PNG loader here.
 * Hint: DON'T. It's hell.
 * Created on 02/06/17.
 */
public class PNG8IImageLoader implements IImageLoader {

    public static byte[][] getPalette(String s) {
        DataInputStream dis = null;
        try {
            String ad = PathUtils.autoDetectWindows(s);
            dis = new DataInputStream(GaBIEn.getInFile(ad));
            // Magic number blahblahblah
            byte[] magic = new byte[8];
            if (dis.read(magic) != 8)
                throw new IOException("didn't read all of the magic");
            if (magic[0] != (byte) 0x89)
                throw new IOException("bad magic byte 0");
            if (magic[1] != (byte) 0x50)
                throw new IOException("bad magic byte 1");
            if (magic[2] != (byte) 0x4E)
                throw new IOException("bad magic byte 2");
            if (magic[3] != (byte) 0x47)
                throw new IOException("bad magic byte 3");
            byte[] hdr = getChunk("IHDR", dis);
            if (hdr == null) {
                dis.close();
                return null;
            }
            if (hdr[9] != 3) {
                dis.close();
                return null;
            }
            byte[] pal = getChunk("PLTE", dis);
            byte[] trs = null;
            if (pal != null)
                trs = getChunk("tRNS", dis);
            dis.close();
            return new byte[][] {pal, trs};
        } catch (Exception ioe) {
            try {
                if (dis != null)
                    dis.close();
            } catch (Exception e2) {

            }
            // Exceptions here are, frankly, accepted behavior.
            return null;
        }
    }

    @Override
    public IImage getImage(String name, boolean panorama) {
        if (panorama)
            return null;
        name += ".png";
        byte[][] pal = getPalette(name);
        if (pal != null)
            return GaBIEn.getImageCKEx(PathUtils.autoDetectWindows(name), true, false, pal[0][0] & 0xFF, pal[0][1] & 0xFF, pal[0][2] & 0xFF);
        return null;
    }

    private static byte[] getChunk(String ihdr, DataInputStream dis) throws IOException {
        int magicWanted = 0;
        int end = 0;
        try {
            byte[] d = ihdr.getBytes("UTF-8");
            magicWanted = new DataInputStream(new ByteArrayInputStream(d)).readInt();
            end = new DataInputStream(new ByteArrayInputStream("IEND".getBytes("UTF-8"))).readInt();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        while (true) {
            int len = dis.readInt();
            int maj = dis.readInt();
            if (maj == end)
                return null;
            if (maj == magicWanted) {
                byte[] data = new byte[len];
                if (dis.read(data) != len)
                    throw new IOException("did not read all of chunk");
                dis.readInt(); // checksum
                return data;
            } else {
                dis.skipBytes(len);
            }
            dis.readInt(); // checksum
        }
    }

    @Override
    public void flushCache() {

    }

    public static int[] convPal(byte[][] palette) {
        if (palette == null)
            return null;
        int[] data = new int[palette[0].length / 3];
        for (int i = 0; i < palette[0].length; i += 3) {
            int a = 0xFF;
            int p = i / 3;
            if (palette[1] != null)
                if (p < palette[1].length)
                    a = palette[1][p] & 0xFF;
            data[p] = a << 24;
            data[p] |= (palette[0][i] & 0xFF) << 16;
            data[p] |= (palette[0][i + 1] & 0xFF) << 8;
            data[p] |= (palette[0][i + 2] & 0xFF);
        }
        return data;
    }
}
