/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import r48.App;
import r48.dbs.ObjectRootHandle;
import r48.io.data.IRIO;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController;
import r48.map.StuffRenderer;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.IkaEventGraphicRenderer;
import r48.map.events.TraditionalEventAccess;
import r48.map.imaging.FixAndSecondaryImageLoader;
import r48.map.imaging.GabienImageLoader;
import r48.map.tiles.IkaTileRenderer;
import r48.texture.CacheTexLoader;

/**
 * Created on 03/06/17.
 */
public class IkaSystem extends MapSystem {
    public final IkaTileRenderer tileRenderer;
    public IkaSystem(App app) {
        super(app, new CacheTexLoader(new FixAndSecondaryImageLoader(app, "Pbm/", "", new GabienImageLoader(app, ".pbm", 0, 0, 0))), true);
        tileRenderer = new IkaTileRenderer(app.t, imageLoader);
    }

    public StuffRenderer rendererGeneral() {
        tileRenderer.checkReload();
        IEventGraphicRenderer eventRenderer = new IkaEventGraphicRenderer(app, imageLoader);
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer);
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO target) {
        return rendererGeneral();
    }

    @Override
    public MapViewDetails mapViewRequest(final String gum, boolean allowCreate) {
        final ObjectRootHandle map = app.odb.getObject(gum, allowCreate);
        if (map == null)
            return null;
        final IEventAccess events = new TraditionalEventAccess(app, gum, "@events", 0, "IkachanEvent");
        return new MapViewDetails(app, gum, map) {
            @Override
            public MapViewState rebuild() {
                StuffRenderer sr = rendererGeneral();
                return MapViewState.fromRT(sr, StuffRenderer.prepareTraditional(app, sr, new int[] {0}, map.getObject(), events, "Back", true, true, 0, 0, -1, -1, 1), null, gum, new String[] {}, map.getObject(), "@data", false, events, false, false);
            }
            @Override
            public IEditingToolbarController makeToolbar(IMapToolContext context) {
                return new MapEditingToolbarController(context, false);
            }
        };
    }
}
