/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.imagefx;

import gabien.GaBIEn;
import gabien.IGrInDriver;

import java.util.LinkedList;

/**
 * NOTE: This doesn't emulate what appears to be an automatic gamma-adjust in RPG_RT & EasyRPG Player,
 *  that actually makes this look completely off. I don't know how to emulate that.
 * Created on 31/07/17.
 */
public class ToneImageEffect implements IImageEffect {
    public final int ar, ag, ab, as;
    public ToneImageEffect(int i, int i1, int i2, int i3) {
        ar = i;
        ag = i1;
        ab = i2;
        as = i3;
    }

    @Override
    public String uniqueToString() {
        return "T" + ar + "," + ag + "," + ab + "," + as;
    }

    @Override
    public IGrInDriver.IImage process(IGrInDriver.IImage input) {
        int[] array = input.getPixels();
        for (int i = 0; i < array.length; i++) {
            array[i] = processCol(array[i]);
        }
        return GaBIEn.createImage(array, input.getWidth(), input.getHeight());
    }

    private int processCol(int i) {
        int r = (i & 0xFF0000) >> 16;
        int g = (i & 0xFF00) >> 8;
        int b = i & 0xFF;
        int grey = (r + g + b) / 3;
        r = grey + mod1(r - grey, as);
        g = grey + mod1(g - grey, as);
        b = grey + mod1(b - grey, as);
        r = clamp(mod1(r, ar));
        g = clamp(mod1(g, ag));
        b = clamp(mod1(b, ab));
        return (i & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private int clamp(int i) {
        if (i < 0)
            return 0;
        if (i > 255)
            return 255;
        return i;
    }

    private int mod1(int i, int m) {
        return (i * m) / 127;
    }
}
