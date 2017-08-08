/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.maptools;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.ui.UIAppendButton;

/**
 * Created on 10/06/17.
 */
public class UIMTAutotileRectangle extends UIPanel implements IMapViewCallbacks {

    public final UIMTAutotile parent;
    public final int startX, startY;
    public final boolean autotile;

    public UIAppendButton innerLabel = new UIAppendButton(TXDB.get("Cancel"), new UILabel(TXDB.get("Click on a tile to finish the rectangle, or:"), FontSizes.dialogWindowTextHeight), new Runnable() {
        @Override
        public void run() {
            AppMain.nextMapTool = parent;
        }
    }, FontSizes.dialogWindowTextHeight);

    public UIMTAutotileRectangle(UIMTAutotile par, int x, int y, boolean at) {
        allElements.add(innerLabel);
        parent = par;
        startX = x;
        startY = y;
        autotile = at;
        setBounds(getBounds());
    }

    @Override
    public void setBounds(Rect r) {
        Rect s = innerLabel.getBounds();
        super.setBounds(new Rect(r.x, r.y, s.width, s.height));
    }

    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        int minX = Math.min(startX, cx);
        int maxX = Math.max(startX, cx);
        int minY = Math.min(startY, cy);
        int maxY = Math.max(startY, cy);
        if (tx >= minX)
            if (ty >= minY)
                if (tx <= maxX)
                    if (ty <= maxY)
                        return parent.getPlaceSelection(tx - startX, ty - startY);
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
    public void confirmAt(int x, int y, int layer) {
        if (!parent.map.mapTable.outOfBounds(x, y)) {
            int minX = Math.min(startX, x);
            int maxX = Math.max(startX, x);
            int minY = Math.min(startY, y);
            int maxY = Math.max(startY, y);
            for (int i = minX; i <= maxX; i++)
                for (int j = minY; j <= maxY; j++)
                    parent.map.mapTable.setTiletype(i, j, parent.map.currentLayer, (short) parent.getPlaceSelection(i - startX, j - startY));
            if (autotile)
                for (int i = minX - 1; i <= maxX + 1; i++)
                    for (int j = minY - 1; j <= maxY + 1; j++)
                        UIMTAutotile.updateAutotile(parent.map, parent.atBases, i, j, parent.map.currentLayer);
            parent.map.passModificationNotification();
            AppMain.nextMapTool = parent;
        }
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return true;
    }
}
