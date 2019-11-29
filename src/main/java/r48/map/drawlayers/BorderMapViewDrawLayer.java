/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import r48.dbs.TXDB;
import r48.map.MapViewDrawContext;
import r48.ui.Art;

/**
 * Created on 6/16/18.
 */
public class BorderMapViewDrawLayer implements IMapViewDrawLayer {
    public int width, height;
    public BorderMapViewDrawLayer(int w, int h) {
        width = w;
        height = h;
    }

    @Override
    public String getName() {
        return TXDB.get("Map Border");
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
