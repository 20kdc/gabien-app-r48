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
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;

/**
 * Created on 1/1/17.
 */
public class UIMTEventMover extends UIMTBase implements IMapViewCallbacks {
    private RubyIO targetEvent;
    private UIMapView mapView;

    public UIMTEventMover(RubyIO evI, IMapToolContext mv) {
        super(mv, true);
        mapView = mv.getMapView();
        targetEvent = evI;
        changeInner(new UILabel(TXDB.get("Click to place event"), FontSizes.dialogWindowTextHeight));
    }

    // tool stuff

    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        if (cx == tx)
            if (cy == ty)
                return 0;
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 0;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

    }

    @Override
    public void confirmAt(int x, int y, int layer) {
        targetEvent.getInstVarBySymbol("@x").fixnumVal = x;
        targetEvent.getInstVarBySymbol("@y").fixnumVal = y;
        mapView.passModificationNotification();
        mapToolContext.accept(new UIMTEventPicker(mapToolContext));
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return true;
    }
}
