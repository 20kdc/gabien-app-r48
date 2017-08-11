/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.maptools;

import gabien.IGrDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;

/**
 * UIMTAutotile isn't flexible enough for this.
 * Created on 2/19/17.
 */
public class UIMTShadowLayer extends UIPanel implements IMapViewCallbacks {
    public final UIMapView map;
    private UIScrollLayout uiSVL;
    private UINumberBox regionId;
    private int flags = 0;

    public UIMTShadowLayer(UIMapView mv) {
        map = mv;
        uiSVL = new UIScrollLayout(true);
        String[] s = new String[] {TXDB.get("Up-Left"), TXDB.get("Up-Right"), TXDB.get("Down-Left"), TXDB.get("Down-Right")};
        UITextButton[] controlButtons = new UITextButton[4];
        int power = 1;
        for (int i = 0; i < 4; i++) {
            final int thePower = power;
            controlButtons[i] = new UITextButton(FontSizes.tableElementTextHeight, s[i], new Runnable() {
                @Override
                public void run() {
                    flags ^= thePower;
                }
            }).togglable();
            power <<= 1;
        }
        uiSVL.panels.add(new UISplitterLayout(controlButtons[0], controlButtons[1], false, 1, 2));
        uiSVL.panels.add(new UISplitterLayout(controlButtons[2], controlButtons[3], false, 1, 2));
        int th = UITextButton.getRecommendedSize("X", FontSizes.tableElementTextHeight).height * 2;
        uiSVL.panels.add(new UISplitterLayout(new UILabel(TXDB.get("Region:"), FontSizes.tableElementTextHeight), regionId = new UINumberBox(FontSizes.tableElementTextHeight), false, 1, 2));
        th += Math.max(UILabel.getRecommendedSize("", FontSizes.tableElementTextHeight).height, UINumberBox.getRecommendedSize(FontSizes.tableElementTextHeight).height);
        allElements.add(uiSVL);
        setBounds(new Rect(0, 0, 160, th));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        uiSVL.setBounds(new Rect(0, 0, r.width, r.height));
    }

    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        if (cx == tx)
            if (cy == ty)
                if (layer == 3)
                    return (short) flags;
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        if (minimap)
            return 0;
        return 1;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
        if (map.mapTable.outOfBounds(tx, ty))
            return;
        int regionId = (map.mapTable.getTiletype(tx, ty, 3) & 0xFF00) >> 8;
        int l = UILabel.getTextLength("R" + regionId, FontSizes.mapDebugTextHeight) + 1;
        igd.clearRect(0, 0, 0, px, py, l, FontSizes.mapDebugTextHeight);
        UILabel.drawString(igd, px, py, "R" + regionId, true, FontSizes.mapDebugTextHeight);
    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

    }

    @Override
    public void confirmAt(int x, int y, int layer) {
        if (map.mapTable.outOfBounds(x, y))
            return;
        map.mapTable.setTiletype(x, y, 3, (short) (flags | (regionId.number << 8)));
        map.passModificationNotification();
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return false;
    }
}
