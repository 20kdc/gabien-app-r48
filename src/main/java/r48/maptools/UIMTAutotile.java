/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.maptools;

import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIPanel;
import gabien.ui.UITabPane;
import r48.AppMain;
import r48.FontSizes;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;
import r48.map.tiles.AutoTileTypeField;
import r48.ui.UITileGrid;

/**
 * Note that once created, it is meant to be locked to the layer it was created for.
 * Created on 12/29/16.
 */
public class UIMTAutotile extends UIPanel implements IMapViewCallbacks {
    private UITabPane tabPane;
    private UITileGrid[] tileMaps;
    private final UIMapView map;
    private AutoTileTypeField[] atBases;

    public UIMTAutotile(UIMapView mv) {
        map = mv;
        setupView();
        setBounds(new Rect(0, 0, 320, 200));
    }

    private void setupView() {
        allElements.clear();
        int layer = map.currentLayer;
        tileMaps = AppMain.stuffRenderer.tileRenderer.createATUIPlanes(map);
        tabPane = new UITabPane(AppMain.stuffRenderer.tileRenderer.getPlaneNames(layer), tileMaps, FontSizes.tilesTabTextHeight);
        atBases = AppMain.stuffRenderer.tileRenderer.indicateATs();
        allElements.add(tabPane);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        tabPane.setBounds(new Rect(0, 0, r.width, r.height));
    }

    // -- Tool stuff --

    @Override
    public short shouldDrawAtCursor(short there, int layer, int currentLayer) {
        if (layer == currentLayer)
            return (short) tileMaps[tabPane.tab].getTCSelected();
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
    public void confirmAt(int x, int y, int layer) {
        if (x < 0)
            return;
        if (y < 0)
            return;
        if (x >= map.mapTable.width)
            return;
        if (y >= map.mapTable.height)
            return;
        int sel = tileMaps[tabPane.tab].getTCSelected();
        if (tabPane.tab != 8) {
            if (tileMaps[tabPane.tab].atGroup != 0) {
                if (map.mapTable.outOfBounds(x, y))
                    return;
                map.mapTable.setTiletype(x, y, layer, (short) sel);
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

    private AutoTileTypeField getAutotileType(int x, int y, int layer, AutoTileTypeField preferred) {
        if (map.mapTable.outOfBounds(x, y))
            return preferred;
        int m = map.mapTable.getTiletype(x, y, layer);
        for (int i = 0; i < atBases.length; i++)
            if (atBases[i].start <= m)
                if ((atBases[i].start + atBases[i].length) > m)
                    return atBases[i];
        return null;
    }

    private void updateAutotile(int x, int y, int layer) {
        AutoTileTypeField myAT = getAutotileType(x, y, layer, null);
        if (myAT == null)
            return;

        int index = 0;
        int power = 1;
        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++) {
                if (i == 0)
                    if (j == 0)
                        continue;
                if (myAT.considersSameAs(getAutotileType(x + j, y + i, layer, myAT)))
                    index |= power;
                power <<= 1;
            }
        int recommendedTile = AppMain.autoTiles[myAT.databaseId].inverseMap[index] + myAT.start;
        map.mapTable.setTiletype(x, y, layer, (short) recommendedTile);
        map.passModificationNotification();
    }

    @Override
    public String toString() {
        return "T" + tileMaps[tabPane.tab].tileStart + "-" + (tileMaps[tabPane.tab].tileStart + tileMaps[tabPane.tab].tileCount - 1) + ":" + tileMaps[tabPane.tab].getSelected();
    }

    public void selectTile(short aShort) {
        for (int i = 0; i < tileMaps.length; i++)
            if (tileMaps[i].tryTCSetSelected(aShort)) {
                tabPane.selectTab(i);
                break;
            }
    }
}
