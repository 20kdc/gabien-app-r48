/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.maptools;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;
import r48.ui.UIAppendButton;

/**
 * Created on September 2, 2017
 */
public class UIMTCopyRectangle extends UIMTBase implements IMapViewCallbacks {

    public int startX, startY;
    public boolean stage;

    public UILabel innerLabel = new UILabel(TXDB.get("Click on a tile to start the rectangle, or:"), FontSizes.dialogWindowTextHeight);
    public UIAppendButton inner = new UIAppendButton(TXDB.get("Cancel"), innerLabel, new Runnable() {
        @Override
        public void run() {
            mapToolContext.accept(null);
        }
    }, FontSizes.dialogWindowTextHeight);

    public UIMTCopyRectangle(IMapToolContext par) {
        super(par, true);
        changeInner(inner);
        setBounds(getBounds());
    }

    @Override
    public void setBounds(Rect r) {
        Rect s = innerLabel.getBounds();
        super.setBounds(new Rect(r.x, r.y, s.width, s.height));
    }

    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        if (tx == cx)
            if (ty == cy)
                return 0;
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return minimap ? 0 : 1;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
        UIMapView map = mapToolContext.getMapView();
        if (!stage)
            return;
        if (tx == startX)
            if (ty == startY)
                if (!minimap)
                    if ((((int) (GaBIEn.getTime() * 4)) % 2) == 0)
                        igd.clearRect(0, 0, 255, px, py, map.tileSize, map.tileSize);
    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

    }

    @Override
    public void confirmAt(int x, int y, int layer) {
        if (stage) {
            UIMapView map = mapToolContext.getMapView();
            if (!map.mapTable.outOfBounds(x, y)) {
                int minX = Math.min(startX, x);
                int maxX = Math.max(startX, x);
                int minY = Math.min(startY, y);
                int maxY = Math.max(startY, y);
                RubyTable rt = new RubyTable((maxX - minX) + 1, (maxY - minY) + 1, map.mapTable.planeCount, new int[map.mapTable.planeCount]);
                for (int l = 0; l < map.mapTable.planeCount; l++)
                    for (int i = minX; i <= maxX; i++)
                        for (int j = minY; j <= maxY; j++)
                            if (!map.mapTable.outOfBounds(i, j))
                                rt.setTiletype(i - minX, j - minY, l, map.mapTable.getTiletype(i, j, l));
                RubyIO rb = new RubyIO();
                rb.setUser("Table", rt.innerBytes);
                AppMain.theClipboard = rb;
                mapToolContext.accept(null);
            }
        } else {
            startX = x;
            startY = y;
            innerLabel.Text = TXDB.get("Click on another tile to finish copying.");
            setBounds(getBounds());
            stage = true;
        }
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return true;
    }
}