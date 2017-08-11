/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui;

import gabien.IGrDriver;
import gabien.IGrInDriver;
import r48.AppMain;

/**
 * Drawing functions for UI resources that vary dependent on resolution or such.
 * Created on 11/08/17.
 */
public class Art {
    // Note that X & Y are at the top-left of the tile.
    public static void drawTarget(int x, int y, int tileSize, IGrDriver igd) {
        if (tileSize <= 16) {
            igd.blitImage(16, 36, 8, 8, (x + (tileSize / 2)) - 4, (y + (tileSize / 2)) - 4, AppMain.layerTabs);
        } else {
            igd.blitImage(0, 36, 16, 16, (x + (tileSize / 2)) - 8, (y + (tileSize / 2)) - 8, AppMain.layerTabs);
        }
    }

    public static void drawZoom(IGrInDriver igd, boolean b, int x, int y, int height) {
        int m = height / 16;
        igd.clearRect(128, 128, 128, x, y, height, height);
        igd.clearRect(64, 64, 64, x + m, y + m, height - (m * 2), height - (m * 2));

        if (b)
            igd.clearRect(255, 255, 255, x + (height / 2) - m, y + (m * 2), m * 2, height - (m * 4));
        igd.clearRect(255, 255, 255, x + (m * 2), y + (height / 2) - m, height - (m * 4), m * 2);
    }
}
