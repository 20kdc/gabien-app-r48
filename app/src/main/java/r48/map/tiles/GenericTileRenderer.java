/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.tiles;

import gabien.render.IGrDriver;
import gabien.render.IImage;
import r48.App;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

/**
 * Useful for, say, your average Tiled output.
 * Created on May 11th 2018.
 */
public class GenericTileRenderer extends ITileRenderer {
    public final int tileStride, tileCount;
    public final IImage img;

    public GenericTileRenderer(App app, IImage image, int ts, int tw, int count) {
        super(app, ts, tw);
        img = image;
        tileStride = tw;
        tileCount = count;
    }

    @Override
    public void drawTile(int layer, int tidx, int px, int py, IGrDriver igd) {
        int tx = (tidx & 0xFFFF) % tileStride;
        int ty = (tidx & 0xFFFF) / tileStride;
        igd.blitScaledImage(tileSize * tx, tileSize * ty, tileSize, tileSize, px, py, tileSize, tileSize, img);
    }

    @Override
    public TileEditingTab[] getEditConfig(int layerIdx) {
        return new TileEditingTab[] {
                new TileEditingTab(T.m.tiles, false, false, TileEditingTab.range(0, tileCount))
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        return new AutoTileTypeField[0];
    }

    @Override
    public int getFrame() {
        return 0;
    }
}
