/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.render.IImage;
import r48.R48;
import r48.RubyTableR;
import r48.io.data.IRIO;
import r48.map.drawlayers.*;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map2d.layers.GridMapViewDrawLayer;
import r48.map2d.layers.MapViewDrawLayer;
import r48.map2d.layers.TileMapViewDrawLayer;
import r48.map2d.tiles.TileRenderer;
import r48.texture.ITexLoader;
import r48.tr.pages.TrRoot;

/**
 * A replacement for (the old) StuffRenderer.
 * Created on 03/06/17.
 */
public class StuffRenderer {
    public final R48 app;
    public final TileRenderer tileRenderer;
    public final IEventGraphicRenderer eventRenderer;
    public final ITexLoader imageLoader;

    public StuffRenderer(R48 app, ITexLoader l, TileRenderer t, IEventGraphicRenderer e) {
        this.app = app;
        tileRenderer = t;
        eventRenderer = e;
        imageLoader = l;
    }

    public static MapViewDrawLayer[] prepareTraditional(R48 app, StuffRenderer renderer, int[] tlOrder, IRIO map, IEventAccess events, String vxaPano, boolean lx, boolean ly, int alx, int aly, int panoSW, int panoSH, int panoSC) {
        if (map == null)
            return new MapViewDrawLayer[0];
        final TrRoot T = app.t;
        RubyTableR rt = new RubyTableR(map.getIVar("@data").getBuffer());
        // 0: P
        // 1: E-1
        // 2, 3, [4, 5, [[6, 7]...]]]: Ti, Ei
        // E-2: ES
        // E-1: G
        MapViewDrawLayer[] layers = new MapViewDrawLayer[(rt.planeCount * 2) + 5];
        IImage panoImg = null;
        if (!vxaPano.equals(""))
            panoImg = renderer.imageLoader.getImage(vxaPano, true);
        layers[0] = new PanoramaMapViewDrawLayer(app.t, panoImg, lx, ly, alx, aly, rt.width, rt.height, panoSW, panoSH, panoSC);
        layers[1] = new EventMapViewDrawLayer(app.a, app.t, -1, events, renderer.eventRenderer, T.m.lowest);
        for (int i = 0; i < rt.planeCount; i++) {
            layers[(i * 2) + 2] = new TileMapViewDrawLayer(T, rt, tlOrder[i], renderer.tileRenderer);
            layers[(i * 2) + 3] = new EventMapViewDrawLayer(app.a, app.t, i, events, renderer.eventRenderer, "");
        }
        layers[layers.length - 3] = new EventMapViewDrawLayer(app.a, app.t, 0x7FFFFFFF, events, renderer.eventRenderer, "");
        layers[layers.length - 2] = new GridMapViewDrawLayer(app.t);
        layers[layers.length - 1] = new BorderMapViewDrawLayer(app, rt.getBounds().multiplied(renderer.tileRenderer.tileSize));
        return layers;
    }
}
