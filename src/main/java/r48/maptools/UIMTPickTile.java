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
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;

/**
 * Actual tool is just a label saying what to do, these callbacks piggy back on it
 * Created on 09/06/17.
 */
public class UIMTPickTile extends UIMTBase implements IMapViewCallbacks {
    public UILabel innerLabel = new UILabel(TXDB.get("Click on a tile to pick it."), FontSizes.dialogWindowTextHeight);
    public final UIMapView map;

    public UIMTPickTile(IMapToolContext m) {
        super(m, true);
        map = m.getMapView();
        changeInner(innerLabel);
    }

    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
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
        if (!map.mapTable.outOfBounds(x, y))
            map.pickTileHelper.accept(map.mapTable.getTiletype(x, y, layer));
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return true;
    }
}
