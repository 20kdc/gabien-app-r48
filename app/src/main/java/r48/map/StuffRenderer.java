/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.IImage;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.map.drawlayers.*;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;

/**
 * A replacement for (the old) StuffRenderer.
 * Created on 03/06/17.
 */
public class StuffRenderer {
    public final ITileRenderer tileRenderer;
    public final IEventGraphicRenderer eventRenderer;
    public final IImageLoader imageLoader;
    public final IMapViewDrawLayer[] layers;
    public final boolean[] activeDef;

    public StuffRenderer(IImageLoader l, ITileRenderer t, IEventGraphicRenderer e, IMapViewDrawLayer[] l2) {
        this(l, t, e, l2, null);
    }

    public StuffRenderer(IImageLoader l, ITileRenderer t, IEventGraphicRenderer e, IMapViewDrawLayer[] l2, boolean[] activeDefault) {
        tileRenderer = t;
        eventRenderer = e;
        imageLoader = l;
        layers = l2;
        if (activeDefault != null) {
            activeDef = activeDefault;
        } else {
            activeDef = new boolean[l2.length];
            for (int i = 0; i < activeDef.length; i++) {
                activeDef[i] = true;
                if (l2[i] instanceof PassabilityMapViewDrawLayer)
                    activeDef[i] = false;
                if (l2[i] instanceof GridMapViewDrawLayer)
                    activeDef[i] = false;
            }
        }
    }

    public static IMapViewDrawLayer[] prepareTraditional(ITileRenderer itr, int[] tlOrder, IEventGraphicRenderer igr, IImageLoader iil, IRIO map, IEventAccess events, String vxaPano, boolean lx, boolean ly, int alx, int aly, int panoSW, int panoSH, int panoSC) {
        if (map == null)
            return new IMapViewDrawLayer[0];
        RubyTable rt = new RubyTable(map.getIVar("@data").getBuffer());
        // 0: P
        // 1: E-1
        // 2, 3, [4, 5, [[6, 7]...]]]: Ti, Ei
        // E-2: ES
        // E-1: G
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[(rt.planeCount * 2) + 5];
        IImage panoImg = null;
        if (!vxaPano.equals(""))
            panoImg = iil.getImage(vxaPano, true);
        layers[0] = new PanoramaMapViewDrawLayer(panoImg, lx, ly, alx, aly, rt.width, rt.height, panoSW, panoSH, panoSC);
        layers[1] = new EventMapViewDrawLayer(-1, events, igr, TXDB.get(" (Lowest)"));
        for (int i = 0; i < rt.planeCount; i++) {
            layers[(i * 2) + 2] = new TileMapViewDrawLayer(rt, tlOrder[i], itr);
            layers[(i * 2) + 3] = new EventMapViewDrawLayer(i, events, igr, "");
        }
        layers[layers.length - 3] = new EventMapViewDrawLayer(0x7FFFFFFF, events, igr, "");
        layers[layers.length - 2] = new GridMapViewDrawLayer();
        layers[layers.length - 1] = new BorderMapViewDrawLayer(rt.width, rt.height);
        return layers;
    }
}
