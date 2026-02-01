/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.elements.UILabel;
import r48.io.data.DMKey;
import r48.map.IMapToolContext;
import r48.map.UIMapView;
import r48.map2d.IMapViewCallbacks;
import r48.map2d.MapViewDrawContext;

/**
 * Created on 1/1/17.
 */
public class UIMTEventMover extends UIMTBase implements IMapViewCallbacks {
    private DMKey targetEventKey;
    private UIMapView mapView;

    public UIMTEventMover(IMapToolContext mv, DMKey evK) {
        super(mv);
        mapView = mv.getMapView();
        targetEventKey = evK;
        changeInner(new UILabel(T.m.tsPlaceEv, app.f.dialogWindowTH), true);
    }

    // tool stuff

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, boolean minimap) {
        if (mvdc.mouseStatus != null) {
            int tx = mvdc.mouseStatus.x / mvdc.tileSize;
            int ty = mvdc.mouseStatus.y / mvdc.tileSize;
            app.a.drawSelectionBox(tx, ty, mvdc.tileSize, mvdc.tileSize, 1, mvdc.igd);
        }
    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
        if (!first)
            return;
        mapView.mapTable.eventAccess.setEventXY(targetEventKey, x, y);
        mapToolContext.accept(new UIMTEventPicker(mapToolContext));
    }

    @Override
    @NonNull
    public String viewState(int mouseXT, int mouseYT) {
        return "EventMover." + mouseXT + "." + mouseYT;
    }
}
