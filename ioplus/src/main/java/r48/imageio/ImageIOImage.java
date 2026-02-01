/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.imageio;

import gabien.GaBIEn;
import gabien.render.IImage;
import gabien.render.WSIImage;

import java.util.LinkedList;

/**
 * An image loaded via r48.imageio
 * Created on April 13th 2018
 */
public class ImageIOImage {
    // Width, height.
    public final int width, height;
    // The presence/lack of of a palette is fixed - the contents aren't.
    // LinkedList<Integer> because a ColourMode class would be ridiculous.
    // NOTE: IF YOU ARE COMING FROM THE IMAGEEDITOR CODE, DON'T ACCESS DIRECTLY!
    // You're supposed to use the ImageEditorImage stuff.
    public final LinkedList<Integer> palette;
    // ARGB or indexes as appropriate.
    public final int[] colourData;

    // NOTE: Those caught editing the data/pal after creation are meanies.
    // Don't do it. >:(
    public ImageIOImage(int w, int h, int[] data, LinkedList<Integer> pal) {
        width = w;
        height = h;
        if (data.length != (w * h))
            throw new IndexOutOfBoundsException("Length must be equal to w * h (" + (w * h) + "), not " + data.length);
        colourData = data;
        palette = pal;
    }

    /**
     * Translates colourData to ARGB, by-reference if possible.
     */
    private int[] colourDataToARGBByRef() {
        if (palette == null) {
            return colourData;
        } else {
            int[] data = new int[colourData.length];
            for (int i = 0; i < data.length; i++)
                data[i] = translatePalette(colourData[i]);
            return data;
        }
    }

    public IImage rasterize() {
        return GaBIEn.createImage(colourDataToARGBByRef(), width, height);
    }

    public WSIImage rasterizeToWSI() {
        return GaBIEn.createWSIImage(colourDataToARGBByRef(), width, height);
    }

    private int translatePalette(int c) {
        if ((c < 0) || (c >= palette.size())) {
            return 0xFFFF00FF;
        } else {
            return palette.get(c);
        }
    }

    public int getRGB(int i, int j) {
        int c = colourData[i + (j * width)];
        if (palette != null)
            return translatePalette(c);
        return c;
    }

    public int getRaw(int i, int j) {
        return colourData[i + (j * width)];
    }
}
