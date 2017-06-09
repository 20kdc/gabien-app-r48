/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.drawlayers;

import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyTable;
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
        return "Tile Layer " + tileLayer;
    }

    @Override
    public void draw(int camX, int camY, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        int camTR = UIElement.sensibleCellDiv((camX + igd.getWidth()), eTileSize) + 1;
        int camTB = UIElement.sensibleCellDiv((camY + igd.getHeight()), eTileSize) + 1;
        int camTX = UIElement.sensibleCellDiv(camX, eTileSize);
        int camTY = UIElement.sensibleCellDiv(camY, eTileSize);
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
                    if (i == mouseXT)
                        if (j == mouseYT)
                            if (callbacks != null)
                                tidx = callbacks.shouldDrawAtCursor(tidx, tileLayer, currentLayer);
                    AppMain.stuffRenderer.tileRenderer.drawTile(tileLayer, tidx, px, py, igd, eTileSize);
                }
            }
        }
    }
}
