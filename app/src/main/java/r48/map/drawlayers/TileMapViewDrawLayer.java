/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import r48.App;
import r48.ITileAccess;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.tiles.ITileRenderer;

/**
 * Created on 08/06/17.
 */
public class TileMapViewDrawLayer extends App.Svc implements IMapViewDrawLayer {
    public final ITileAccess targetTable;
    public final int[] tileLayers;
    public final int[] tileLayersBases;
    public final ITileRenderer tr;
    public final String name;

    public TileMapViewDrawLayer(App app, ITileAccess table, int i, ITileRenderer itr) {
        this(app, table, new int[] {i}, itr, app.t.m.l_tile.r(i));
    }

    public TileMapViewDrawLayer(App app, ITileAccess table, int[] i, ITileRenderer itr, String post) {
        super(app);
        targetTable = table;
        tileLayers = i;
        tileLayersBases = new int[i.length];
        for (int layer = 0; layer < i.length; layer++)
            tileLayersBases[layer] = table.getPBase(tileLayers[layer]);
        tr = itr;
        name = post;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean shouldDraw(int x, int y, int layer, int value) {
        return true;
    }

    public boolean shouldDrawRow(int y, int layer) {
        return true;
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        for (int j = mvdc.camT.y; j < mvdc.camT.y + mvdc.camT.height; j++) {
            int yBase = targetTable.getYBase(j);
            if (yBase == -1)
                continue;
            if (!shouldDrawRow(j, mvdc.currentLayer))
                continue;
            for (int i = mvdc.camT.x; i < mvdc.camT.x + mvdc.camT.width; i++) {
                int xBase = targetTable.getXBase(i);
                if (xBase == -1)
                    continue;
                // this addition is all we're going to hear from for basically ever
                xBase += yBase;
                int px = i * mvdc.tileSize;
                int py = j * mvdc.tileSize;
                for (int pi = 0; pi < tileLayers.length; pi++) {
                    int pBase = tileLayersBases[pi];
                    if (pBase == -1)
                        continue;
                    int tidx = targetTable.getTiletypeRaw(tileLayersBases[pi] + xBase);
                    tileDrawIntern(tileLayers[pi], mvdc.mouseStatus, mvdc.currentLayer, mvdc.callbacks, mvdc.debugToggle, mvdc.igd, tidx, i, j, px, py);
                }
            }
        }
    }

    private void tileDrawIntern(int tdi, MapViewDrawContext.MouseStatus mouseAllowed, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd, int tidx, int i, int j, int px, int py) {
        if (callbacks != null)
            tidx = callbacks.shouldDrawAt(mouseAllowed, i, j, tidx, tdi, currentLayer);
        if (shouldDraw(i, j, tdi, tidx)) {
            if (debug) {
                String t = Integer.toString(tidx, 16);
                GaBIEn.engineFonts.f6.drawLAB(igd, px, py + (tdi * 6), t, false);
            } else {
                tr.drawTile(tdi, tidx, px, py, igd);
            }
        }
    }
}
