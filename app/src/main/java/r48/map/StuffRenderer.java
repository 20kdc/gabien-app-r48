/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.render.IImage;
import r48.App;
import r48.RubyTableR;
import r48.io.data.IRIO;
import r48.map.drawlayers.*;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;
import r48.tr.pages.TrRoot;

/**
 * A replacement for (the old) StuffRenderer.
 * Created on 03/06/17.
 */
public class StuffRenderer {
    public final App app;
    public final ITileRenderer tileRenderer;
    public final IEventGraphicRenderer eventRenderer;
    public final IImageLoader imageLoader;

    public StuffRenderer(App app, IImageLoader l, ITileRenderer t, IEventGraphicRenderer e) {
        this.app = app;
        tileRenderer = t;
        eventRenderer = e;
        imageLoader = l;
    }

    public static IMapViewDrawLayer[] prepareTraditional(App app, StuffRenderer renderer, int[] tlOrder, IRIO map, IEventAccess events, String vxaPano, boolean lx, boolean ly, int alx, int aly, int panoSW, int panoSH, int panoSC) {
        if (map == null)
            return new IMapViewDrawLayer[0];
        final TrRoot T = app.t;
        RubyTableR rt = new RubyTableR(map.getIVar("@data").getBuffer());
        // 0: P
        // 1: E-1
        // 2, 3, [4, 5, [[6, 7]...]]]: Ti, Ei
        // E-2: ES
        // E-1: G
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[(rt.planeCount * 2) + 5];
        IImage panoImg = null;
        if (!vxaPano.equals(""))
            panoImg = renderer.imageLoader.getImage(vxaPano, true);
        layers[0] = new PanoramaMapViewDrawLayer(app, panoImg, lx, ly, alx, aly, rt.width, rt.height, panoSW, panoSH, panoSC);
        layers[1] = new EventMapViewDrawLayer(app, -1, events, renderer.eventRenderer, T.m.lowest);
        for (int i = 0; i < rt.planeCount; i++) {
            layers[(i * 2) + 2] = new TileMapViewDrawLayer(app, rt, tlOrder[i], renderer.tileRenderer, false, false);
            layers[(i * 2) + 3] = new EventMapViewDrawLayer(app, i, events, renderer.eventRenderer, "");
        }
        layers[layers.length - 3] = new EventMapViewDrawLayer(app, 0x7FFFFFFF, events, renderer.eventRenderer, "");
        layers[layers.length - 2] = new GridMapViewDrawLayer(app);
        layers[layers.length - 1] = new BorderMapViewDrawLayer(app, rt.width, rt.height);
        return layers;
    }
}
