/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import r48.dbs.TXDB;
import r48.map.MapViewDrawContext;

/**
 * Since this is used everywhere, it's a good indication of where the "global layers" end.
 * Unknown creation date.
 */
public class GridMapViewDrawLayer implements IMapViewDrawLayer {
    @Override
    public String getName() {
        return TXDB.get("RM-Style Grid Overlay");
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        for (int i = mvdc.camT.x; i < mvdc.camT.x + mvdc.camT.width; i++) {
            int a = (i * mvdc.tileSize) + (mvdc.tileSize - 1);
            mvdc.igd.clearRect(0, 0, 0, a, mvdc.cam.y, 1, mvdc.igd.getHeight());
        }
        for (int i = mvdc.camT.y; i < mvdc.camT.y + mvdc.camT.height; i++) {
            int a = (i * mvdc.tileSize) + (mvdc.tileSize - 1);
            mvdc.igd.clearRect(0, 0, 0, mvdc.cam.x, a, mvdc.igd.getWidth(), 1);
        }
    }
}
