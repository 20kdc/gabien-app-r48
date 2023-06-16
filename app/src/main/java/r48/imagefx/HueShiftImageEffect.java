/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.imagefx;

import gabien.GaBIEn;
import gabien.render.IImage;
import gabien.ui.UIElement;

/**
 * Hue changing. If you're wondering why a family member seems a little blue, now you know.
 * Created on 31/07/17.
 */
public class HueShiftImageEffect implements IImageEffect {
    // III
    // RGB
    // 012OR
    // 345OG
    // 678OB
    // values are scaled 0 to 255
    public final int[] matrix;
    public final int shift;

    public HueShiftImageEffect(int i) {
        i = UIElement.sensibleCellMod(i, 360);
        shift = i;
        // 120
        int[] baseA, baseB, baseC;
        baseA = new int[] {
                255, 0, 0,
                0, 255, 0,
                0, 0, 255
        };
        baseB = new int[] {
                0, 0, 255,
                255, 0, 0,
                0, 255, 0
        };
        baseC = new int[] {
                0, 255, 0,
                0, 0, 255,
                255, 0, 0
        };

        if (i == 0) {
            matrix = baseA;
        } else if (i < 120) {
            matrix = sipMatrix(baseA, baseB, i / 120d);
        } else if (i == 120) {
            matrix = baseB;
        } else if (i < 240) {
            matrix = sipMatrix(baseB, baseC, (i - 120) / 120d);
        } else if (i == 240) {
            matrix = baseC;
        } else {
            matrix = sipMatrix(baseC, baseA, (i - 240) / 120d);
        }
    }

    private int[] sipMatrix(int[] baseA, int[] baseB, double v) {
        // Sine interpolate
        //int a = clamp((int) (((Math.cos(v * Math.PI) + 1.0d) / 2.0d) * 255));
        // Square interpolate
        double v2 = -((v - 0.5d) * 2);
        int a = clamp((int) ((((v2 * Math.abs(v2)) + 1.0d) / 2.0d) * 255));
        // Linear interpolate
        //int a = clamp((int) ((1.0d - v) * 255));
        int b = 255 - a;
        int[] r = new int[9];
        for (int i = 0; i < 9; i++)
            r[i] = ((baseA[i] * a) + (baseB[i] * b)) / 255;
        return r;
    }

    @Override
    public String uniqueToString() {
        return "H" + shift;
    }

    @Override
    public IImage process(IImage input) {
        int[] array = input.getPixels();
        for (int i = 0; i < array.length; i++)
            array[i] = processCol(array[i]);
        return GaBIEn.createImage(array, input.getWidth(), input.getHeight());
    }

    // The 'rainbow generator' makes a great test of this code
    // The matrix as given causes a reduction in value, so the code here compensates for that
    // NOTE: Similar code is used in UIColourPicker for translating RGB back to HSV
    public int processCol(int i) {
        int or = (i & 0xFF0000) >> 16;
        int og = (i & 0xFF00) >> 8;
        int ob = i & 0xFF;
        int ovalue = Math.max(Math.max(or, og), ob);
        int r = clamp(((matrix[0] * or) + (matrix[1] * og) + (matrix[2] * ob)) / 255);
        int g = clamp(((matrix[3] * or) + (matrix[4] * og) + (matrix[5] * ob)) / 255);
        int b = clamp(((matrix[6] * or) + (matrix[7] * og) + (matrix[8] * ob)) / 255);
        int value = Math.max(Math.max(r, g), b);
        if (value != 0) {
            // ovalue of 0x100 * 0x100 = 0x10000
            // divided by value 0x100 = 0x100 (no change)
            // divided by value 0x80 = 0x200 (double)
            int valueBoostFactor = (ovalue * 255) / value;
            r = clamp((r * valueBoostFactor) / 255);
            g = clamp((g * valueBoostFactor) / 255);
            b = clamp((b * valueBoostFactor) / 255);
        }
        return (i & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private int clamp(int i) {
        if (i < 0)
            return 0;
        if (i > 255)
            return 255;
        return i;
    }
}
