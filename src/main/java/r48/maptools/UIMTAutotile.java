/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.maptools;

import gabien.IGrDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;
import r48.map.tiles.AutoTileTypeField;
import r48.ui.UIAppendButton;
import r48.ui.UINSVertLayout;
import r48.ui.UITileGrid;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Note that once created, it is meant to be locked to the layer it was created for.
 * ... this is kind of getting monolithic with multiple subtools. Oh well.
 * Created on 12/29/16.
 */
public class UIMTAutotile extends UIMTBase implements IMapViewCallbacks {
    // Sub-tools panel
    private UIElement subtoolBar;
    private int subtool = 0;

    private UITabPane tabPane;
    private UITileGrid[] tileMaps;
    public final UIMapView map;
    public AutoTileTypeField[] atBases;

    public UIMTAutotile(IMapToolContext mv) {
        super(mv, false);
        map = mv.getMapView();
        setupView();
        setBounds(new Rect(0, 0, (map.tileSize * map.renderer.tileRenderer.getRecommendedWidth()) + FontSizes.gridScrollersize, 200));
    }

    private void setupView() {
        int layer = map.currentLayer;
        tileMaps = map.renderer.tileRenderer.createATUIPlanes(map);
        tabPane = new UITabPane(map.renderer.tileRenderer.getPlaneNames(layer), tileMaps, FontSizes.tilesTabTextHeight);
        atBases = map.renderer.tileRenderer.indicateATs();

        // Begin subtool bar...

        subtool = 0;
        final LinkedList<UITextButton> options = new LinkedList<UITextButton>();

        UITextButton baseTool = new UITextButton(FontSizes.atSubtoolTextHeight, TXDB.get("Pen"), new Runnable() {
            @Override
            public void run() {
                for (UITextButton utb : options)
                    utb.state = false;
                options.get(0).state = true;
                subtool = 0;
            }
        }).togglable();
        options.add(baseTool);
        baseTool.state = true;

        UIAppendButton uab = new UIAppendButton(TXDB.get("Rect."), baseTool, new Runnable() {
            @Override
            public void run() {
                for (UITextButton utb : options)
                    utb.state = false;
                options.get(1).state = true;
                subtool = 1;
            }
        }, FontSizes.atSubtoolTextHeight);
        options.add(uab.button.togglable());

        uab = new UIAppendButton(TXDB.get("Fill"), uab, new Runnable() {
            @Override
            public void run() {
                for (UITextButton utb : options)
                    utb.state = false;
                options.get(2).state = true;
                subtool = 2;
            }
        }, FontSizes.atSubtoolTextHeight);
        options.add(uab.button.togglable());

        subtoolBar = uab;

        changeInner(new UINSVertLayout(subtoolBar, tabPane));
    }

    // -- Tool stuff --

    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        if (layer != currentLayer)
            return there;
        if ((subtool != 0) || (tileMaps[tabPane.tab].atGroup != 0)) {
            if (cx == tx)
                if (cy == ty)
                    return (short) tileMaps[tabPane.tab].getTCSelected();
            return there;
        }
        if (tx < cx)
            return there;
        if (ty < cy)
            return there;
        if (tx >= cx + tileMaps[tabPane.tab].selWidth)
            return there;
        if (ty >= cy + tileMaps[tabPane.tab].selHeight)
            return there;
        int px = tx - cx;
        int py = ty - cy;
        int sel = tileMaps[tabPane.tab].getTCSelected();
        return (short) (sel + px + (py * tileMaps[tabPane.tab].getSelectStride()));
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
        if (x < 0)
            return;
        if (y < 0)
            return;
        if (x >= map.mapTable.width)
            return;
        if (y >= map.mapTable.height)
            return;
        int sel = tileMaps[tabPane.tab].getTCSelected();

        if (subtool == 1) {
            // Tool 1: Rectangle
            mapToolContext.accept(new UIMTAutotileRectangle(this, x, y, tileMaps[tabPane.tab].atGroup != 0));
            return;
        } else if (subtool == 2) {
            // Tool 2: Floodfill.
            // The rules on what is considered the same are:
            // 1. Any two tiles which have the same ID are the same.
            // 2. Any two tiles which are both part of an AT group and that is the same AT group, are the same.
            // 3. Anything else is not the same.
            HashSet<FloodFillPoint> investigatePoints = new HashSet<FloodFillPoint>();
            HashSet<FloodFillPoint> handledPoints = new HashSet<FloodFillPoint>();
            investigatePoints.add(new FloodFillPoint(x, y));
            int key = map.mapTable.getTiletype(x, y, map.currentLayer);
            while (investigatePoints.size() > 0) {
                LinkedList<FloodFillPoint> working = new LinkedList<FloodFillPoint>(investigatePoints);
                investigatePoints.clear();
                for (FloodFillPoint ffp : working) {
                    // work out if this ought to be replaced
                    if (ffp.contentMatches(key)) {
                        if (handledPoints.contains(ffp))
                            continue;
                        handledPoints.add(ffp);
                        map.mapTable.setTiletype(ffp.tX, ffp.tY, map.currentLayer, (short) sel);
                        investigatePoints.add(new FloodFillPoint(ffp.tX - 1, ffp.tY));
                        investigatePoints.add(new FloodFillPoint(ffp.tX + 1, ffp.tY));
                        investigatePoints.add(new FloodFillPoint(ffp.tX, ffp.tY - 1));
                        investigatePoints.add(new FloodFillPoint(ffp.tX, ffp.tY + 1));
                    }
                }
            }
            if (tileMaps[tabPane.tab].atGroup != 0) {
                for (FloodFillPoint ffp : handledPoints) {
                    for (int i = -1; i < 2; i++)
                        for (int j = -1; j < 2; j++)
                            updateAutotile(map, atBases, ffp.tX + i, ffp.tY + j, layer);
                }
            }
            map.passModificationNotification();
            return;
        }

        // Tool 0: Default pen/selection.
        if (tileMaps[tabPane.tab].atGroup != 0) {
            if (map.mapTable.outOfBounds(x, y))
                return;
            map.mapTable.setTiletype(x, y, layer, (short) sel);
            for (int i = -1; i < 2; i++)
                for (int j = -1; j < 2; j++)
                    updateAutotile(map, atBases, x + i, y + j, layer);
            return;
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

    private static AutoTileTypeField getAutotileType(UIMapView map, int x, int y, int layer, AutoTileTypeField[] atBases, AutoTileTypeField preferred) {
        if (map.mapTable.outOfBounds(x, y))
            return preferred;
        int m = map.mapTable.getTiletype(x, y, layer);
        for (int i = 0; i < atBases.length; i++)
            if (atBases[i].start <= m)
                if ((atBases[i].start + atBases[i].length) > m)
                    return atBases[i];
        return null;
    }

    public static void updateAutotile(UIMapView map, AutoTileTypeField[] atBases, int x, int y, int layer) {
        AutoTileTypeField myAT = getAutotileType(map, x, y, layer, atBases, null);
        if (myAT == null)
            return;

        int index = 0;
        int power = 1;
        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++) {
                if (i == 0)
                    if (j == 0)
                        continue;
                if (myAT.considersSameAs(getAutotileType(map, x + j, y + i, layer, atBases, myAT)))
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

    @Override
    public boolean shouldIgnoreDrag() {
        // When using "dangerous" tools, ignore drag.
        return subtool != 0;
    }

    public short getPlaceSelection(int i, int i1) {
        int selWidth = tileMaps[tabPane.tab].selWidth;
        int selHeight = tileMaps[tabPane.tab].selHeight;
        return (short) (tileMaps[tabPane.tab].getTCSelected() + (i % selWidth) + ((i1 % selHeight) * tileMaps[tabPane.tab].getSelectStride()));
    }

    private class FloodFillPoint {
        public final int tX, tY;

        public FloodFillPoint(int x, int y) {
            tX = x;
            tY = y;
        }

        public boolean contentMatches(int target) {
            if (map.mapTable.outOfBounds(tX, tY))
                return false;
            short here = map.mapTable.getTiletype(tX, tY, map.currentLayer);
            if (here == target)
                return true;
            AutoTileTypeField attf = getAutotileType(map, tX, tY, map.currentLayer, atBases, null);
            if (attf == null)
                return false;
            return (target >= attf.start) && (target < (attf.start + attf.length));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FloodFillPoint))
                return false;
            FloodFillPoint ffp = (FloodFillPoint) o;
            if (ffp.tX != tX)
                return false;
            if (ffp.tY != tY)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return tX + (tY << 16);
        }
    }

}
