/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;
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

            tileMaps[i] = new UITileGrid(map.mapTable.renderer, map.currentLayer, tileTabs[i].atProcessing, tileTabs[i].visTilesNormal, tileTabs[i].visTilesHover, lText, FontSizes.getSpriteScale());
        }

        tabPane = new UITabPane(FontSizes.tilesTabTextHeight, true, false, FontSizes.tilesTabScrollersize);
        for (UIElement uie : tileMaps)
            tabPane.addTab(new TabUtils.Tab(uie, new TabUtils.TabIcon[] {}));
        atBases = map.mapTable.renderer.tileRenderer.indicateATs();

        // Begin subtool bar...

        subtool = 0;
        final LinkedList<UIButton> options = new LinkedList<UIButton>();

        UITextButton baseTool = new UITextButton(TXDB.get("Pen"), FontSizes.atSubtoolTextHeight, new Runnable() {
            @Override
            public void run() {
                for (UIButton utb : options)
                    utb.state = false;
                options.get(0).state = true;
                subtool = 0;
            }
        }).togglable(true);
        options.add(baseTool);

        UIAppendButton uab = new UIAppendButton(TXDB.get("Rectangle"), baseTool, new Runnable() {
            @Override
            public void run() {
                for (UIButton utb : options)
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
                for (UIButton utb : options)
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
        int sel = tileMaps[tab].getSelected();
        int[] actTiles = tileTabs[tab].actTiles;
        int lvm = sel + (px % tileMaps[tab].selWidth) + ((py % tileMaps[tab].selHeight) * tileMaps[tab].getSelectStride());
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
    public short shouldDrawAt(boolean mouse, int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        if (layer != currentLayer)
            return there;
        if (!mouse)
            return there;

        int tab = tabPane.getTabIndex();
        // give up
        if (tab == -1)
            return there;

        if (subtool != 0) {
            if (cx == tx)
                if (cy == ty)
                    return (short) tileTabs[tab].actTiles[tileMaps[tab].getSelected()];
            return there;
        }
        if (tx < cx)
            return there;
        if (ty < cy)
            return there;
        if (tx >= cx + tileMaps[tab].selWidth)
            return there;
        if (ty >= cy + tileMaps[tab].selHeight)
            return there;
        int px = tx - cx;
        int py = ty - cy;
        return getTCSelected(px, py);
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
        int tab = tabPane.getTabIndex();
        // give up
        if (tab == -1)
            return;
        if (subtool == 1) {
            // Tool 1: Rectangle
            mapToolContext.accept(new UIMTAutotileRectangle(this, x, y, tileTabs[tab].atProcessing));
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
                        map.mapTable.setTiletype(ffp.tX, ffp.tY, map.currentLayer, getTCSelected(ffp.tX - x, ffp.tY - y));
                        investigatePoints.add(new FloodFillPoint(ffp.tX - 1, ffp.tY));
                        investigatePoints.add(new FloodFillPoint(ffp.tX + 1, ffp.tY));
                        investigatePoints.add(new FloodFillPoint(ffp.tX, ffp.tY - 1));
                        investigatePoints.add(new FloodFillPoint(ffp.tX, ffp.tY + 1));
                    }
                }
            }
            if (tileTabs[tab].atProcessing) {
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
        performGeneralRectangle(layer, x, y, x, y, x + tileMaps[tab].selWidth - 1, y + tileMaps[tab].selHeight - 1);
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
        return "T" + tileTabs[tab].actTiles[tileMaps[tab].getSelected()];
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

    @Override
    public boolean shouldIgnoreDrag() {
        // When using "dangerous" tools, ignore drag.
        return subtool != 0;
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
