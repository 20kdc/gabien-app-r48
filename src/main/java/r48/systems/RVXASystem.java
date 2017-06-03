/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.systems;

import r48.AppMain;
import r48.RubyIO;
import r48.map.StuffRenderer;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.imaging.GabienImageLoader;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.VXATileRenderer;

/**
 * Created on 03/06/17.
 */
public class RVXASystem extends RXPSystem {
    @Override
    public StuffRenderer rendererFromMap(RubyIO map) {
        String vxaPano = "";
        if (map != null) {
            vxaPano = map.getInstVarBySymbol("@parallax_name").decString();
            if (map.getInstVarBySymbol("@parallax_show").type != 'T')
                vxaPano = "";
        }

        IImageLoader imageLoader = new GabienImageLoader(AppMain.rootPath + "Graphics/", ".png");
        ITileRenderer tileRenderer = new VXATileRenderer(imageLoader, tsoFromMap(map), vxaPano);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, true);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer);
    }
}
