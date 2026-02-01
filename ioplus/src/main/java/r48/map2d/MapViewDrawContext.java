/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map2d;

import gabien.render.IGrDriver;
import gabien.uslx.append.MathsX;
import gabien.uslx.append.Rect;
import r48.io.data.IRIO;

/**
 * Pulled out of what became AppMapViewDrawContext, 1st February 2026.
 */
public abstract class MapViewDrawContext {
    public final int tileSize;
    public final Rect cam;
    public final Rect camT;
    public final Rect camTMargin;
    public final boolean atOrBelowHalfSize;
    public int currentLayer;
    public IMapViewCallbacks callbacks;
    public boolean debugToggle;
    public IGrDriver igd;
    public MouseStatus mouseStatus;

    public MapViewDrawContext(int tileSize, Rect cam, boolean atOrBelowHalfSize) {
        super();

        int camTR = MathsX.seqDiv(cam.x + cam.width, tileSize) + 1;
        int camTB = MathsX.seqDiv(cam.y + cam.height, tileSize) + 1;
        int camTX = MathsX.seqDiv(cam.x, tileSize);
        int camTY = MathsX.seqDiv(cam.y, tileSize);

        this.tileSize = tileSize;
        this.cam = cam;
        this.camT = new Rect(camTX, camTY, camTR - camTX, camTB - camTY);
        this.camTMargin = new Rect(camTX - 2, camTY - 2, camT.width + 4, camT.height + 4);
        this.atOrBelowHalfSize = atOrBelowHalfSize;
    }

    public abstract void drawIndicator(int tx, int ty, IndicatorStyle solid);

    public abstract boolean currentlyOpenInEditor(IRIO evI);

    public void drawMouseIndicator() {
        if (mouseStatus != null)
            drawIndicator(mouseStatus.x, mouseStatus.y, IndicatorStyle.Selection);
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