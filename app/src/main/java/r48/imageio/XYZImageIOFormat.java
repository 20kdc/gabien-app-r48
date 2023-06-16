/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.imageio;

import r48.app.AppCore;
import r48.io.IntUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import gabien.render.IImage;

/**
 * Here goes nothing.
 * Created on 31/05/17.
 */
public class XYZImageIOFormat extends ImageIOFormat {
    public XYZImageIOFormat(AppCore app) {
        super(app, false);
    }

    public static boolean supports(ImageIOImage img) {
        if (img.palette != null)
            return img.palette.size() <= 256;
        return false;
    }

    @Override
    public String saveName(ImageIOImage img) {
        if (supports(img))
            return T.g.img_xyz;
        return null;
    }

    @Override
    public byte[] saveFile(ImageIOImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write('X');
        baos.write('Y');
        baos.write('Z');
        baos.write('1');
        IntUtils.writeU16(baos, img.width);
        IntUtils.writeU16(baos, img.height);
        DeflaterOutputStream d2 = new DeflaterOutputStream(baos);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        for (int i = 0; i < 256; i++) {
            int c = 0;
            if (img.palette.size() > i)
                c = img.palette.get(i);
            baos2.write((c >> 16) & 0xFF);
            baos2.write((c >> 8) & 0xFF);
            baos2.write(c & 0xFF);
        }
        for (int i = 0; i < img.colourData.length; i++)
            baos2.write(img.colourData[i]);
        baos2.writeTo(d2);
        d2.finish();
        return baos.toByteArray();
    }

    @Override
    public ImageIOImage loadFile(byte[] s, IImage gInput) throws IOException {
        ByteArrayInputStream fis = new ByteArrayInputStream(s);
        if (fis.read() != 'X')
            throw new IOException("Bad magic");
        if (fis.read() != 'Y')
            throw new IOException("Bad magic");
        if (fis.read() != 'Z')
            throw new IOException("Bad magic");
        if (fis.read() != '1')
            throw new IOException("Bad magic");
        int w = IntUtils.readU16(fis);
        int h = IntUtils.readU16(fis);
        // The rest of the file is ZLIB-encoded data, with a trivial format.
        InflaterInputStream iis = new InflaterInputStream(fis);
        LinkedList<Integer> pal = new LinkedList<Integer>();
        for (int i = 0; i < 256; i++) {
            int r = IntUtils.readU8(iis);
            int g = IntUtils.readU8(iis);
            int b = IntUtils.readU8(iis);
            pal.add(0xFF000000 | ((r << 16) | (g << 8) | b));
        }
        int[] img = new int[w * h];
        int ind = 0;
        for (int i = 0; i < w * h; i++)
            img[ind++] = IntUtils.readU8(iis);
        return new ImageIOImage(w, h, img, pal);
    }
}
