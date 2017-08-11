/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.toolsets;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.IMapContext;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.UIMapViewContainer;
import r48.map.systems.MapSystem;

/**
 * This is what AppMain holds to get at the current map, basically
 * Created on 2/12/17.
 */
public class MapToolset implements IToolset {
    private IMapContext context;

    @Override
    public String[] tabNames() {
        return new String[] {
                TXDB.get("Map"),
                TXDB.get("MapInfos")
        };
    }

    @Override
    public UIElement[] generateTabs(final ISupplier<IConsumer<UIElement>> windowMaker) {
        final UIMapViewContainer mapBox = new UIMapViewContainer(windowMaker);
        context = new IMapContext() {
            @Override
            public String getCurrentMap() {
                if (mapBox.view == null)
                    return null;
                return mapBox.view.mapId;
            }

            @Override
            public void loadMap(RubyIO k) {
                mapBox.loadMap(AppMain.system.mapLoadRequest(k, windowMaker));
            }

            @Override
            public void freeOsbResources() {
                if (mapBox.view == null)
                    return;
                mapBox.view.freeOsbResources();
            }
        };
        final UIElement mapInfoEl = AppMain.system.createMapExplorer(windowMaker, context);
        if (mapInfoEl != null) {
            return new UIElement[] {
                    mapBox, mapInfoEl
            };
        }
        return new UIElement[] {
                mapBox
        };
    }

    public IMapContext getContext() {
        return context;
    }
}
