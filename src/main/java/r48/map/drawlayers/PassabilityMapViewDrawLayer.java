/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.drawlayers;

import gabien.IGrDriver;
import r48.AppMain;
import r48.map.IMapViewCallbacks;

import java.util.Random;

/**
 * Draws a passability overlay.
 * Created on 09/06/17.
 */
public class PassabilityMapViewDrawLayer implements IMapViewDrawLayer {
    public final IPassabilitySource src;
    public PassabilityMapViewDrawLayer(IPassabilitySource ips) {
        src = ips;
    }

    @Override
    public String getName() {
        return "Passability Overlay";
    }

    @Override
    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        int tileSize = AppMain.stuffRenderer.tileRenderer.getTileSize();
        if (eTileSize != tileSize)
            return;
        for (int i = camTX; i < camTR; i++) {
            for (int j = camTY; j < camTB; j++) {
                int px = i * eTileSize;
                int py = j * eTileSize;
                px -= camX;
                py -= camY;
                int flags = src.getPassability(i, j);
                igd.blitImage(0, ((flags & 0x01) != 0) ? 0 : 8, 8, 8, px + 4, py, AppMain.layerTabs);
                igd.blitImage(8, ((flags & 0x02) != 0) ? 0 : 8, 8, 8, px + 8, py + 4, AppMain.layerTabs);
                igd.blitImage(16, ((flags & 0x04) != 0) ? 0 : 8, 8, 8, px + 4, py + 8, AppMain.layerTabs);
                igd.blitImage(24, ((flags & 0x08) != 0) ? 0 : 8, 8, 8, px, py + 4, AppMain.layerTabs);
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
