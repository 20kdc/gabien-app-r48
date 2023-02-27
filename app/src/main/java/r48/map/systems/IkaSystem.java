/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import gabien.uslx.append.*;
import r48.App;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController;
import r48.map.StuffRenderer;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.IkaEventGraphicRenderer;
import r48.map.events.TraditionalEventAccess;
import r48.map.imaging.CacheImageLoader;
import r48.map.imaging.FixAndSecondaryImageLoader;
import r48.map.imaging.GabienImageLoader;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.IkaTileRenderer;

/**
 * Created on 03/06/17.
 */
public class IkaSystem extends MapSystem {
    public IkaSystem(App app) {
        super(app, new CacheImageLoader(new FixAndSecondaryImageLoader(app, "Pbm/", "", new GabienImageLoader(".pbm", 0, 0, 0))), true);
    }

    public StuffRenderer rendererGeneral(IRIO map, IEventAccess iea) {
        ITileRenderer tileRenderer = new IkaTileRenderer(imageLoader);
        IEventGraphicRenderer eventRenderer = new IkaEventGraphicRenderer(app, imageLoader);
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer, StuffRenderer.prepareTraditional(tileRenderer, new int[] {0}, eventRenderer, imageLoader, map, iea, "Back", true, true, 0, 0, -1, -1, 1));
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO target) {
        return rendererGeneral(null, null);
    }

    @Override
    public MapViewDetails mapViewRequest(final String gum, boolean allowCreate) {
        if (!allowCreate)
            if (app.odb.getObject(gum, null) == null)
                return null;
        final IObjectBackend.ILoadedObject map = app.odb.getObject(gum);
        final IEventAccess events = new TraditionalEventAccess(app, gum, "IkachanMap", "@events", 0, "IkachanEvent");
        return new MapViewDetails(app, gum, "IkachanMap", new IFunction<String, MapViewState>() {
            @Override
            public MapViewState apply(String s) {
                return MapViewState.fromRT(rendererGeneral(map.getObject(), events), gum, new String[] {}, map.getObject(), "@data", false, events);
            }
        }, new IFunction<IMapToolContext, IEditingToolbarController>() {
            @Override
            public IEditingToolbarController apply(IMapToolContext iMapToolContext) {
                return new MapEditingToolbarController(iMapToolContext, false);
            }
        });
    }
}
