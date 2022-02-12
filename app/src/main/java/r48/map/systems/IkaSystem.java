/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import gabien.uslx.append.*;
import r48.AppMain;
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
    public IkaSystem() {
        super(new CacheImageLoader(new FixAndSecondaryImageLoader("Pbm/", "", new GabienImageLoader(".pbm", 0, 0, 0))), true);
    }

    public StuffRenderer rendererGeneral(IRIO map, IEventAccess iea) {
        ITileRenderer tileRenderer = new IkaTileRenderer(imageLoader);
        IEventGraphicRenderer eventRenderer = new IkaEventGraphicRenderer(imageLoader);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, StuffRenderer.prepareTraditional(tileRenderer, new int[] {0}, eventRenderer, imageLoader, map, iea, "Back", true, true, 0, 0, -1, -1, 1));
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO target) {
        return rendererGeneral(null, null);
    }

    @Override
    public MapViewDetails mapViewRequest(final String gum, boolean allowCreate) {
        if (!allowCreate)
            if (AppMain.objectDB.getObject(gum, null) == null)
                return null;
        final IObjectBackend.ILoadedObject map = AppMain.objectDB.getObject(gum);
        final IEventAccess events = new TraditionalEventAccess(gum, "IkachanMap", "@events", 0, "IkachanEvent");
        return new MapViewDetails(gum, "IkachanMap", new IFunction<String, MapViewState>() {
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
