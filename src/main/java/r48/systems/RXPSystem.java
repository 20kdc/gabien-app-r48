/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.systems;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.RubyIO;
import r48.map.StuffRenderer;
import r48.map.UIMapViewContainer;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.imaging.GabienImageLoader;
import r48.map.imaging.IImageLoader;
import r48.map.mapinfos.RXPRMLikeMapInfoBackend;
import r48.map.mapinfos.UIGRMMapInfos;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.XPTileRenderer;

/**
 * Created on 03/06/17.
 */
public class RXPSystem extends MapSystem {
    @Override
    public UIElement createMapExplorer(ISupplier<IConsumer<UIElement>> windowMaker, UIMapViewContainer mapBox) {
        return new UIGRMMapInfos(windowMaker, mapBox, new RXPRMLikeMapInfoBackend());
    }

    protected static RubyIO tsoFromMap(RubyIO map) {
        if (map == null)
            return null;
        RubyIO tileset = null;
        int tid = (int) map.getInstVarBySymbol("@tileset_id").fixnumVal;
        RubyIO tilesets = AppMain.objectDB.getObject("Tilesets");
        if ((tid >= 0) && (tid < tilesets.arrVal.length))
            tileset = tilesets.arrVal[tid];
        if (tileset != null)
            if (tileset.type == '0')
                tileset = null;
        return tileset;
    }

    @Override
    public StuffRenderer rendererFromMap(RubyIO map) {
        IImageLoader imageLoader = new GabienImageLoader(AppMain.rootPath + "Graphics/", ".png");
        ITileRenderer tileRenderer = new XPTileRenderer(imageLoader, tsoFromMap(map));
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, false);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer);
    }

    @Override
    public StuffRenderer rendererFromTso(RubyIO tso) {
        IImageLoader imageLoader = new GabienImageLoader(AppMain.rootPath + "Graphics/", ".png");
        ITileRenderer tileRenderer = new XPTileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, false);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer);
    }
}
