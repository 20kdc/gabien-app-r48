/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import gabien.ui.*;
import r48.FontSizes;
import r48.IMapContext;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController;
import r48.map.StuffRenderer;
import r48.map.imaging.IImageLoader;

/**
 * Responsible for creating NSRs and such.
 * Everything that makes R48 a map editor hangs off of this.
 * The default system is the Null system.
 * Note that, at least theoretically, it is possible to run any system on the "R48" Ruby object backend, if you wish...
 * ...if it's a good idea is not my place to say. :)
 * Created on 03/06/17.
 */
public abstract class MapSystem {

    // All implementations will probably use a common image loader across the mapsystem.
    // It's not an absolute, but it's pretty likely.
    protected final IImageLoader imageLoader;

    public MapSystem(IImageLoader imgLoad) {
        imageLoader = imgLoad;
    }

    public UIElement createMapExplorer(final ISupplier<IConsumer<UIElement>> windowMaker, final IMapContext mapBox) {
        return new UIPopupMenu(new String[] {
                TXDB.get("Load Map")
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        mapBox.loadMap(new RubyIO().setFX(0));
                    }
                }
        }, FontSizes.menuTextHeight, false);
    }

    // The map can be null. This is used by the map view and on initial system startup.
    public abstract StuffRenderer rendererFromMap(RubyIO map);

    // Converts "map_id"-style elements to their map ID strings.
    // Returns null if the reference doesn't exist.
    public String mapReferentToId(RubyIO mapReferent) {
        return "Map";
    }

    // The schema might have tables attached to tilesets and might want to allow editing of them.
    public StuffRenderer rendererFromTso(RubyIO target) {
        return rendererFromMap(null);
    }

    // Similar to mapReferentToId, but used to get details for an incoming map change
    public MapLoadDetails mapLoadRequest(RubyIO mapReferent, final ISupplier<IConsumer<UIElement>> windowMaker) {
        MapLoadDetails mld = new MapLoadDetails();
        mld.objectId = mapReferentToId(mapReferent);
        mld.getToolbar = new IFunction<IMapToolContext, IEditingToolbarController>() {
            @Override
            public IEditingToolbarController apply(IMapToolContext uiMapView) {
                return new MapEditingToolbarController(uiMapView, windowMaker);
            }
        };
        return mld;
    }

    public static class MapLoadDetails {
        public String objectId;
        public IFunction<IMapToolContext, IEditingToolbarController> getToolbar;
    }
}
