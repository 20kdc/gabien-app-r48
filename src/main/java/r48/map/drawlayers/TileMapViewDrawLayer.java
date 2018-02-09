/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import gabien.IGrDriver;
import gabien.ui.UILabel;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;
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

    public boolean shouldDraw(int x, int y, int layer, short value) {
        return true;
    }

    public boolean shouldDrawRow(int y, int layer) {
        return true;
    }

    @Override
    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        for (int j = camTY; j < camTB; j++) {
            if (j < 0)
                continue;
            if (j >= targetTable.height)
                continue;
            if (!shouldDrawRow(j, currentLayer))
                continue;
            for (int i = camTX; i < camTR; i++) {
                if (i < 0)
                    continue;
                if (i >= targetTable.width)
                    continue;
                int px = i * eTileSize;
                int py = j * eTileSize;
                px -= camX;
                py -= camY;
                if (tileLayer == -1) {
                    for (int k = 0; k < targetTable.planeCount; k++)
                        tileDrawIntern(k, mouseXT, mouseYT, currentLayer, callbacks, debug, igd, i, j, px, py);
                } else {
                    tileDrawIntern(tileLayer, mouseXT, mouseYT, currentLayer, callbacks, debug, igd, i, j, px, py);
                }
            }
        }
    }

    private void tileDrawIntern(int tdi, int mouseXT, int mouseYT, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd, int i, int j, int px, int py) {
        short tidx = targetTable.getTiletype(i, j, tdi);
        if (callbacks != null)
            tidx = callbacks.shouldDrawAt(mouseXT, mouseYT, i, j, tidx, tdi, currentLayer);
        if (shouldDraw(i, j, tdi, tidx)) {
            if (debug) {
                String t = Integer.toString(tidx, 16);
                UILabel.drawString(igd, px, py + (tdi * UIMapView.mapDebugTextHeight), t, false, UIMapView.mapDebugTextHeight);
            } else {
                tr.drawTile(tdi, tidx, px, py, igd, 1);
            }
        }
    }
}
