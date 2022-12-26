/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.ui.*;
import gabien.uslx.append.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;
import r48.ui.UIAppendButton;
import r48.ui.UINSVertLayout;
import r48.ui.UITileGrid;
import r48.ui.utilitybelt.FillAlgorithm;

import java.util.LinkedList;

/**
 * A monolithic 'all the main editing tools' class. Even a single instance of this class
 *  has to be adaptable to whatever layer & editing details it ends up with.
 * Created on 12/29/16.
 */
public class UIMTAutotile extends UIMTBase implements IMapViewCallbacks {
    // Sub-tools panel
    private UIElement subtoolBar;
    private int subtool = 0;
    // Set when first = true for all subtools to use.
    private int anchorX, anchorY;

    private UITabPane tabPane;
    private TileEditingTab[] tileTabs;
    private UITileGrid[] tileMaps;
    public final UIMapView map;
    public AutoTileTypeField[] atBases;

    public UIMTAutotile(IMapToolContext mv) {
        super(mv);
        map = mv.getMapView();
        int scale = setupView(true);
        // Properly set the tab pane into gear, then see if it's still having issues displaying tabs.
        // If so, give it as much extra space as possible without messing with the tile-count-width.
        tabPane.handleIncoming();
        setForcedBounds(null, new Rect(0, 0, (map.tileSize * scale * map.mapTable.renderer.tileRenderer.getRecommendedWidth()) + FontSizes.gridScrollersize, FontSizes.scaleGuess(200)));
        if (tabPane.getShortened())
            setForcedBounds(null, new Rect(0, 0, ((map.tileSize * scale * (map.mapTable.renderer.tileRenderer.getRecommendedWidth() + 1)) - 1) + FontSizes.gridScrollersize, FontSizes.scaleGuess(200)));
    }
    
    private int setupView(boolean inConstructor) {
        // Logic here:
        // UIMTAutotile's tools really don't work well as symbols, so that's been abandoned,
        //  but the current text squeeze is bad.
        // The solution is to go for the core problem, which is the contents being too small for the UI.
        // The maths are chosen such that 8x32 is sufficient, but anything below that gets a boost.
        int resultScale = FontSizes.getSpriteScale();
        if ((map.tileSize * map.mapTable.renderer.tileRenderer.getRecommendedWidth()) < 256)
            resultScale *= 2;

        // The bridge between the TileEditingTab strcuture and the older code.
        tileTabs = map.mapTable.renderer.tileRenderer.getEditConfig(map.currentLayer);
        tileMaps = new UITileGrid[tileTabs.length];
        for (int i = 0; i < tileTabs.length; i++) {
            String lText = tileTabs[i].localizedText;
            if (tileTabs[i].doNotUse) {
                lText = " " + lText + "<X>";
            } else {
                lText = " " + lText;
            }

            tileMaps[i] = new UITileGrid(map.mapTable.renderer, map.currentLayer, tileTabs[i].atProcessing, tileTabs[i].visTilesNormal, tileTabs[i].visTilesHover, lText, resultScale);
        }

        tabPane = new UITabPane(FontSizes.tilesTabTextHeight, true, false, FontSizes.tilesTabScrollersize);
        for (UIElement uie : tileMaps)
            tabPane.addTab(new UITabBar.Tab(uie, new UITabBar.TabIcon[] {}));
        atBases = map.mapTable.renderer.tileRenderer.indicateATs();

        // Begin subtool bar...

        subtool = 0;
        final LinkedList<UIButton<?>> options = new LinkedList<UIButton<?>>();

        UITextButton baseTool = new UITextButton(TXDB.get("Pen"), FontSizes.atSubtoolTextHeight, new Runnable() {
            @Override
            public void run() {
                for (UIButton<?> utb : options)
                    utb.state = false;
                options.get(0).state = true;
                subtool = 0;
            }
        }).togglable(true);
        options.add(baseTool);

        UIAppendButton uab = new UIAppendButton(TXDB.get("Rectangle"), baseTool, new Runnable() {
            @Override
            public void run() {
                for (UIButton<?> utb : options)
                    utb.state = false;
                options.get(1).state = true;
                subtool = 1;
            }
        }, FontSizes.atSubtoolTextHeight);
        uab.button.toggle = true;
        options.add(uab.button);

        uab = new UIAppendButton(TXDB.get("Fill"), uab, new Runnable() {
            @Override
            public void run() {
                for (UIButton<?> utb : options)
                    utb.state = false;
                options.get(2).state = true;
                subtool = 2;
            }
        }, FontSizes.atSubtoolTextHeight);
        uab.button.toggle = true;
        options.add(uab.button);

        subtoolBar = uab;

        changeInner(new UINSVertLayout(subtoolBar, tabPane), inConstructor);
        return resultScale;
    }

    public void refresh() {
        // Attempt to transfer state.
        int ti = tabPane.getTabIndex();

        int inTabIndex = 0;
        TileEditingTab tet = null;
        if (ti != -1) {
            tet = tileTabs[ti];
            inTabIndex = tileMaps[ti].getSelected();
        }
        setupView(false);
        if (tet != null) {
            for (int i = 0; i < tileTabs.length; i++) {
                if (!tileTabs[i].doNotUse) {
                    if (tileTabs[i].compatibleWith(tet)) {
                        tileMaps[i].setSelected(inTabIndex);
                        tabPane.selectTab(tileMaps[i]);
                        break;
                    }
                }
            }
        }
    }

    // -- Tool stuff --

    public short getTCSelected(int px, int py) {
        int tab = tabPane.getTabIndex();
        if (tab == -1) {
            System.err.println("getTCSelected called with -1 getTabIndex! This is a minor bug.");
            return 0;
        }
        UITileGrid map = tileMaps[tab];
        int sel = map.getSelected();
        int[] actTiles = tileTabs[tab].actTiles;
        int lvm = sel + UIElement.sensibleCellMod(px, map.selWidth) + (UIElement.sensibleCellMod(py, map.selHeight) * map.getSelectStride());
        if (lvm < 0) {
            System.err.println("Selection calculator error <0 in getTCSelected");
            return 0;
        }
        if (lvm >= actTiles.length) {
            System.err.println("Selection calculator error >l in getTCSelected");
            return 0;
        }
        return (short) actTiles[lvm];
    }

    @Override
    public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
        if (layer != currentLayer)
            return there;
        if (mouse == null)
            return there;
        if (mouse.pressed)
            return there;

        int tab = tabPane.getTabIndex();
        // give up
        if (tab == -1)
            return there;

        UITileGrid map = tileMaps[tab];

        if (subtool != 0) {
            if ((mouse.x == tx) && (mouse.y == ty)) {
                TileEditingTab tabInst = tileTabs[tab];
                int selectedLocalTileIndex = map.getSelected();
                if (tabInst.inActTilesRange(selectedLocalTileIndex))
                    return (short) tabInst.actTiles[selectedLocalTileIndex];
            }
            return there;
        }
        int px = tx - mouse.x;
        int py = ty - mouse.y;
        if (px < 0)
            return there;
        if (py < 0)
            return there;
        if (px >= map.selWidth)
            return there;
        if (py >= map.selHeight)
            return there;
        return getTCSelected(px, py);
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 0;
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, int l, boolean minimap) {

    }

    @Override
    public void confirmAt(final int x, final int y, int pixx, int pixy, final int layer, boolean first) {
        if (first) {
            anchorX = x;
            anchorY = y;
        }
        if (x < 0)
            return;
        if (y < 0)
            return;
        if (x >= map.mapTable.width)
            return;
        if (y >= map.mapTable.height)
            return;
        int tab = tabPane.getTabIndex();
        // give up
        if (tab == -1)
            return;
        if (subtool == 0) {
            // Tool 0: Default pen/selection.
            // Same algorithm as used in the image editor's copy/paste functionality.
            int rx = x - UIElement.sensibleCellMod(x - anchorX, tileMaps[tab].selWidth);
            int ry = y - UIElement.sensibleCellMod(y - anchorY, tileMaps[tab].selHeight);
            performGeneralRectangle(layer, anchorX, anchorY, rx, ry, rx + tileMaps[tab].selWidth - 1, ry + tileMaps[tab].selHeight - 1);
        } else if (subtool == 1) {
            if (!first)
                return;
            // Tool 1: Rectangle
            mapToolContext.accept(new UIMTAutotileRectangle(this, x, y, tileTabs[tab].atProcessing));
        } else if (subtool == 2) {
            if (!first)
                return;
            // Tool 2: Floodfill.
            // The rules on what is considered the same are:
            // 1. Any two tiles which have the same ID are the same.
            // 2. Any two tiles which are both part of an AT group and that is the same AT group, are the same.
            // 3. Anything else is not the same.
            final int key = map.mapTable.getTiletype(x, y, layer);
            FillAlgorithm fa = new FillAlgorithm(new IFunction<FillAlgorithm.Point, FillAlgorithm.Point>() {
                @Override
                public FillAlgorithm.Point apply(FillAlgorithm.Point point) {
                    if (map.mapTable.outOfBounds(point.x, point.y))
                        return null;
                    return point;
                }
            }, new IFunction<FillAlgorithm.Point, Boolean>() {
                @Override
                public Boolean apply(FillAlgorithm.Point point) {
                    short here = map.mapTable.getTiletype(point.x, point.y, layer);
                    if (here != key) {
                        AutoTileTypeField attf = getAutotileType(map, point.x, point.y, layer, atBases, null);
                        if (attf == null)
                            return false;
                        if (!attf.contains(key))
                            return false;
                    }
                    map.mapTable.setTiletype(point.x, point.y, layer, getTCSelected(point.x - x, point.y - y));
                    return true;
                }
            });
            fa.availablePointSet.add(new FillAlgorithm.Point(x, y));
            while (!fa.availablePointSet.isEmpty())
                fa.pass();
            if (tileTabs[tab].atProcessing)
                for (FillAlgorithm.Point ffp : fa.executedPointSet)
                    for (int i = -1; i < 2; i++)
                        for (int j = -1; j < 2; j++)
                            updateAutotile(map, atBases, ffp.x + i, ffp.y + j, layer);
            map.passModificationNotification();
        }
    }

    public void performGeneralRectangle(int layer, int ox, int oy, int x, int y, int mx, int my) {
        int tab = tabPane.getTabIndex();
        if (tab == -1)
            return;
        for (int px = x; px <= mx; px++) {
            if (px < 0)
                continue;
            if (px >= map.mapTable.width)
                continue;
            for (int py = y; py <= my; py++) {
                if (py < 0)
                    continue;
                if (py >= map.mapTable.height)
                    continue;
                map.mapTable.setTiletype(px, py, layer, getTCSelected(px - ox, py - oy));
            }
        }
        if (tileTabs[tab].atProcessing) {
            for (int px = x - 1; px <= mx + 1; px++) {
                if (px < 0)
                    continue;
                if (px >= map.mapTable.width)
                    continue;
                for (int py = y - 1; py <= my + 1; py++) {
                    if (py < 0)
                        continue;
                    if (py >= map.mapTable.height)
                        continue;
                    updateAutotile(map, atBases, px, py, layer);
                }
            }
        }
        map.passModificationNotification();
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
        // Also handles out-of-bounds
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
        int tab = tabPane.getTabIndex();
        // give up
        if (tab == -1)
            return "???";
        TileEditingTab tabInst = tileTabs[tab];
        int selectedLocalTileIndex = tileMaps[tab].getSelected();
        if (!tabInst.inActTilesRange(selectedLocalTileIndex))
            return "TMFAULT/" + tab + "/" + selectedLocalTileIndex;
        return "T" + tabInst.actTiles[selectedLocalTileIndex];
    }

    public void selectTile(short aShort) {
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < tileTabs.length; i++) {
                if ((pass == 0) && tileTabs[i].atProcessing)
                    continue;
                for (int j = 0; j < tileTabs[i].actTiles.length; j++) {
                    if (tileTabs[i].actTiles[j] == aShort) {
                        tabPane.selectTab(tileMaps[i]);
                        tileMaps[i].setSelected(j);
                        return;
                    }
                }
            }
        }
        System.err.println("Cannot find tile " + aShort);
    }
}
