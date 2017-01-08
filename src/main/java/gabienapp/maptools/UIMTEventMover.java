/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.maptools;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import gabien.ui.UIPanel;
import gabienapp.Application;
import gabienapp.map.IMapViewCallbacks;
import gabienapp.RubyIO;
import gabienapp.map.UIMapView;

/**
 * Created on 1/1/17.
 */
public class UIMTEventMover extends UIPanel implements IMapViewCallbacks {
    private RubyIO targetEvent;
    private UILabel uil;
    private UIMapView mapView;
    public UIMTEventMover(RubyIO evI, UIMapView mv) {
        mapView = mv;
        targetEvent = evI;
        uil = new UILabel("Click to place event", true);
        allElements.add(uil);
        setBounds(new Rect(0, 0, 160, 18));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        uil.setBounds(new Rect(0, 0, r.width, r.height));
    }

    // tool stuff
    @Override
    public short shouldDrawAtCursor(short there, int layer, int currentLayer) {
        // This should be obvious enough.
        return 0;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 0;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrInDriver igd, int px, int py, int ol, boolean minimap) {
    }

    @Override
    public void confirmAt(int x, int y, int layer) {
        targetEvent.getInstVarBySymbol("@x").fixnumVal = x;
        targetEvent.getInstVarBySymbol("@y").fixnumVal = y;
        mapView.passModificationNotification();
        Application.nextMapTool = new UIMTEventPicker(null, mapView);
    }
}
