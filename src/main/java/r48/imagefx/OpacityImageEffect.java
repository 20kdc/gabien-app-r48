/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.imagefx;

import gabien.GaBIEn;
import gabien.IImage;

/**
 * Created on 30/07/17.
 */
public class OpacityImageEffect implements IImageEffect {
    public final int opacity;

    public OpacityImageEffect(int o) {
        opacity = o;
    }

    @Override
    public String uniqueToString() {
        return "O" + opacity;
    }

    @Override
    public IImage process(IImage image) {
        int[] data = image.getPixels();
        for (int i = 0; i < data.length; i++) {
            int base = data[i] & 0xFFFFFF;
            int rest = (data[i] >> 24) & 0xFF;
            double dp = rest / 255.0d;
            dp *= opacity / 255.0d;
            rest = (int) (dp * 255);
            data[i] = base | (rest << 24);
        }
        return GaBIEn.createImage(data, image.getWidth(), image.getHeight());
    }
}
