/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.App;
import r48.map.MapViewDrawContext;
import r48.ui.Art;

/**
 * Created on 6/16/18.
 */
public class BorderMapViewDrawLayer extends App.Svc implements IMapViewDrawLayer {
    public int width, height;
    public BorderMapViewDrawLayer(App app, int w, int h) {
        super(app);
        width = w;
        height = h;
    }

    @Override
    public String getName() {
        return app.ts("Map Border");
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        for (int i = Math.max(mvdc.camT.x, 0); i < Math.min(mvdc.camT.x + mvdc.camT.width, width); i++) {
            Art.drawSymbol(mvdc.igd, Art.Symbol.Stripes, i * mvdc.tileSize, -mvdc.tileSize, mvdc.tileSize, false, false);
            Art.drawSymbol(mvdc.igd, Art.Symbol.Stripes, i * mvdc.tileSize, height * mvdc.tileSize, mvdc.tileSize, false, false);
        }
        for (int j = Math.max(mvdc.camT.y, -1); j < Math.min(mvdc.camT.y + mvdc.camT.height, height + 1); j++) {
            Art.drawSymbol(mvdc.igd, Art.Symbol.Stripes, -mvdc.tileSize, j * mvdc.tileSize, mvdc.tileSize, false, false);
            Art.drawSymbol(mvdc.igd, Art.Symbol.Stripes, width * mvdc.tileSize, j * mvdc.tileSize, mvdc.tileSize, false, false);
        }
    }
}
