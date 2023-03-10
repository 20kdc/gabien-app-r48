/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.App;
import r48.map.MapViewDrawContext;

/**
 * Since this is used everywhere, it's a good indication of where the "global layers" end.
 * Unknown creation date.
 */
public class GridMapViewDrawLayer extends App.Svc implements IMapViewDrawLayer {

    public GridMapViewDrawLayer(App app) {
        super(app);
    }

    @Override
    public String getName() {
        return T.z.l215;
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
