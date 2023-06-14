/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.ui.UIElement;
import r48.maptools.UIMTAutotile;
import r48.maptools.UIMTBase;

/**
 * Created on August 14th 2017, #blameIDEA, etcetc.
 */
public interface IMapToolContext {
    UIMapView getMapView();

    void createWindow(UIElement window);

    void accept(UIMTBase nextTool);

    UIMTAutotile showATField();

    boolean getMasterRenderDisableSwitch();
    void setMasterRenderDisableSwitch(boolean value);

    boolean getMasterAnimDisableSwitch();
    void setMasterAnimDisableSwitch(boolean value);
}
