/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets;

import gabien.ui.UIElement;
import r48.App;
import r48.IMapContext;
import r48.map.UIMapView;
import r48.map.UIMapViewContainer;

/**
 * This is what AppMain holds to get at the current map, basically
 * Created on 2/12/17.
 */
public class MapToolset extends App.Svc implements IToolset {
    private IMapContext context;
    private final UIElement[] tabs;

    public MapToolset(App app) {
        super(app);
        final UIMapViewContainer mapBox = new UIMapViewContainer(app);
        context = new IMapContext() {
            @Override
            public String getCurrentMapObject() {
                if (mapBox.view == null)
                    return null;
                return mapBox.view.map.objectId;
            }

            @Override
            public void loadMap(String gum) {
                System.out.println("Game Unique Map:" + gum);
                mapBox.loadMap(gum);
            }

            @Override
            public void freeOsbResources() {
                if (mapBox.view == null)
                    return;
                mapBox.view.freeOsbResources();
            }

            @Override
            public void performCacheFlush() {
                // Can be null safely
                UIMapView.performFullCacheFlush(app, mapBox.view);
            }

            @Override
            public void performIRIOFlush() {
            }

            @Override
            public App getApp() {
                return app;
            }
        };

        String mapInfos = T.t.mMapInfos;
        String saves = T.t.mSaves;
        final UIElement saveEl = app.system.createSaveExplorer(context, saves);
        if (saveEl != null) {
            final UIElement mapInfoEl = app.system.createMapExplorer(context, mapInfos);
            if (mapInfoEl != null) {
                tabs = new UIElement[] {
                        mapBox, mapInfoEl, saveEl
                };
            } else {
                tabs = new UIElement[] {
                        mapBox, saveEl
                };
            }
        } else {
            final UIElement mapInfoEl = app.system.createMapExplorer(context, mapInfos);
            if (mapInfoEl != null) {
                tabs = new UIElement[] {
                        mapBox, mapInfoEl
                };
            } else {
                tabs = new UIElement[] {
                        mapBox
                };
            }
        }
    }

    @Override
    public UIElement[] generateTabs() {
        return tabs;
    }

    public IMapContext getContext() {
        return context;
    }
}
