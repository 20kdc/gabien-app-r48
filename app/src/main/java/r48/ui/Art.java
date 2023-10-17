/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.GaBIEn;
import gabien.GaBIEnUI;
import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.ui.UIBorderedElement;
import gabien.uslx.append.Rect;
import r48.App;
import r48.imagefx.HueShiftImageEffect;

/**
 * Drawing functions for UI resources that vary dependent on resolution or such.
 * Created on 11/08/17.
 */
public class Art {

    // Images
    public static IImage layerTabs = GaBIEn.getImageCKEx("layertab.png", false, true, 255, 0, 255);
    public static IImage noMap = GaBIEn.getImageCKEx("nomad.png", false, true, 0, 0, 0);
    public static IImage symbolic = GaBIEn.getImageCKEx("symbolic.png", false, true, 0, 0, 0);

    // Generated Images
    private static IImage colourPal, rainbow;

    // Must be -dotLineAni
    private static final int dotLineMetric = 27;
    private static final int dotLineAni = 2;

    public Art() {
    }

    // This controls the layout of (in particular) zoom
    public static int getZIconSize(App app) {
        return UIBorderedElement.getBorderedTextHeight(GaBIEnUI.sysThemeRoot.getTheme(), app.f.mapPositionTH);
    }

    public static int getZIconMargin(App app) {
        return app.f.scaleGuess(4);
    }

    public static Rect getZIconRect(App app, boolean click, int x) {
        int zbs = getZIconSize(app);
        int zbm = getZIconMargin(app);
        int ry = (x * ((zbm * 2) + zbs));
        if (click)
            return new Rect(0, ry, zbs + (zbm * 2), zbs + (zbm * 2));
        return new Rect(zbm, zbm + ry, zbs, zbs);
    }

    public static void drawZoom(IGrDriver igd, boolean b, int x, int y, int size) {
        int m = size / 12;
        if (m < 1)
            m = 1;
        igd.clearRect(128, 128, 128, x, y, size, size);
        igd.clearRect(64, 64, 64, x + m, y + m, size - (m * 2), size - (m * 2));

        int pa = (((size + 1) / 2) + m) - ((size / 2) - m);
        if (b)
            igd.clearRect(255, 255, 255, x + (size / 2) - m, y + (m * 2), pa, size - (m * 4));
        igd.clearRect(255, 255, 255, x + (m * 2), y + (size / 2) - m, size - (m * 4), pa);
    }

    public static void drawDragControl(IGrDriver igd, boolean select, int x, int y, int size) {
        int m = size / 12;

        int a = 64;
        int b = 128;
        if (select) {
            a = 128;
            b = 255;
        }

        igd.clearRect(b, b, b, x, y, size, size);
        igd.clearRect(a, a, a, x + m, y + m, size - (m * 2), size - (m * 2));

        //igd.clearRect(b, b, b, x + (size / 2) - m2, y + (m * 2), m2 * 2, size - (m * 4));
        //igd.clearRect(b, b, b, x + (m * 2), y + (size / 2) - m2, size - (m * 4), m2 * 2);

        int xd = m;
        Art.drawSymbol(igd, Art.Symbol.Camera, x + m + xd, y + m + xd, size - ((m + xd) * 2), false, select);
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
            igd.blitImage(32, f, 1, dotLineMetric, x, y, layerTabs);
            y += dotLineMetric;
            h -= dotLineMetric;
        }
        igd.blitImage(32, f, 1, h, x, y, layerTabs);
    }

    private static void drawDotLineH(int x, int y, int w, int f, IGrDriver igd) {
        if (w <= 0)
            return;
        while (w > dotLineMetric) {
            igd.blitImage(32 + f, 0, dotLineMetric, 1, x, y, layerTabs);
            x += dotLineMetric;
            w -= dotLineMetric;
        }
        igd.blitImage(32 + f, 0, w, 1, x, y, layerTabs);
    }

    private static IImage genColourPal() {
        int[] img = new int[256 * 256];
        int idx = 0;
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                // At X0, R = V
                // At X255, R = S * V
                int r = 255 - y;
                int g = ((255 - x) * r) / 255;
                int v = 0xFF000000 | (r << 16) | (g << 8) | g;
                img[idx++] = v;
            }
        }
        return GaBIEn.createImage(img, 256, 256);
    }

    public static IImage getColourPal(App app, int hue) {
        if (colourPal == null)
            colourPal = genColourPal();
        return app.ui.imageFXCache.process(colourPal, new HueShiftImageEffect(hue));
    }

    public static int getRainbowHue(int x) {
        return -((x * 359) / 255);
    }

    private static IImage genRainbow() {
        int[] img = new int[256];
        for (int x = 0; x < 256; x++)
            img[x] = new HueShiftImageEffect(getRainbowHue(x)).processCol(0xFFFF0000);
        return GaBIEn.createImage(img, 256, 1);
    }

    public static IImage getRainbow() {
        if (rainbow == null)
            rainbow = genRainbow();
        return rainbow;
    }

    public static void tabWindowIcon(IGrDriver igd, int x, int y, int size) {
        int tabAscender = size / 4;
        int tabMargin = Math.max(1, size / 8);
        igd.clearRect(64, 64, 128, x, y, size / 2, size - tabAscender);
        igd.clearRect(32, 32, 64, x + tabMargin, y + tabMargin, (size / 2) - (tabMargin * 2), (size - tabAscender) - (tabMargin * 2));
        igd.clearRect(64, 64, 128, x, y + tabAscender, size, size - tabAscender);
    }

    public static void windowWindowIcon(IGrDriver igd, int x, int y, int size) {
        int frameHeight = size / 3;
        int tMargin = Math.max(1, size / 8);
        igd.clearRect(128, 128, 128, x, y, size, frameHeight);
        igd.clearRect(32, 32, 32, x + tMargin, y + tMargin, size - (tMargin * 2), frameHeight - (tMargin * 2));
        igd.clearRect(96, 96, 96, x, y + frameHeight, size, size - frameHeight);
    }

    // For ID reference, ignore the left 20px of symbolic.png, and look at the 16x16-sprite grid.
    public static void drawSymbol(IGrDriver igd, Symbol symbol, int x, int y, int size, boolean force, boolean background) {
        drawSymbol(igd, symbol, x, y, size, size, force, background);
    }

    public static void drawSymbol(IGrDriver igd, Symbol symbol, int x, int y, int sizeW, int sizeH, boolean force, boolean background) {
        if (background)
            igd.clearRect(0, 0, 0, x, y, sizeW, sizeH);
        // NOTE: Symbols are drawn at one of the following sizes:
        // 4px
        // 8px
        // 16px
        // 16px * X
        // Symbols do NOT get scaled to non-integer sizes, unless size < 4 or force.
        int size = Math.min(sizeW, sizeH);
        if (size <= 4) {
            drawSymbol4px(igd, symbol.ordinal(), x, y, sizeW, sizeH);
        } else if (size < 8) {
            int mX = (sizeW - 4) / 2;
            int mY = (sizeH - 4) / 2;
            if (force) {
                mX = 0;
                mY = 0;
            }
            drawSymbol4px(igd, symbol.ordinal(), x + mX, y + mY, force ? sizeW : 4, force ? sizeH : 4);
        } else if (size < 16) {
            int mX = (sizeW - 8) / 2;
            int mY = (sizeH - 8) / 2;
            if (force) {
                mX = 0;
                mY = 0;
            }
            drawSymbol8px(igd, symbol.ordinal(), x + mX, y + mY, force ? sizeW : 8, force ? sizeH : 8);
        } else {
            int ms = 16 * (size / 16);
            int mX = (sizeW - ms) / 2;
            int mY = (sizeH - ms) / 2;
            if (force) {
                mX = 0;
                mY = 0;
            }
            drawSymbol16px(igd, symbol.ordinal(), x + mX, y + mY, force ? sizeW : ms, force ? sizeH : ms);
        }
    }

    private static void drawSymbol4px(IGrDriver igd, int symbol, int x, int y, int sizeW, int sizeH) {
        igd.blitScaledImage(0, symbol * 4, 4, 4, x, y, sizeW, sizeH, symbolic);
    }

    private static void drawSymbol8px(IGrDriver igd, int symbol, int x, int y, int sizeW, int sizeH) {
        int page = symbol / 4;
        symbol %= 4;
        int subpage = symbol / 2;
        symbol %= 2;
        subpage += page * 2;
        igd.blitScaledImage(4 + (symbol * 8), subpage * 8, 8, 8, x, y, sizeW, sizeH, symbolic);
    }

    private static void drawSymbol16px(IGrDriver igd, int symbol, int x, int y, int sizeW, int sizeH) {
        int page = symbol / 4;
        symbol %= 4;
        igd.blitScaledImage(20 + (symbol * 16), page * 16, 16, 16, x, y, sizeW, sizeH, symbolic);
    }

    // Basically a "compatibility" function. Tries to draw an appropriate event-point image given a tile size and a top-left position.
    public static void drawTarget(int px, int py, int tileSize, IGrDriver igd) {
        Art.drawSymbol(igd, Art.Symbol.Target, px + (tileSize / 4), py + (tileSize / 4), tileSize / 2, false, false);
    }

    public enum Symbol {
        // NOTE! If you can't tell the difference in grayscale, it's too alike.
        Map, BarV, BarVBranchR, BarCornerUR,
        Target, Area, Expandable, Play,
        Loop, XRed, Div3, Div2,
        Save, Copy, PasteDNU, CloneFrame,
        // CopyGroup/PasteGroup have multiple items, while Copy/Paste show blank boxes/paper.
        Inspect, Back, CopyRectangle, PasteRectangle,
        // "Rectangle" is white & dashed, area is solid-skyblue,
        //  XWhite is white and smaller than XRed.
        Rectangle, Stripes, XWhite, Pencil,
        Camera, New, Folder, Keyframe,
        Tween, SaveDisabled, Forward, Eyedropper,
        // "Fill" is for a flood fill, so it's a bucket
        Line, Fill
    }
}
