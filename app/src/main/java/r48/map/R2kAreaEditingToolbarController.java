/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.GaBIEn;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.App;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.maptools.UIMTBase;
import r48.schema.util.SchemaPath;
import r48.ui.Art;

/**
 * Created on 11/08/17.
 */
public class R2kAreaEditingToolbarController implements IEditingToolbarController {
    public final IObjectBackend.ILoadedObject mapInfosRoot;
    public final IRIO areaInfo;
    public final int tileSize;
    public final IMapToolContext mapToolContext;

    public R2kAreaEditingToolbarController(IMapToolContext mtc, IObjectBackend.ILoadedObject mapInfos, IRIO mapInfo) {
        tileSize = mtc.getMapView().tileSize;
        mapToolContext = mtc;
        mapInfosRoot = mapInfos;
        areaInfo = mapInfo;
    }

    @Override
    public void noTool() {
        mapToolContext.accept(new UIMTAreaTool());
    }

    @Override
    public UIElement getBar() {
        return new UILabel(TXDB.get("Editing Area..."), FontSizes.mapLayertabTextHeight);
    }

    @Override
    public boolean allowPickTile() {
        return false;
    }

    private class UIMTAreaTool extends UIMTBase implements IMapViewCallbacks {

        public UILabel label;
        public String textA = TXDB.get("Click to define first point (old area shown)");
        public String textB = TXDB.get("Click again to define second point");

        public UIMTAreaTool() {
            super(R2kAreaEditingToolbarController.this.mapToolContext);
            label = new UILabel(textA, FontSizes.dialogWindowTextHeight);
            changeInner(label, true);
        }

        public int firstPointX, firstPointY;
        public boolean definingPoint2;

        @Override
        public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
            return there;
        }

        @Override
        public int wantOverlay(boolean minimap) {
            return 1;
        }

        @Override
        public void performGlobalOverlay(MapViewDrawContext mvdc, int l, boolean minimap) {
            if (definingPoint2)
                mvdc.drawIndicator(firstPointX, firstPointY, MapViewDrawContext.IndicatorStyle.Target);
            int x = (int) (GaBIEn.getTime() * 4);
            if ((x & 1) == 0) {
                Rect r = getViewedRect();
                Art.drawSelectionBox((r.x * tileSize) - 1, (r.y * tileSize) - 1, (r.width * mvdc.tileSize) + 2, (r.height * mvdc.tileSize) + 2, 1, mvdc.igd);
            }
        }

        private Rect getViewedRect() {
            IRIO rect = areaInfo.getIVar("@area_rect");
            IRIO l = rect.getIVar("@left");
            IRIO u = rect.getIVar("@up");
            IRIO r = rect.getIVar("@right");
            IRIO d = rect.getIVar("@down");
            return new Rect((int) l.getFX(), (int) u.getFX(), (int) (r.getFX() - l.getFX()), (int) (d.getFX() - u.getFX()));
        }

        @Override
        public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
            if (!first)
                return;
            if (!definingPoint2) {
                firstPointX = x;
                firstPointY = y;
                definingPoint2 = true;
                label.text = textB;
            } else {
                IRIO rect = areaInfo.getIVar("@area_rect");
                IRIO l = rect.getIVar("@left");
                IRIO u = rect.getIVar("@up");
                IRIO r = rect.getIVar("@right");
                IRIO d = rect.getIVar("@down");
                l.setFX(Math.min(firstPointX, x));
                u.setFX(Math.min(firstPointY, y));
                r.setFX(Math.max(firstPointX, x) + 1);
                d.setFX(Math.max(firstPointY, y) + 1);
                App app = mapToolContext.getMapView().app;
                app.odb.objectRootModified(mapInfosRoot, new SchemaPath(app.sdb.getSDBEntry("RPG::MapTree"), mapInfosRoot));
                label.text = textA;
                definingPoint2 = false;
            }
        }
    }
}
