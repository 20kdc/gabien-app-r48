/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import r48.dbs.TXDB;
import r48.map.MapViewDrawContext;
import r48.map.pass.IPassabilitySource;
import r48.ui.Art;

import java.util.Random;

/**
 * Draws a passability overlay.
 * Created on 09/06/17.
 */
public class PassabilityMapViewDrawLayer implements IMapViewDrawLayer {
    public final IPassabilitySource src;
    public final int tileSize;

    public PassabilityMapViewDrawLayer(IPassabilitySource ips, int ts) {
        src = ips;
        tileSize = ts;
    }

    @Override
    public String getName() {
        return TXDB.get("Passability Overlay");
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        if (mvdc.tileSize != tileSize)
            return;
        for (int i = mvdc.camTX; i < mvdc.camTR; i++) {
            for (int j = mvdc.camTY; j < mvdc.camTB; j++) {
                int px = i * tileSize;
                int py = j * tileSize;
                px -= mvdc.camX;
                py -= mvdc.camY;
                int flags = src.getPassability(i, j);
                if (flags == -1)
                    continue;
                // Don't actually bother to draw green.
                // This gives a much better view of what the "boundaries" are without clutter.

                int tsH = tileSize - 8;
                int tsQ = (tileSize / 2) - 4;
                if ((flags & 0x01) == 0)
                    mvdc.igd.blitImage(16, 0, 8, 8, px + tsQ, py + tsH, Art.layerTabs);
                if ((flags & 0x02) == 0)
                    mvdc.igd.blitImage(8, 0, 8, 8, px + tsH, py + tsQ, Art.layerTabs);
                if ((flags & 0x04) == 0)
                    mvdc.igd.blitImage(24, 0, 8, 8, px, py + tsQ, Art.layerTabs);
                if ((flags & 0x08) == 0)
                    mvdc.igd.blitImage(0, 0, 8, 8, px + tsQ, py, Art.layerTabs);
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
