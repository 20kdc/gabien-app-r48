/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import gabien.FontManager;
import gabien.IGrDriver;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;
import r48.map.tiles.ITileRenderer;

/**
 * Created on 08/06/17.
 */
public class TileMapViewDrawLayer implements IMapViewDrawLayer {
    public final RubyTable targetTable;
    public final int tileLayer;
    public final ITileRenderer tr;
    public final String name;

    public TileMapViewDrawLayer(RubyTable table, int i, ITileRenderer itr) {
        targetTable = table;
        tileLayer = i;
        tr = itr;
        name = FormatSyntax.formatExtended(TXDB.get("Tile Layer #A"), new RubyIO().setFX(tileLayer));
    }

    public TileMapViewDrawLayer(RubyTable table, int i, ITileRenderer itr, String post) {
        targetTable = table;
        tileLayer = i;
        tr = itr;
        name = post;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean shouldDraw(int x, int y, int layer, short value) {
        return true;
    }

    public boolean shouldDrawRow(int y, int layer) {
        return true;
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        for (int j = mvdc.camT.y; j < mvdc.camT.y + mvdc.camT.height; j++) {
            if (j < 0)
                continue;
            if (j >= targetTable.height)
                continue;
            if (!shouldDrawRow(j, mvdc.currentLayer))
                continue;
            for (int i = mvdc.camT.x; i < mvdc.camT.x + mvdc.camT.width; i++) {
                if (i < 0)
                    continue;
                if (i >= targetTable.width)
                    continue;
                int px = i * mvdc.tileSize;
                int py = j * mvdc.tileSize;
                if (tileLayer == -1) {
                    for (int k = 0; k < targetTable.planeCount; k++)
                        tileDrawIntern(k, mvdc.mouseStatus, mvdc.currentLayer, mvdc.callbacks, mvdc.debugToggle, mvdc.igd, i, j, px, py);
                } else {
                    tileDrawIntern(tileLayer, mvdc.mouseStatus, mvdc.currentLayer, mvdc.callbacks, mvdc.debugToggle, mvdc.igd, i, j, px, py);
                }
            }
        }
    }

    private void tileDrawIntern(int tdi, MapViewDrawContext.MouseStatus mouseAllowed, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd, int i, int j, int px, int py) {
        short tidx = targetTable.getTiletype(i, j, tdi);
        if (callbacks != null)
            tidx = callbacks.shouldDrawAt(mouseAllowed, i, j, tidx, tdi, currentLayer);
        if (shouldDraw(i, j, tdi, tidx)) {
            if (debug) {
                String t = Integer.toString(tidx, 16);
                FontManager.drawString(igd, px, py + (tdi * UIMapView.mapDebugTextHeight), t, false, false, UIMapView.mapDebugTextHeight);
            } else {
                tr.drawTile(tdi, tidx, px, py, igd, 1, false);
            }
        }
    }
}
