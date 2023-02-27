/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.imageio;

import gabien.IImage;
import r48.App;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.io.BMPConnection;

import java.io.IOException;
import java.util.LinkedList;

/**
 * It's that format we all know and love.
 * Created on June 13th 2018.
 */
public class BMP8IImageIOFormat extends ImageIOFormat {
    public int actuallyBits;

    public BMP8IImageIOFormat(App app, int actuallyB) {
        super(app, false);
        actuallyBits = actuallyB;
    }

    @Override
    public String saveName(ImageIOImage img) {
        if (img.palette == null)
            return null;
        // not that this should happen, but...
        if (img.palette.size() == 0)
            return null;
        if (img.palette.size() > (1 << actuallyBits))
            return null;
        return app.fmt.formatExtended(TXDB.get("Save BMP-#AI"), new RubyIO().setFX(actuallyBits));
    }

    @Override
    public byte[] saveFile(ImageIOImage img) throws IOException {
        if (saveName(img) == null)
            throw new IOException("Not supposed to be here, are we?");
        byte[] base = BMPConnection.prepareBMP(img.width, img.height, actuallyBits, img.palette.size(), false, false);
        BMPConnection bc = new BMPConnection(base, BMPConnection.CMode.Normal, 0, false);
        for (int i = 0; i < img.palette.size(); i++)
            bc.putPalette(i, img.palette.get(i));
        for (int i = 0; i < img.width; i++)
            for (int j = 0; j < img.height; j++)
                bc.putPixel(i, j, img.getRaw(i, j));
        return base;
    }

    @Override
    public ImageIOImage loadFile(byte[] s, IImage gInput) throws IOException {
        BMPConnection eDreams = new BMPConnection(s, BMPConnection.CMode.Normal, 0, false);
        if (eDreams.ignoresPalette)
            throw new IOException("Shouldn't load image this way ; it's not paletted. Better to use native methods if possible.");
        if (actuallyBits != eDreams.bpp)
            throw new IOException("Not the same amount of bits");
        int[] pixels = new int[eDreams.width * eDreams.height];
        for (int i = 0; i < pixels.length; i++)
            pixels[i] = eDreams.getPixel(i % eDreams.width, i / eDreams.width);
        LinkedList<Integer> pal = new LinkedList<Integer>();
        for (int i = 0; i < eDreams.paletteCol; i++)
            pal.add(eDreams.getPalette(i) | 0xFF000000);
        return new ImageIOImage(eDreams.width, eDreams.height, pixels, pal);
    }
}
