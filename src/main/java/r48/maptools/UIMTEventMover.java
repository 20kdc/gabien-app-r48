/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.IGrDriver;
import gabien.ui.UILabel;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;
import r48.ui.Art;

/**
 * Created on 1/1/17.
 */
public class UIMTEventMover extends UIMTBase implements IMapViewCallbacks {
    private IRIO targetEventKey;
    private UIMapView mapView;

    public UIMTEventMover(IMapToolContext mv, IRIO evK) {
        super(mv);
        mapView = mv.getMapView();
        targetEventKey = evK;
        changeInner(new UILabel(TXDB.get("Click to place event"), FontSizes.dialogWindowTextHeight), true);
    }

    // tool stuff

    @Override
    public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 1;
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, int l, boolean minimap) {
        if (mvdc.mouseStatus != null) {
            int tx = mvdc.mouseStatus.x / mvdc.tileSize;
            int ty = mvdc.mouseStatus.y / mvdc.tileSize;
            Art.drawSelectionBox(tx, ty, mvdc.tileSize, mvdc.tileSize, 1, mvdc.igd);
        }
    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
        if (!first)
            return;
        mapView.mapTable.eventAccess.setEventXY(targetEventKey, x, y);
        mapToolContext.accept(new UIMTEventPicker(mapToolContext));
    }
}
