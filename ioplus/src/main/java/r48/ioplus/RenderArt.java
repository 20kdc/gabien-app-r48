/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ioplus;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.render.IImage;

/**
 * Extraction of non-UI-involving Art stuff.
 * Extracted 1st February 2026.
 */
public class RenderArt {
    public IImage layerTabs = GaBIEn.getImageEx("layertab.png", false, true);
    public IImage symbolic16 = GaBIEn.getImageEx("symbolic16.png", false, true);

    // Must be -dotLineAni
    private static final int dotLineMetric = 27;
    private static final int dotLineAni = 2;

    // this works decently even on high-DPI (with a sufficient thickness)
    public void drawSelectionBox(int x, int y, int w, int h, int thickness, IGrDriver igd) {
        int f = ((int) (GaBIEn.getTime() * (dotLineAni + 1))) % (dotLineAni + 1);
        while (thickness > 0) {
            drawDotLineV(x, y, h, f, igd);
            drawDotLineV(x + (w - 1), y, h, dotLineAni - f, igd);
            drawDotLineH(x, y, w, dotLineAni - f, igd);
            drawDotLineH(x, y + (h - 1), w, f, igd);
            thickness--;
            x++;
            y++;
            w -= 2;
            h -= 2;
        }
    }

    private void drawDotLineV(int x, int y, int h, int f, IGrDriver igd) {
        if (h <= 0)
            return;
        while (h > dotLineMetric) {
            igd.blitImage(32, f, 1, dotLineMetric, x, y, layerTabs);
            y += dotLineMetric;
            h -= dotLineMetric;
        }
        igd.blitImage(32, f, 1, h, x, y, layerTabs);
    }

    private void drawDotLineH(int x, int y, int w, int f, IGrDriver igd) {
        if (w <= 0)
            return;
        while (w > dotLineMetric) {
            igd.blitImage(32 + f, 0, dotLineMetric, 1, x, y, layerTabs);
            x += dotLineMetric;
            w -= dotLineMetric;
        }
        igd.blitImage(32 + f, 0, w, 1, x, y, layerTabs);
    }
}
