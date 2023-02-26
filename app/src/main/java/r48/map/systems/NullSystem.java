/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import r48.App;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.NullEventGraphicRenderer;
import r48.map.imaging.CacheImageLoader;
import r48.map.imaging.FixAndSecondaryImageLoader;
import r48.map.imaging.GabienImageLoader;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.NullTileRenderer;

/**
 * Created on 03/06/17.
 */
public class NullSystem extends MapSystem {
    public NullSystem(App app) {
        // Redundant cache as error safety net.
        // Having an explicit "ErrorSafetyNetImageLoader" would just complicate things.
        super(app, new CacheImageLoader(new FixAndSecondaryImageLoader("", "", new GabienImageLoader(""))), false);
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO target) {
        ITileRenderer tileRenderer = new NullTileRenderer();
        IEventGraphicRenderer eventRenderer = new NullEventGraphicRenderer();
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    @Override
    public MapViewDetails mapViewRequest(String gum, boolean allowCreate) {
        throw new RuntimeException("There's no map system, how can you bring up a map?");
    }
}
