/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.uslx.append.Rect;
import r48.App;
import r48.io.data.IRIO;
import r48.render2d.MapViewDrawContext;

/**
 * A structure that contains the subset of parameters needed for map view draw layers/etc.
 * Created on November 15, 2018.
 */
public class AppMapViewDrawContext extends MapViewDrawContext {
    public final App app;
    public AppMapViewDrawContext(App app, Rect camera, int ts, boolean atOrBelowHalfSize) {
        super(ts, camera, atOrBelowHalfSize);
        this.app = app;
    }

    @Override
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

    @Override
    public boolean currentlyOpenInEditor(IRIO evI) {
        return app.ui.currentlyOpenInEditor(evI);
    }
}
