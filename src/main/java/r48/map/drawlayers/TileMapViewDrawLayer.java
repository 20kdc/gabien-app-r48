/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.drawlayers;

import gabien.IGrDriver;
import gabien.ui.UILabel;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.map.tiles.ITileRenderer;

/**
 * Created on 08/06/17.
 */
public class TileMapViewDrawLayer implements IMapViewDrawLayer {
    public final RubyTable targetTable;
    public final int tileLayer;
    public final ITileRenderer tr;

    public TileMapViewDrawLayer(RubyTable table, int i, ITileRenderer itr) {
        targetTable = table;
        tileLayer = i;
        tr = itr;
    }

    @Override
    public String getName() {
        return FormatSyntax.formatExtended(TXDB.get("Tile Layer #A"), new RubyIO().setFX(tileLayer));
    }

    @Override
    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        for (int i = camTX; i < camTR; i++) {
            if (i < 0)
                continue;
            if (i >= targetTable.width)
                continue;
            for (int j = camTY; j < camTB; j++) {
                if (j < 0)
                    continue;
                if (j >= targetTable.height)
                    continue;
                int px = i * eTileSize;
                int py = j * eTileSize;
                px -= camX;
                py -= camY;
                // 5, 26-29: cafe main bar. In DEBUG, shows as 255, 25d, 25d, ???, I think.
                // 1c8 >> 3 == 39.
                // 39 in binary is 00111001.
                // Possible offset of 1?
                if (debug) {
                    String t = Integer.toString(targetTable.getTiletype(i, j, tileLayer), 16);
                    UILabel.drawString(igd, px, py + (tileLayer * FontSizes.mapDebugTextHeight), t, false, FontSizes.mapDebugTextHeight);
                } else {
                    short tidx = targetTable.getTiletype(i, j, tileLayer);
                    if (callbacks != null)
                        tidx = callbacks.shouldDrawAt(mouseXT, mouseYT, i, j, tidx, tileLayer, currentLayer);
                    tr.drawTile(tileLayer, tidx, px, py, igd, eTileSize);
                }
            }
        }
    }
}
