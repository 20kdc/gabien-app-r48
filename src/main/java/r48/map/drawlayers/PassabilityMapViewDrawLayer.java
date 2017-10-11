/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import gabien.IGrDriver;
import r48.AppMain;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.map.pass.IPassabilitySource;

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
    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        if (eTileSize != tileSize)
            return;
        for (int i = camTX; i < camTR; i++) {
            for (int j = camTY; j < camTB; j++) {
                int px = i * eTileSize;
                int py = j * eTileSize;
                px -= camX;
                py -= camY;
                int flags = src.getPassability(i, j);
                if (flags == -1)
                    continue;
                // Don't actually bother to draw green.
                // This gives a much better view of what the "boundaries" are without clutter.

                int tsH = tileSize - 8;
                int tsQ = (tileSize / 2) - 4;
                if ((flags & 0x01) == 0)
                    igd.blitImage(16, 0, 8, 8, px + tsQ, py + tsH, AppMain.layerTabs);
                if ((flags & 0x02) == 0)
                    igd.blitImage(8, 0, 8, 8, px + tsH, py + tsQ, AppMain.layerTabs);
                if ((flags & 0x04) == 0)
                    igd.blitImage(24, 0, 8, 8, px, py + tsQ, AppMain.layerTabs);
                if ((flags & 0x08) == 0)
                    igd.blitImage(0, 0, 8, 8, px + tsQ, py, AppMain.layerTabs);
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
