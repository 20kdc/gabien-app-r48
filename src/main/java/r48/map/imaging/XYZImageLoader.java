/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.map.imaging;

import gabien.GaBIEn;
import gabien.IImage;
import gabienapp.Application;
import r48.io.PathUtils;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

/**
 * Here goes nothing.
 * Created on 31/05/17.
 */
public class XYZImageLoader implements IImageLoader {

    @Override
    public IImage getImage(String name, boolean panorama) {
        try {
            InputStream fis = GaBIEn.getInFile(PathUtils.autoDetectWindows(name + ".xyz"));
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
