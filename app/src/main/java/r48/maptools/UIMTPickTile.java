/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import r48.map.IMapToolContext;
import r48.map.UIMapView;
import r48.map2d.IMapViewCallbacks;
import r48.map2d.MapViewDrawContext;

/**
 * Actual tool is just a label saying what to do, these callbacks piggy back on it
 * Created on 09/06/17.
 */
public class UIMTPickTile extends UIMTBase implements IMapViewCallbacks {
    public UILabel innerLabel = new UILabel(T.m.tsPickTile, app.f.dialogWindowTH);
    public final UIMapView map;

    public UIMTPickTile(IMapToolContext m) {
        super(m);
        map = m.getMapView();
        changeInner(new UISplitterLayout(innerLabel, new UITextButton(T.m.tsPickTileAT, app.f.dialogWindowTH, () -> {
            m.setPickTileSwitch(!m.getPickTileSwitch());
        }).togglable(!m.getPickTileSwitch()), true, 1), true);
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, boolean minimap) {
        mvdc.drawMouseIndicator();
    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
        if (!first)
            return;
        if (!map.mapTable.outOfBounds(x, y))
            map.pickTileHelper.accept(map.mapTable.getTiletype(x, y, layer));
    }

    @Override
    @NonNull
    public String viewState(int mouseXT, int mouseYT) {
        return "PickTile";
    }
}
