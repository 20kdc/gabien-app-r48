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
public class UIMTPasteRectangle extends UIMTBase implements IMapViewCallbacks {

    public final RubyTable table;
    public UILabel innerLabel = new UILabel(TXDB.get("Click at the target, or close this window."), FontSizes.dialogWindowTextHeight);

    public UIMTPasteRectangle(IMapToolContext par, RubyTable clipboard) {
        super(par, true);
        changeInner(innerLabel);
        table = clipboard;
        setBounds(getBounds());
    }

    @Override
    public void setBounds(Rect r) {
        Rect s = innerLabel.getBounds();
        super.setBounds(new Rect(r.x, r.y, s.width, s.height));
    }

    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        if (tx < cx)
            return there;
        if (ty < cy)
            return there;
        if (tx >= cx + table.width)
            return there;
        if (ty >= cy + table.height)
            return there;
        int px = tx - cx;
        int py = ty - cy;
        return table.getTiletype(px, py, layer);
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
        UIMapView map = mapToolContext.getMapView();
        for (int l = 0; l < table.planeCount; l++)
            for (int i = 0; i < table.width; i++)
                for (int j = 0; j < table.height; j++)
                    if (!map.mapTable.outOfBounds(i + x, j + y))
                        map.mapTable.setTiletype(i + x, j + y, l, table.getTiletype(i, j, l));
        map.passModificationNotification();
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return true;
    }
}
