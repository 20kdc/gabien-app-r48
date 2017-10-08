/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui;

import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.Rect;
import r48.AppMain;
import r48.FontSizes;

/**
 * Drawing functions for UI resources that vary dependent on resolution or such.
 * Created on 11/08/17.
 */
public class Art {
    public static Rect mapIcon = new Rect(0, 52, 8, 8);
    public static Rect areaIcon = new Rect(16, 44, 8, 8);

    // Note that X & Y are at the top-left of the tile.
    public static void drawTarget(int x, int y, int tileSize, IGrDriver igd) {
        if (tileSize <= 16) {
            igd.blitImage(16, 36, 8, 8, (x + (tileSize / 2)) - 4, (y + (tileSize / 2)) - 4, AppMain.layerTabs);
        } else {
            igd.blitImage(0, 36, 16, 16, (x + (tileSize / 2)) - 8, (y + (tileSize / 2)) - 8, AppMain.layerTabs);
        }
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

    public static void drawSelectionBox(int x, int y, int w, int h, IGrDriver igd) {
        int thickness = FontSizes.getSpriteScale();
        while (thickness > 0) {
            drawDotLineV(x, y, h, igd);
            drawDotLineV(x + (w - 1), y, h, igd);
            drawDotLineH(x, y, w, igd);
            drawDotLineH(x, y + (h - 1), w, igd);
            thickness--;
            x++;
            y++;
            w -= 2;
            h -= 2;
        }
    }

    private static void drawDotLineV(int x, int y, int h, IGrDriver igd) {
        while (h > 32) {
            igd.blitImage(36, 0, 1, 32, x, y, AppMain.layerTabs);
            y += 32;
            h -= 32;
        }
        igd.blitImage(36, 0, 1, h, x, y, AppMain.layerTabs);
    }

    private static void drawDotLineH(int x, int y, int w, IGrDriver igd) {
        while (w > 32) {
            igd.blitImage(36, 0, 32, 1, x, y, AppMain.layerTabs);
            x += 32;
            w -= 32;
        }
        igd.blitImage(36, 0, w, 1, x, y, AppMain.layerTabs);
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
}
