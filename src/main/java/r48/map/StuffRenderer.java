/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map;

import gabien.IGrInDriver;
import r48.RubyIO;
import r48.RubyTable;
import r48.map.drawlayers.EventMapViewDrawLayer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.drawlayers.PanoramaMapViewDrawLayer;
import r48.map.drawlayers.TileMapViewDrawLayer;
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

    public StuffRenderer(IImageLoader l, ITileRenderer t, IEventGraphicRenderer e, IMapViewDrawLayer[] l2) {
        tileRenderer = t;
        eventRenderer = e;
        imageLoader = l;
        layers = l2;
    }

    public static IMapViewDrawLayer[] prepareTraditional(ITileRenderer itr, int[] tlOrder, IEventGraphicRenderer igr, IImageLoader iil, RubyIO map, String vxaPano) {
        if (map == null)
            return new IMapViewDrawLayer[0];
        RubyTable rt = new RubyTable(map.getInstVarBySymbol("@data").userVal);
        // 0: P
        // 1: E-1
        // 2, 3: T0, E0
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[(rt.planeCount * 2) + 2];
        IGrInDriver.IImage panoImg = null;
        if (!vxaPano.equals(""))
            panoImg = iil.getImage(vxaPano, true);
        layers[0] = new PanoramaMapViewDrawLayer(panoImg);
        RubyIO events = map.getInstVarBySymbol("@events");
        layers[1] = new EventMapViewDrawLayer(-1, events, igr);
        for (int i = 0; i < rt.planeCount; i++) {
            layers[(i * 2) + 2] = new TileMapViewDrawLayer(rt, tlOrder[i], itr);
            layers[(i * 2) + 3] = new EventMapViewDrawLayer(i, events, igr);
        }
        return layers;
    }
}
