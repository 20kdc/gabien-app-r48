/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.ui.UILabel;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.ui.UIAppendButton;

/**
 * Created on 10/06/17.
 */
public class UIMTAutotileRectangle extends UIMTBase implements IMapViewCallbacks {

    public final UIMTAutotile parent;
    public final int startX, startY;
    public final boolean autotile;

    public UIAppendButton innerLabel = new UIAppendButton(TXDB.get("Cancel"), new UILabel(TXDB.get("Click on a tile to finish the rectangle, or:"), FontSizes.dialogWindowTextHeight), new Runnable() {
        @Override
        public void run() {
            parent.selfClose = false;
            parent.hasClosed = false;
            mapToolContext.accept(parent);
        }
    }, FontSizes.dialogWindowTextHeight);

    public UIMTAutotileRectangle(UIMTAutotile par, int x, int y, boolean at) {
        super(par.mapToolContext);
        changeInner(innerLabel, true);
        parent = par;
        startX = x;
        startY = y;
        autotile = at;
    }

    @Override
    public short shouldDrawAt(boolean mouse, int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        int minX = Math.min(startX, cx);
        int maxX = Math.max(startX, cx);
        int minY = Math.min(startY, cy);
        int maxY = Math.max(startY, cy);
        if (tx >= minX)
            if (ty >= minY)
                if (tx <= maxX)
                    if (ty <= maxY)
                        return parent.getTCSelected(tx - startX, ty - startY);
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return minimap ? 0 : 1;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
        if (tx == startX)
            if (ty == startY)
                if (!minimap)
                    if ((((int) (GaBIEn.getTime() * 4)) % 2) == 0)
                        igd.clearRect(0, 0, 255, px, py, parent.map.tileSize, parent.map.tileSize);
    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer) {
        if (!parent.map.mapTable.outOfBounds(x, y)) {
            int minX = Math.min(startX, x);
            int maxX = Math.max(startX, x);
            int minY = Math.min(startY, y);
            int maxY = Math.max(startY, y);
            parent.performGeneralRectangle(layer, startX, startY, minX, minY, maxX, maxY);
            parent.selfClose = false;
            parent.hasClosed = false;
            mapToolContext.accept(parent);
        }
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return true;
    }
}
