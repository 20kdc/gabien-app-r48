/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.ui.UILabel;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;

/**
 * Actual tool is just a label saying what to do, these callbacks piggy back on it
 * Created on 09/06/17.
 */
public class UIMTPickTile extends UIMTBase implements IMapViewCallbacks {
    public UILabel innerLabel = new UILabel(T.z.l7, app.f.dialogWindowTH);
    public final UIMapView map;

    public UIMTPickTile(IMapToolContext m) {
        super(m);
        map = m.getMapView();
        changeInner(innerLabel, true);
    }

    @Override
    public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 0;
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, int l, boolean minimap) {

    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
        if (!first)
            return;
        if (!map.mapTable.outOfBounds(x, y))
            map.pickTileHelper.accept(map.mapTable.getTiletype(x, y, layer));
    }
}
