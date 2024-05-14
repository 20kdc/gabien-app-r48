/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.render.IGrDriver;
import gabien.ui.UIElement;
import gabien.uslx.append.Rect;
import r48.App;

/**
 * A structure that contains the subset of parameters needed for map view draw layers/etc.
 * Created on November 15, 2018.
 */
public class MapViewDrawContext {
    public final App app;
    public final int tileSize;
    public final Rect cam;
    public final Rect camT;
    public final Rect camTMargin;
    public final boolean atOrBelowHalfSize;
    public int currentLayer;
    public IMapViewCallbacks callbacks;
    public boolean debugToggle;
    public IGrDriver igd;
    // Null if the mouse doesn't exist.
    public MouseStatus mouseStatus;

    public MapViewDrawContext(App app, Rect camera, int ts, boolean atOrBelowHalfSize) {
        this.app = app;
        tileSize = ts;
        this.atOrBelowHalfSize = atOrBelowHalfSize;
        cam = camera;
        int camTR = UIElement.sensibleCellDiv(cam.x + cam.width, tileSize) + 1;
        int camTB = UIElement.sensibleCellDiv(cam.y + cam.height, tileSize) + 1;
        int camTX = UIElement.sensibleCellDiv(cam.x, tileSize);
        int camTY = UIElement.sensibleCellDiv(cam.y, tileSize);
        camT = new Rect(camTX, camTY, camTR - camTX, camTB - camTY);
        camTMargin = new Rect(camTX - 2, camTY - 2, camT.width + 4, camT.height + 4);
    }

    public void drawMouseIndicator() {
        if (mouseStatus != null)
            drawIndicator(mouseStatus.x, mouseStatus.y, IndicatorStyle.Selection);
    }
    public void drawIndicator(int tx, int ty, IndicatorStyle solid) {
        int px = tx * tileSize;
        int py = ty * tileSize;
        if (solid == IndicatorStyle.SolidBlue) {
            igd.clearRect(0, 0, 255, px, py, tileSize, tileSize);
        } else if (solid == IndicatorStyle.Target) {
            app.a.drawTarget(px, py, tileSize, igd, atOrBelowHalfSize);
        } else {
            app.a.drawSelectionBox(px, py, tileSize, tileSize, 1, igd);
        }
    }

    public enum IndicatorStyle {
        Selection,
        SolidBlue,
        Target;
    }

    public static class MouseStatus {
        public final boolean pressed;
        // In tiles.
        public final int x, y;

        public MouseStatus(boolean pressed, int x, int y) {
            this.pressed = pressed;
            this.x = x;
            this.y = y;
        }
    }
}
