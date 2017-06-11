/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.systems;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import gabien.ui.UIPopupMenu;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.StuffRenderer;
import r48.map.UIMapViewContainer;

/**
 * Responsible for creating NSRs and such.
 * Everything that makes R48 a map editor hangs off of this.
 * The default system is the Null system.
 * Note that, at least theoretically, it is possible to run any system on the "R48" Ruby object backend, if you wish...
 * ...if it's a good idea is not my place to say. :)
 * Created on 03/06/17.
 */
public abstract class MapSystem {
    public UIElement createMapExplorer(final ISupplier<IConsumer<UIElement>> windowMaker, final UIMapViewContainer mapBox) {
        return new UIPopupMenu(new String[] {
                TXDB.get("Load Map")
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        mapBox.loadMap("Map");
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
}
