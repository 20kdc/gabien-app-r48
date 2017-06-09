/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.drawlayers;

import gabien.IGrDriver;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.map.IMapViewCallbacks;
import r48.map.tiles.ITileRenderer;

/**
 * Created on 09/06/17.
 */
public class R2kTileMapViewDrawLayer implements IMapViewDrawLayer {
    public RubyTable targetTable;
    public boolean upper;
    public int layer;
    public RubyIO tileset;
    public R2kTileMapViewDrawLayer(RubyTable tbl, int targLayer, boolean targUpper, RubyIO ts) {
        targetTable = tbl;
        upper = targUpper;
        layer = targLayer;
        tileset = ts;
    }

    @Override
    public String getName() {
        return "Tile Layer " + layer + " (" + (upper ? "Upper" : "Lower") + ")";
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
                    String t = Integer.toString(targetTable.getTiletype(i, j, layer), 16);
                    UILabel.drawString(igd, px, py + (layer * FontSizes.mapDebugTextHeight), t, false, FontSizes.mapDebugTextHeight);
                } else {
                    short tidx = targetTable.getTiletype(i, j, layer);
                    if (i == mouseXT)
                        if (j == mouseYT)
                            if (callbacks != null)
                                tidx = callbacks.shouldDrawAtCursor(tidx, layer, currentLayer);
                    // Work out upper/lower.
                    boolean checkUpper = false;
                    checkUpper |= checkUpperRange(tidx, 0, 1000, 0, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 1000, 2000, 1, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 2000, 3000, 2, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 3000, 3050, 3, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 3050, 3100, 4, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 3100, 4000, 5, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4000, 4050, 6, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4050, 4100, 7, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4100, 4150, 8, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4150, 4200, 9, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4200, 4250, 10, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4250, 4300, 11, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4300, 4350, 12, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4350, 4400, 13, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4400, 4450, 14, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4450, 4500, 15, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4500, 4550, 16, "@lowpass_data");
                    checkUpper |= checkUpperRange(tidx, 4550, 4600, 17, "@lowpass_data");
                    for (int k = 0; k < 144; k++)
                        checkUpper |= checkUpperRange(tidx, 5000 + k, 5000 + k + 1, 18 + k, "@lowpass_data");
                    for (int k = 0; k < 144; k++)
                        checkUpper |= checkUpperRange(tidx, 10000 + k, 10000 + k + 1, k, "@highpass_data");
                    if (checkUpper == upper)
                        AppMain.stuffRenderer.tileRenderer.drawTile(layer, tidx, px, py, igd, eTileSize);
                }
            }
        }
    }

    private boolean checkUpperRange(short tidx, int rangeS, int rangeE, int group, String s) {
        if (tidx >= rangeS)
            if (tidx < rangeE) {
                RubyTable rt = new RubyTable(tileset.getInstVarBySymbol(s).userVal);
                return (rt.getTiletype(group, 0, 0) & 0x10) != 0;
            }
        return false;
    }
}
