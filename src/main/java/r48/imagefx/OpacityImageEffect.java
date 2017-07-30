/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.imagefx;

import gabien.GaBIEn;
import gabien.IGrInDriver;

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
    public IGrInDriver.IImage process(IGrInDriver.IImage image) {
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
