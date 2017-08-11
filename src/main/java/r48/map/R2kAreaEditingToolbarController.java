/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.IEditingToolbarController;
import r48.map.UIMapView;
import r48.schema.util.SchemaPath;
import r48.ui.Art;

/**
 * Created on 11/08/17.
 */
public class R2kAreaEditingToolbarController implements IEditingToolbarController {
    public final RubyIO mapInfosRoot, areaInfo;
    public final int tileSize;
    public R2kAreaEditingToolbarController(int ts, RubyIO mapInfos, RubyIO mapInfo) {
        tileSize = ts;
        mapInfosRoot = mapInfos;
        areaInfo = mapInfo;
    }

    @Override
    public void noTool() {
        AppMain.nextMapTool = new UIMTAreaTool();
    }

    @Override
    public UIElement getBar() {
        return new UILabel(TXDB.get("Editing Area..."), FontSizes.mapLayertabTextHeight);
    }

    private class UIMTAreaTool extends UIPanel implements IMapViewCallbacks {

        public UILabel label;
        public String textA = TXDB.get("Click to define first point (old area shown)");
        public String textB = TXDB.get("Click again to define second point");

        public UIMTAreaTool() {
            label = new UILabel(textA, FontSizes.dialogWindowTextHeight);
            allElements.add(label);
            setBounds(label.getBounds());
        }

        @Override
        public void setBounds(Rect r) {
            super.setBounds(r);
            label.setBounds(new Rect(0, 0, r.width, r.height));
        }

        public int firstPointX, firstPointY;
        public boolean definingPoint2;

        @Override
        public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
            return there;
        }

        @Override
        public int wantOverlay(boolean minimap) {
            return 1;
        }

        @Override
        public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
            if (definingPoint2)
                if (tx == firstPointX)
                    if (ty == firstPointY)
                        Art.drawTarget(px, py, tileSize, igd);
        }

        @Override
        public void performGlobalOverlay(IGrDriver igd, int px, int py, int ol, boolean minimap) {
            int x = (int) (GaBIEn.getTime() * 4);
            if ((x & 1) == 0) {
                Rect r = getViewedRect();
                Art.drawSelectionBox(px + (r.x * tileSize) - 1, py + (r.y * tileSize) - 1, (r.width * tileSize) + 2, (r.height * tileSize) + 2, igd);
            }
        }

        private Rect getViewedRect() {
            RubyIO rect = areaInfo.getInstVarBySymbol("@area_rect");
            RubyIO l = rect.getInstVarBySymbol("@left");
            RubyIO u = rect.getInstVarBySymbol("@up");
            RubyIO r = rect.getInstVarBySymbol("@right");
            RubyIO d = rect.getInstVarBySymbol("@down");
            return new Rect((int) l.fixnumVal, (int) u.fixnumVal, (int) (r.fixnumVal - l.fixnumVal), (int) (d.fixnumVal - u.fixnumVal));
        }

        @Override
        public void confirmAt(int x, int y, int layer) {
            if (!definingPoint2) {
                firstPointX = x;
                firstPointY = y;
                definingPoint2 = true;
                label.Text = textB;
            } else {
                RubyIO rect = areaInfo.getInstVarBySymbol("@area_rect");
                RubyIO l = rect.getInstVarBySymbol("@left");
                RubyIO u = rect.getInstVarBySymbol("@up");
                RubyIO r = rect.getInstVarBySymbol("@right");
                RubyIO d = rect.getInstVarBySymbol("@down");
                l.fixnumVal = Math.min(firstPointX, x);
                u.fixnumVal = Math.min(firstPointY, y);
                r.fixnumVal = Math.max(firstPointX, x) + 1;
                d.fixnumVal = Math.max(firstPointY, y) + 1;
                AppMain.objectDB.objectRootModified(mapInfosRoot, new SchemaPath(AppMain.schemas.getSDBEntry("RPG::MapTree"), mapInfosRoot, null));
                label.Text = textA;
                definingPoint2 = false;
            }
        }

        @Override
        public boolean shouldIgnoreDrag() {
            return true;
        }
    }
}
