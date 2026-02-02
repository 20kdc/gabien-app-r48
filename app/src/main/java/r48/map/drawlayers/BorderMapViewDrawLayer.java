/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import gabien.uslx.append.Block;
import gabien.uslx.append.Rect;
import r48.R48;
import r48.map2d.MapViewDrawContext;
import r48.map2d.layers.MapViewDrawLayer;
import r48.ui.Art;

/**
 * Created on 6/16/18.
 */
public class BorderMapViewDrawLayer extends MapViewDrawLayer {
    public Rect mapBoundsPx;
    public final R48 app;
    public BorderMapViewDrawLayer(R48 app, Rect mapBoundsPx) {
        super(app.t.m.l_border);
        this.app = app;
        this.mapBoundsPx = mapBoundsPx;
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        int half = mvdc.tileSize / 2;
        try (Block blk = mvdc.igd.openScissor(mapBoundsPx.x - half, mapBoundsPx.y - half, mapBoundsPx.width + mvdc.tileSize, mapBoundsPx.height + mvdc.tileSize)) {
            for (int i = Math.max(mvdc.cam.x, mapBoundsPx.x); i < Math.min(mvdc.cam.right, mapBoundsPx.right); i += mvdc.tileSize) {
                app.a.drawSymbol(mvdc.igd, Art.Symbol.Stripes, i, mapBoundsPx.y - mvdc.tileSize, mvdc.tileSize, false, false);
                app.a.drawSymbol(mvdc.igd, Art.Symbol.Stripes, i, mapBoundsPx.bottom, mvdc.tileSize, false, false);
            }
            for (int j = Math.max(mvdc.cam.y, mapBoundsPx.y - mvdc.tileSize); j < Math.min(mvdc.cam.bottom, mapBoundsPx.right + mvdc.tileSize); j += mvdc.tileSize) {
                app.a.drawSymbol(mvdc.igd, Art.Symbol.Stripes, mapBoundsPx.x - mvdc.tileSize, j, mvdc.tileSize, false, false);
                app.a.drawSymbol(mvdc.igd, Art.Symbol.Stripes, mapBoundsPx.right, j, mvdc.tileSize, false, false);
            }
        }
    }
}
