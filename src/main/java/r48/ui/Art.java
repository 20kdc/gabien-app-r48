/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.imagefx.HueShiftImageEffect;

/**
 * Drawing functions for UI resources that vary dependent on resolution or such.
 * Created on 11/08/17.
 */
public class Art {
    public static Rect mapIcon = new Rect(0, 52, 8, 8);
    public static Rect areaIcon = new Rect(16, 44, 8, 8);
    public static Rect hiddenTreeIcon = new Rect(24, 36, 8, 8);
    private static IImage colourPal;
    public static Rect r48ico = new Rect(37, 1, 31, 31);

    // Must be -dotLineAni
    private static final int dotLineMetric = 27;
    private static final int dotLineAni = 2;

    // Note that X & Y are at the top-left of the tile.
    public static void drawTarget(int x, int y, int tileSize, IGrDriver igd) {
        if (tileSize <= 16) {
            igd.blitImage(16, 36, 8, 8, (x + (tileSize / 2)) - 4, (y + (tileSize / 2)) - 4, AppMain.layerTabs);
        } else {
            igd.blitImage(0, 36, 16, 16, (x + (tileSize / 2)) - 8, (y + (tileSize / 2)) - 8, AppMain.layerTabs);
        }
    }

    // This controls the layout of (in particular) zoom
    public static int getZIconSize() {
        return UILabel.getRecommendedSize("", FontSizes.mapPositionTextHeight).height;
    }

    public static int getZIconMargin() {
        return FontSizes.scaleGuess(4);
    }

    public static Rect getZIconRect(boolean click, int x) {
        int zbs = getZIconSize();
        int zbm = getZIconMargin();
        int ry = (x * ((zbm * 2) + zbs));
        if (click)
            return new Rect(0, ry, zbs + (zbm * 2), zbs + (zbm * 2));
        return new Rect(zbm, zbm + ry, zbs, zbs);
    }

    public static void drawZoom(IGrDriver igd, boolean b, int x, int y, int height) {
        int m = height / 12;
        igd.clearRect(128, 128, 128, x, y, height, height);
        igd.clearRect(64, 64, 64, x + m, y + m, height - (m * 2), height - (m * 2));

        if (b)
            igd.clearRect(255, 255, 255, x + (height / 2) - m, y + (m * 2), m * 2, height - (m * 4));
        igd.clearRect(255, 255, 255, x + (m * 2), y + (height / 2) - m, height - (m * 4), m * 2);
    }

    public static void drawDragControl(IGrDriver igd, boolean select, int x, int y, int height) {
        int m = height / 12;
        int m2 = height / 4;

        int a = 255;
        int b = 64;
        if (select) {
            b = 255;
            a = 64;
        }

        igd.clearRect(128, 128, 128, x, y, height, height);
        igd.clearRect(a, a, a, x + m, y + m, height - (m * 2), height - (m * 2));

        igd.clearRect(b, b, b, x + (height / 2) - m2, y + (m * 2), m2 * 2, height - (m * 4));
        igd.clearRect(b, b, b, x + (m * 2), y + (height / 2) - m2, height - (m * 4), m2 * 2);
    }

    // this works decently even on high-DPI (with a sufficient thickness)
    public static void drawSelectionBox(int x, int y, int w, int h, int thickness, IGrDriver igd) {
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

    private static void drawDotLineV(int x, int y, int h, int f, IGrDriver igd) {
        if (h <= 0)
            return;
        while (h > dotLineMetric) {
            igd.blitImage(36, f, 1, dotLineMetric, x, y, AppMain.layerTabs);
            y += dotLineMetric;
            h -= dotLineMetric;
        }
        igd.blitImage(36, f, 1, h, x, y, AppMain.layerTabs);
    }

    private static void drawDotLineH(int x, int y, int w, int f, IGrDriver igd) {
        if (w <= 0)
            return;
        while (w > dotLineMetric) {
            igd.blitImage(36 + f, 0, dotLineMetric, 1, x, y, AppMain.layerTabs);
            x += dotLineMetric;
            w -= dotLineMetric;
        }
        igd.blitImage(36 + f, 0, w, 1, x, y, AppMain.layerTabs);
    }


    // sprite is only used for width/height.
    // It's assumed the output goes to a scaling blit function.
    public static Rect reconcile(Rect display, Rect sprite) {
        int sca = display.width / sprite.width;
        int scb = display.height / sprite.height;
        int sc = Math.min(sca, scb);
        if (sc == 0)
            sc = 1;
        return new Rect(display.x + (display.width / 2) - ((sc * sprite.width) / 2), display.y + (display.height / 2) - ((sc * sprite.height) / 2), sc * sprite.width, sc * sprite.height);
    }

    private static IImage genColourPal() {
        int[] x = new int[256 * 256];
        int idx = 0;
        for (int baseline = 0; baseline < 256; baseline++) {
            int specWid = 256 - baseline;
            for (int red = 0; red < 256; red++) {
                // At specWid 1, ered should always be 255
                // At specWid 256, ered should equal red.
                int fromRight = 255 - red;
                // do transform
                fromRight *= specWid;
                fromRight /= 256;
                // ...
                int effectiveRed = 255 - fromRight; // reverses fromRight
                int v = 0xFF000000 | (effectiveRed << 16) | (baseline << 8) | baseline;
                x[idx++] = v;
            }
        }
        return GaBIEn.createImage(x, 256, 256);
    }

    public static IImage getColourPal(int hue) {
        if (colourPal == null)
            colourPal = genColourPal();
        return AppMain.imageFXCache.process(colourPal, new HueShiftImageEffect(hue));
    }

    public static void tabWindowIcon(IGrDriver igd, int x, int y, int size) {
        int tabAscender = size / 4;
        igd.clearRect(64, 64, 128, x, y, size / 2, size - tabAscender);
        igd.clearRect(32, 32, 64, x + 1, y + 1, (size / 2) - 2, (size - tabAscender) - 2);
        igd.clearRect(64, 64, 128, x, y + tabAscender, size, size - tabAscender);
    }

    public static void windowWindowIcon(IGrDriver igd, int x, int y, int size) {
        int frameHeight = size / 4;
        igd.clearRect(128, 128, 128, x, y, size, frameHeight);
        igd.clearRect(32, 32, 32, x + 1, y + 1, size - 2, frameHeight - 2);
        igd.clearRect(96, 96, 96, x, y + frameHeight, size, size - frameHeight);
    }
}
