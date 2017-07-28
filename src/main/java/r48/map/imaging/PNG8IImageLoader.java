/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabienapp.Application;

import java.io.*;

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
    public final String root;

    public PNG8IImageLoader(String rootPath) {
        root = rootPath;
    }

    @Override
    public IGrInDriver.IImage getImage(String name, boolean panorama) {
        if (panorama)
            return null;
        try {
            String ad = Application.autoDetectWindows(root + name + ".png");
            DataInputStream dis = new DataInputStream(new FileInputStream(ad));
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
            // Ignore the rest, we'd be here all day
            byte[] pal = getChunk("PLTE", dis);
            if (pal == null)
                return null;
            return GaBIEn.getImageCK(ad, pal[0] & 0xFF, pal[1] & 0xFF, pal[2] & 0xFF);
        } catch (Exception ioe) {
            // Exceptions here are, frankly, accepted behavior.
            return null;
        }
    }

    private byte[] getChunk(String ihdr, DataInputStream dis) throws IOException {
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
                return data;
            } else {
                dis.skipBytes(len);
            }
            dis.readInt();
        }
    }

    @Override
    public void flushCache() {

    }
}
