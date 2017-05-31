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
import r48.map.StuffRenderer;
import r48.map.UIMapViewContainer;

/**
 * Created on 2/12/17.
 */
public class MapToolset implements IToolset {
    @Override
    public String[] tabNames() {
        return new String[] {
                "Map",
                "MapInfos"
        };
    }

    @Override
    public UIElement[] generateTabs(ISupplier<IConsumer<UIElement>> windowMaker) {
        final UIMapViewContainer mapBox = new UIMapViewContainer(windowMaker);
        final UIElement mapInfoEl = StuffRenderer.createMapExplorer(windowMaker, mapBox);
        if (mapInfoEl != null) {
            return new UIElement[] {
                    mapBox, mapInfoEl
            };
        }
        return new UIElement[] {
                mapBox
        };
    }
}
