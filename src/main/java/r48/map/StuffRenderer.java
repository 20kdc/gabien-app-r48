/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map;

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

    public StuffRenderer(IImageLoader l, ITileRenderer t, IEventGraphicRenderer e) {
        tileRenderer = t;
        eventRenderer = e;
        imageLoader = l;
    }
}
