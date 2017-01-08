/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.maptools;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIPanel;
import gabien.ui.UITabPane;
import gabien.ui.UITextButton;
import gabienapp.Application;
import gabienapp.map.IMapViewCallbacks;
import gabienapp.map.UIMapView;
import gabienapp.ui.UITileGrid;

/**
 * Created on 12/29/16.
 */
public class UIMTAutotile extends UIPanel implements IMapViewCallbacks {
    private final UITabPane tabPane;
    private final UITileGrid[] tileMaps;
    private final UIMapView map;

    public UIMTAutotile(UIMapView mv) {
        map = mv;
        IGrInDriver.IImage tm0 = Application.stuffRenderer.tilesetMaps[0];
        int tileCount = 48;
        if (tm0 != null)
            tileCount = ((tm0.getHeight() / 32) * 8);
        tileMaps = new UITileGrid[] {
                new UITileGrid(mv, 0, 49, true),
                new UITileGrid(mv, 48, 49, true),
                new UITileGrid(mv, 48 * 2, 49, true),
                new UITileGrid(mv, 48 * 3, 49, true),
                new UITileGrid(mv, 48 * 4, 49, true),
                new UITileGrid(mv, 48 * 5, 49, true),
                new UITileGrid(mv, 48 * 6, 49, true),
                new UITileGrid(mv, 48 * 7, 49, true),
                new UITileGrid(mv, 48 * 8, tileCount, false),
        };
        tabPane = new UITabPane(new String[] {
                "A0", // yes, I know this is blank. Oh well.
                "A1",
                "A2",
                "A3",
                "A4",
                "A5",
                "A6",
                "A7",
                "TM"
        }, tileMaps);
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
            if (sel == (tileMaps[tabPane.tab].tileStart + 48)) {
                map.mapTable.setTiletype(x, y, layer, (short) tileMaps[tabPane.tab].tileStart);
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
        return map.mapTable.getTiletype(x, y, layer) / 48;
    }
    private void updateAutotile(int x, int y, int layer) {
        int myAT = getAutotileType(x, y, layer, 8);
        if (myAT > 7)
            return; // this includes invalid tiles

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
        int recommendedTile = Application.autoTiles.inverseMap[index] + (48 * myAT);
        map.mapTable.setTiletype(x, y, layer, (short) recommendedTile);
        map.passModificationNotification();
    }

    @Override
    public String toString() {
        return "T" + tileMaps[tabPane.tab].tileStart + "-" + (tileMaps[tabPane.tab].tileStart + tileMaps[tabPane.tab].tileCount - 1) + ":" + tileMaps[tabPane.tab].getSelected();
    }

}
