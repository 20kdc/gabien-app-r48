/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
    private IMapContext context;
    private final UIElement[] tabs;

    public MapToolset(final ISupplier<IConsumer<UIElement>> windowMaker) {
        final UIMapViewContainer mapBox = new UIMapViewContainer(windowMaker);
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
        };
        final UIElement mapInfoEl = AppMain.system.createMapExplorer(windowMaker, context);
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

    @Override
    public String[] tabNames() {
        return new String[] {
                TXDB.get("Map"),
                TXDB.get("MapInfos")
        };
    }

    @Override
    public UIElement[] generateTabs(final ISupplier<IConsumer<UIElement>> windowMaker) {
        return tabs;
    }

    public IMapContext getContext() {
        return context;
    }
}
