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
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.NullEventGraphicRenderer;
import r48.map.tiles.NullTileRenderer;
import r48.map2d.tiles.TileRenderer;
import r48.texture.CacheTexLoader;
import r48.texture.FixAndSecondaryTexLoader;
import r48.texture.GabienTexLoader;

/**
 * Created on 03/06/17.
 */
public class NullSystem extends MapSystem {
    public NullSystem(App app) {
        // Redundant cache as error safety net.
        // Having an explicit "ErrorSafetyNetImageLoader" would just complicate things.
        super(app, new CacheTexLoader(new FixAndSecondaryTexLoader("", "", new GabienTexLoader(app.gameResources, ""))), false);
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO target) {
        TileRenderer tileRenderer = new NullTileRenderer();
        IEventGraphicRenderer eventRenderer = new NullEventGraphicRenderer();
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer);
    }

    @Override
    public MapViewDetails mapViewRequest(String gum, boolean allowCreate) {
        throw new RuntimeException("There's no map system, how can you bring up a map?");
    }
}
