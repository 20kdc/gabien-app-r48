/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map;

import gabien.ui.UIElement;
import r48.maptools.UIMTBase;

/**
 * Created on August 14th 2017, #blameIDEA, etcetc.
 */
public interface IMapToolContext {
    UIMapView getMapView();
    void createWindow(UIElement window);
    void accept(UIMTBase nextTool);
}
