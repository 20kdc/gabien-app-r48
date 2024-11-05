/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.App;
import r48.map.MapViewDrawContext;
import r48.map.pass.IPassabilitySource;

import java.util.Random;

/**
 * Draws a passability overlay.
 * Created on 09/06/17.
 */
public class PassabilityMapViewDrawLayer extends App.Svc implements IMapViewDrawLayer {
    public final IPassabilitySource src;
    public final int tileSize;

    public PassabilityMapViewDrawLayer(App app, IPassabilitySource ips, int ts) {
        super(app);
        src = ips;
        tileSize = ts;
    }

    @Override
    public String getName() {
        return T.m.l_passability;
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        if (mvdc.tileSize != tileSize)
            return;
        for (int i = mvdc.camT.x; i < mvdc.camT.x + mvdc.camT.width; i++) {
            for (int j = mvdc.camT.y; j < mvdc.camT.y + mvdc.camT.height; j++) {
                int px = i * tileSize;
                int py = j * tileSize;
                int flags = src.getPassability(i, j);
                if (flags == -1)
                    continue;
                // Don't actually bother to draw green.
                // This gives a much better view of what the "boundaries" are without clutter.

                int tsEnd = tileSize - 8;
                int tsMid = (tileSize / 2) - 4;
                if ((flags & IPassabilitySource.PASS_DOWN) == 0)
                    mvdc.igd.blitImage(16, 0, 8, 8, px + tsMid, py + tsEnd, app.a.layerTabs);
                if ((flags & IPassabilitySource.PASS_RIGHT) == 0)
                    mvdc.igd.blitImage(8, 0, 8, 8, px + tsEnd, py + tsMid, app.a.layerTabs);
                if ((flags & IPassabilitySource.PASS_LEFT) == 0)
                    mvdc.igd.blitImage(24, 0, 8, 8, px, py + tsMid, app.a.layerTabs);
                if ((flags & IPassabilitySource.PASS_UP) == 0)
                    mvdc.igd.blitImage(0, 0, 8, 8, px + tsMid, py, app.a.layerTabs);
            }
        }
    }

    public static class TestPassabilitySource implements IPassabilitySource {
        public Random r = new Random();

        @Override
        public int getPassability(int x, int y) {
            return r.nextInt(16);
        }
    }
}
