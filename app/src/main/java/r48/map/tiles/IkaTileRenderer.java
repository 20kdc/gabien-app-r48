/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.tiles;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import r48.dbs.TXDB;
import r48.map.imaging.IImageLoader;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

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
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int spriteScale, boolean editor) {
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
        IImage i = imageLoader.getImage("Prt" + blockTypes[plane], false);
        int ets = getTileSize();
        if (plane != 6) {
            igd.blitScaledImage(ets * block, 0, ets, ets, px, py, ets * spriteScale, ets * spriteScale, i);
        } else {
            // fun fact, this was probably the most loved feature of IkachanMapEdit.
            // I would be in for a *lynching* if I got rid of it.
            drawPrtDir(getFrame(), block, ets, px, py, spriteScale, i, igd);
        }
    }

    public static void drawPrtDir(int frame, int block, int ets, int px, int py, int spriteScale, IImage i, IGrDriver igd) {
        if (block == 0)
            igd.blitScaledImage(frame, 0, ets, ets, px, py, ets * spriteScale, ets * spriteScale, i);
        if (block == 1)
            igd.blitScaledImage(ets - frame, 0, ets, ets, px, py, ets * spriteScale, ets * spriteScale, i);
        if (block == 2)
            igd.blitScaledImage(0, frame, ets, ets, px, py, ets * spriteScale, ets * spriteScale, i);
        if (block == 3)
            igd.blitScaledImage(0, ets - frame, ets, ets, px, py, ets * spriteScale, ets * spriteScale, i);
    }

    @Override
    public TileEditingTab[] getEditConfig(int layerIdx) {
        return new TileEditingTab[] {
                new TileEditingTab(TXDB.get("Tiles"), false, TileEditingTab.range(0, 256))
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        // simple enough: Ikachan doesn't have ATs.
        return new AutoTileTypeField[0];
    }

    @Override
    public int getFrame() {
        double time = GaBIEn.getTime();
        return (int) ((time - Math.floor(time)) * 64) % 16;
    }

    @Override
    public int getRecommendedWidth() {
        return 16;
    }
}
