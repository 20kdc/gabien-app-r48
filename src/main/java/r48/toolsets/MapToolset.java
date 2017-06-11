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
import r48.dbs.TXDB;
import r48.map.UIMapViewContainer;

/**
 * This is what AppMain holds to get at the current map, basically
 * Created on 2/12/17.
 */
public class MapToolset implements IToolset {
    UIMapViewContainer lastMadeMVC;

    @Override
    public String[] tabNames() {
        return new String[] {
                TXDB.get("Map"),
                TXDB.get("MapInfos")
        };
    }

    @Override
    public UIElement[] generateTabs(ISupplier<IConsumer<UIElement>> windowMaker) {
        final UIMapViewContainer mapBox = new UIMapViewContainer(windowMaker);
        lastMadeMVC = mapBox;
        final UIElement mapInfoEl = AppMain.system.createMapExplorer(windowMaker, mapBox);
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
        return new IMapContext() {
            @Override
            public String getCurrentMap() {
                if (lastMadeMVC == null)
                    return null;
                if (lastMadeMVC.view == null)
                    return null;
                return lastMadeMVC.view.mapId;
            }

            @Override
            public void loadMap(String s) {
                lastMadeMVC.loadMap(s);
            }

            @Override
            public void freeOsbResources() {
                lastMadeMVC.view.freeOsbResources();
            }
        };
    }
}
