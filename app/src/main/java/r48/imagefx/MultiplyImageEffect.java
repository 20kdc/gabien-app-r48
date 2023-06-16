/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.imagefx;

import gabien.GaBIEn;
import gabien.render.IImage;

/**
 * Created on 30/07/17.
 */
public class MultiplyImageEffect implements IImageEffect {
    public final int alpha, red, green, blue;

    public MultiplyImageEffect(int a, int r, int g, int b) {
        alpha = a;
        red = r;
        green = g;
        blue = b;
    }

    @Override
    public String uniqueToString() {
        return "*" + alpha + "," + red + "," + green + "," + blue;
    }

    @Override
    public IImage process(IImage image) {
        int[] data = image.getPixels();
        for (int i = 0; i < data.length; i++) {
            int a = (data[i] >> 24) & 0xFF;
            int r = (data[i] >> 16) & 0xFF;
            int g = (data[i] >> 8) & 0xFF;
            int b = data[i] & 0xFF;

            a = (a * alpha) / 255;
            r = (r * red) / 255;
            g = (g * green) / 255;
            b = (b * blue) / 255;

            if (a > 255)
                a = 255;
            if (r > 255)
                r = 255;
            if (g > 255)
                g = 255;
            if (b > 255)
                b = 255;

            data[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        return GaBIEn.createImage(data, image.getWidth(), image.getHeight());
    }
}
