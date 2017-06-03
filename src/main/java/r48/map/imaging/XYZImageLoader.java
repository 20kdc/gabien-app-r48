/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import r48.io.r2k.R2kUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

/**
 * Here goes nothing.
 * Created on 31/05/17.
 */
public class XYZImageLoader implements IImageLoader {
    public final String root;

    public XYZImageLoader(String rootPath) {
        root = rootPath;
    }

    @Override
    public IGrInDriver.IImage getImage(String name, boolean panorama) {
        try {
            FileInputStream fis = new FileInputStream(root + name + ".xyz");
            if (fis.read() != 'X')
                throw new IOException("Bad magic");
            if (fis.read() != 'Y')
                throw new IOException("Bad magic");
            if (fis.read() != 'Z')
                throw new IOException("Bad magic");
            if (fis.read() != '1')
                throw new IOException("Bad magic");
            int w = R2kUtil.readLcfU16(fis);
            int h = R2kUtil.readLcfU16(fis);
            // The rest of the file is ZLIB-encoded data, with a trivial format.
            InflaterInputStream iis = new InflaterInputStream(fis);
            int[] pal = new int[256];
            for (int i = 0; i < 256; i++) {
                int r = R2kUtil.readLcfU8(iis);
                int g = R2kUtil.readLcfU8(iis);
                int b = R2kUtil.readLcfU8(iis);
                pal[i] = ((r << 16) | (g << 8) | b);
                if ((i != 0) | panorama)
                    pal[i] |= 0xFF000000;
            }
            int[] img = new int[w * h];
            int ind = 0;
            for (int i = 0; i < w * h; i++)
                img[ind++] = pal[R2kUtil.readLcfU8(iis)];
            return GaBIEn.createImage(img, w, h);
        } catch (Exception e) {
            // Exceptions here are, frankly, accepted behavior.
            // e.printStackTrace();
            return null;
        }
    }

    @Override
    public void flushCache() {

    }
}
