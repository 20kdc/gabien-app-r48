/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.imagefx;

import gabien.GaBIEn;
import gabien.IImage;

/**
 * Uses a "simulated additive blending" algorithm I made up to give okay results on some tests.
 * The only additive blending that can actually work is actual additive blending.
 * Created on February 25th, 2018.
 */
public class AlphaControlImageEffect implements IImageEffect {
    // Emulate subtractive
    public final boolean type;

    public AlphaControlImageEffect(boolean t) {
        type = t;
    }

    @Override
    public String uniqueToString() {
        return "A" + (type ? '-' : '+');
    }

    @Override
    public IImage process(IImage input) {
        int[] rt = input.getPixels();
        for (int i = 0; i < rt.length; i++) {
            int b = rt[i] & 0xFF;
            rt[i] >>= 8;
            int g = rt[i] & 0xFF;
            rt[i] >>= 8;
            int r = rt[i] & 0xFF;
            rt[i] >>= 8;
            int a = rt[i] & 0xFF;
            // Process...
            int opacity = (a * Math.min(255, r + g + b)) / 255;
            // If subtracting, make RGB near-zero and double opacity. Seems the best so far
            if (type) {
                r = (255 - r) / 32;
                g = (255 - g) / 32;
                b = (255 - b) / 32;
                a = Math.min(255, opacity * 2);
            } else {
                // Here's where the cool stuff has to happen.
                a = (opacity * 2) / 3;
                while ((r < 255) && (g < 255) && (b < 255)) {
                    r *= 4;
                    r /= 3;
                    r++;
                    g *= 4;
                    g /= 3;
                    g++;
                    b *= 4;
                    b /= 3;
                    b++;
                }
                r = Math.min(r, 255);
                g = Math.min(g, 255);
                b = Math.min(b, 255);
            }
            rt[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        return GaBIEn.createImage(rt, input.getWidth(), input.getHeight());
    }
}
