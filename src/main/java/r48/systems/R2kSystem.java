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
import r48.map.events.R2kEventGraphicRenderer;
import r48.map.imaging.CacheImageLoader;
import r48.map.imaging.IImageLoader;
import r48.map.imaging.XYZOrPNGImageLoader;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.map.mapinfos.UIGRMMapInfos;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.LcfTileRenderer;

/**
 * ...
 * Created on 03/06/17.
 */
public class R2kSystem extends MapSystem {
    @Override
    public UIElement createMapExplorer(ISupplier<IConsumer<UIElement>> windowMaker, UIMapViewContainer mapBox) {
        return new UIGRMMapInfos(windowMaker, mapBox, new R2kRMLikeMapInfoBackend());
    }

    private RubyIO tsoFromMap2000(RubyIO map) {
        if (map == null)
            return null;
        return AppMain.objectDB.getObject("RPG_RT.ldb").getInstVarBySymbol("@tilesets").getHashVal(map.getInstVarBySymbol("@tileset_id"));
    }

    @Override
    public StuffRenderer rendererFromMap(RubyIO map) {
        String vxaPano = "";
        if (map != null) {
            vxaPano = map.getInstVarBySymbol("@parallax_name").decString();
            if (map.getInstVarBySymbol("@parallax_flag").type != 'T')
                vxaPano = "";
        }

        IImageLoader imageLoader = new CacheImageLoader(new XYZOrPNGImageLoader(AppMain.rootPath));
        ITileRenderer tileRenderer = new LcfTileRenderer(imageLoader, tsoFromMap2000(map), vxaPano);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(imageLoader, tileRenderer);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer);
    }
}
