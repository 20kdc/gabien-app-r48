/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.maptools;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIPanel;
import gabien.ui.UITabPane;
import r48.AppMain;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

/**
 * Created on 12/29/16.
 */
public class UIMTAutotile extends UIPanel implements IMapViewCallbacks {
    private final UITabPane tabPane;
    private final UITileGrid[] tileMaps;
    private final UIMapView map;
    private final int[] atBases;

    public UIMTAutotile(UIMapView mv) {
        map = mv;
        // may not be right at all, work on this!
        tileMaps = AppMain.stuffRenderer.tileRenderer.createATUIPlanes(mv);
        tabPane = new UITabPane(AppMain.stuffRenderer.tileRenderer.getPlaneNames(), tileMaps);
        atBases = AppMain.stuffRenderer.tileRenderer.indicateATs();
        allElements.add(tabPane);
        setBounds(new Rect(0, 0, 320, 200));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        tabPane.setBounds(new Rect(0, 0, r.width, r.height));
    }

    // -- Tool stuff --

    @Override
    public short shouldDrawAtCursor(short there, int layer, int currentLayer) {
        if (layer == currentLayer) {
            int selected = tileMaps[tabPane.tab].getSelected();
            if (selected == (tileMaps[tabPane.tab].tileStart + 48))
                return (short) tileMaps[tabPane.tab].tileStart;
            return (short) selected;
        }
        return there;
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
        if (x < 0)
            return;
        if (y < 0)
            return;
        if (x >= map.mapTable.width)
            return;
        if (y >= map.mapTable.height)
            return;
        int sel = tileMaps[tabPane.tab].getSelected();
        if (tabPane.tab != 8) {
            if (tileMaps[tabPane.tab].selectedATB()) {
                map.mapTable.setTiletype(x, y, layer, (short) (sel - 47));
                for (int i = -1; i < 2; i++)
                    for (int j = -1; j < 2; j++)
                        updateAutotile(x + i, y + j, layer);
                return;
            }
        }
        for (int px = 0; px < tileMaps[tabPane.tab].selWidth; px++) {
            if (px + x < 0)
                continue;
            if (px + x >= map.mapTable.width)
                continue;
            for (int py = 0; py < tileMaps[tabPane.tab].selHeight; py++) {
                if (py + y < 0)
                    continue;
                if (py + y >= map.mapTable.height)
                    continue;
                short ind = (short) (sel + px + (py * tileMaps[tabPane.tab].getSelectStride()));
                map.mapTable.setTiletype(x + px, y + py, layer, ind);
                map.passModificationNotification();
            }
        }
    }

    private int getAutotileType(int x, int y, int layer, int preferred) {
        if (x < 0)
            return preferred;
        if (y < 0)
            return preferred;
        if (x >= map.mapTable.width)
            return preferred;
        if (y >= map.mapTable.height)
            return preferred;
        int m = map.mapTable.getTiletype(x, y, layer);
        for (int i = 0; i < atBases.length; i++)
            if (atBases[i] <= m)
                if ((atBases[i] + 48) > m)
                    return atBases[i];
        return -1;
    }
    private void updateAutotile(int x, int y, int layer) {
        int myAT = getAutotileType(x, y, layer, 8);
        if (myAT == -1)
            return;

        int index = 0;
        int power = 1;
        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++) {
                if (i == 0)
                    if (j == 0)
                        continue;
                if (getAutotileType(x + j, y + i, layer, myAT) == myAT)
                    index |= power;
                power <<= 1;
            }
        int recommendedTile = AppMain.autoTiles.inverseMap[index] + myAT;
        map.mapTable.setTiletype(x, y, layer, (short) recommendedTile);
        map.passModificationNotification();
    }

    @Override
    public String toString() {
        return "T" + tileMaps[tabPane.tab].tileStart + "-" + (tileMaps[tabPane.tab].tileStart + tileMaps[tabPane.tab].tileCount - 1) + ":" + tileMaps[tabPane.tab].getSelected();
    }

}
