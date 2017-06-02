/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map.tiles;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import r48.map.imaging.IImageLoader;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

/**
 * Created on 1/27/17.
 */
public class IkaTileRenderer implements ITileRenderer {

    private final IImageLoader imageLoader;

    public IkaTileRenderer(IImageLoader il) {
        imageLoader = il;
    }

    @Override
    public int getTileSize() {
        return 16;
    }

    @Override
    public int[] tileLayerDrawOrder() {
        // Standard draw order.
        return new int[] {0};
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrInDriver igd, int ets) {
        String[] blockTypes = new String[16];
        blockTypes[2] = "filt";
        blockTypes[4] = "Item";
        blockTypes[6] = "Dir";
        blockTypes[8] = "Block";
        blockTypes[10] = "Dmg";
        blockTypes[14] = "Snack";
        int plane = (tidx & 0xFFF0) >> 4;
        int block = tidx & 0xF;
        if (plane < 0)
            return;
        if (plane > 15)
            return;
        if (blockTypes[plane] == null)
            return;
        IGrInDriver.IImage i = imageLoader.getImage("Prt" + blockTypes[plane], false);
        if (plane != 6) {
            igd.blitBCKImage(16 * block, 0, 16, 16, px, py, i);
        } else {
            // fun fact, this was probably the most loved feature of IkachanMapEdit.
            // I would be in for a *lynching* if I got rid of it.
            double time = GaBIEn.getTime();
            if (block == 0)
                igd.blitBCKImage((int) ((time - Math.floor(time)) * 64) % 16, 0, 16,
                        16, px, py, i);
            if (block == 1)
                igd.blitBCKImage(
                        ((int) (1.0 - (time - Math.floor(time)) * 64) % 16) + 16,
                        0, 16, 16, px, py, i);
            if (block == 2)
                igd.blitBCKImage(0, (int) ((time - Math.floor(time)) * 64) % 16, 16,
                        16, px, py, i);
            if (block == 3)
                igd.blitBCKImage(0,
                        ((int) (1.0 - (time - Math.floor(time)) * 64) % 16) + 16,
                        16, 16, px, py, i);
        }
    }

    @Override
    public String getPanorama() {
        return "Back";
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        return new UITileGrid[] {new UITileGrid(mv, 0, 256, false, 0)};
    }

    @Override
    public String[] getPlaneNames(int layer) {
        return new String[] {
                "Tiles"
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        // simple enough: Ikachan doesn't have ATs.
        return new AutoTileTypeField[0];
    }
}
