/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.maptools;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;
import r48.ui.UIAppendButton;

/**
 * Created on 10/06/17.
 */
public class UIMTAutotileRectangle extends UIPanel implements IMapViewCallbacks {

    public final UIMTAutotile parent;
    public final int selTile, startX, startY;
    public final boolean autotile;

    public UIAppendButton innerLabel = new UIAppendButton(TXDB.get("Cancel"), new UILabel(TXDB.get("Click on a tile to finish the rectangle, or:"), FontSizes.dialogWindowTextHeight), new Runnable() {
        @Override
        public void run() {
            AppMain.nextMapTool = parent;
        }
    }, FontSizes.dialogWindowTextHeight);

    public UIMTAutotileRectangle(UIMTAutotile par, int tile, int x, int y, boolean at) {
        allElements.add(innerLabel);
        parent = par;
        selTile = tile;
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
        if (cx == tx)
            if (cy == ty)
                return (short) selTile;
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
            for (int i = startX; i <= x; i++)
                for (int j = startY; j <= y; j++)
                    parent.map.mapTable.setTiletype(i, j, parent.map.currentLayer, (short) selTile);
            if (autotile)
                for (int i = startX - 1; i <= x + 1; i++)
                    for (int j = startY - 1; j <= y + 1; j++)
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
