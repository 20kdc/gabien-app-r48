/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.systems;

import r48.AppMain;
import r48.RubyIO;
import r48.map.StuffRenderer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.NullEventGraphicRenderer;
import r48.map.imaging.GabienImageLoader;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.NullTileRenderer;

/**
 * Created on 03/06/17.
 */
public class NullSystem extends MapSystem {
    @Override
    public StuffRenderer rendererFromMap(RubyIO map) {
        IImageLoader imageLoader = new GabienImageLoader(AppMain.rootPath, "");
        ITileRenderer tileRenderer = new NullTileRenderer();
        IEventGraphicRenderer eventRenderer = new NullEventGraphicRenderer();
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }
}
