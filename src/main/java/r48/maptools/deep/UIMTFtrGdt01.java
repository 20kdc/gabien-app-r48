/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools.deep;

import gabien.IGrDriver;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;
import r48.maptools.UIMTBase;
import r48.ui.UIAppendButton;
import r48.ui.utilitybelt.FillAlgorithm;

/**
 * Created on December 30, 2018.
 */
public class UIMTFtrGdt01 extends UIMTBase implements IMapViewCallbacks {
    public boolean placingPen = true;
    public MOutline workspace = new MOutline();
    public int lcrX, lcrY;

    public UIMTFtrGdt01(IMapToolContext o) {
        super(o);

        changeInner(new UIAppendButton(TXDB.get("Confirm"), new UIAppendButton(TXDB.get("Raise Pen"), new UITextButton(TXDB.get("Undo"), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                TOutline.Line l = workspace.removeLast();
                if (l != null) {
                    lcrX = l.a.x;
                    lcrY = l.a.y;
                } else {
                    placingPen = true;
                }
            }
        }), new Runnable() {
            @Override
            public void run() {
                placingPen = true;
            }
        }, FontSizes.schemaFieldTextHeight), new Runnable() {
            @Override
            public void run() {
                UIMapView umv = mapToolContext.getMapView();
                for (FillAlgorithm.Point p : workspace.getAllInvolvedTiles())
                    umv.mapTable.setTiletype(p.x, p.y, umv.currentLayer, workspace.getOutlineForTile(p.x, p.y).getUsedIdReal());
                umv.passModificationNotification();
                workspace = new MOutline();
                placingPen = true;
            }
        }, FontSizes.schemaFieldTextHeight), true);
    }

    @Override
    public String toString() {
        return TXDB.get("Deep Water, layer ") + mapToolContext.getMapView().currentLayer;
    }

    @Override
    public short shouldDrawAt(boolean mouseAllowed, int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        if (layer != currentLayer)
            return there;
        TOutline mo = workspace.getOutlineForTile(tx, ty);
        if (mo != null)
            return mo.getUsedIdReal();
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 1;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
        int ps2 = mapToolContext.getMapView().tileSize / 2;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int itx = (tx * 2) + i, ity = (ty * 2) + j;
                if (!placingPen) {
                    Runnable optval = optValidity(itx, ity);
                    boolean gbi = (itx == lcrX) && (ity == lcrY);
                    int gb = gbi ? 0 : 255;
                    if ((optval != null) || gbi)
                        igd.clearRect(255, gb, gb, px + (i * ps2) - 1, py + (j * ps2) - 1, 2, 2);
                } else {
                    if ((i == 1) && (j == 1))
                        continue;
                    igd.clearRect(255, 255, 255, px + (i * ps2) - 1, py + (j * ps2) - 1, 2, 2);
                }
            }
        }
    }

    private Runnable optValidity(final int i, final int i1) {
        if (Math.abs(i - lcrX) > 1)
            return null;
        if (Math.abs(i1 - lcrY) > 1)
            return null;
        final TOutline.Line tl = new TOutline.Line(lcrX, lcrY, i, i1);
        if (!MOutline.addValidityForLine(null, tl))
            return null;
        if (workspace.containsLine(tl))
            return null;
        boolean b = workspace.validWith(tl);
        if (b)
            return new Runnable() {
                @Override
                public void run() {
                    workspace.append(tl);
                    lcrX = i;
                    lcrY = i1;
                }
            };
        return null;
    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer) {
        int ps2 = mapToolContext.getMapView().tileSize / 2;
        int cpcX = (x * 2 * ps2) + pixx;
        int cpcY = (y * 2 * ps2) + pixy;
        cpcX += ps2 / 2;
        cpcY += ps2 / 2;
        int hsx = sensibleCellMod(cpcX, ps2);
        int hsy = sensibleCellMod(cpcY, ps2);
        if (hsx < (ps2 / 4))
            return;
        if (hsy < (ps2 / 4))
            return;
        if (hsx >= (ps2 - (ps2 / 4)))
            return;
        if (hsy >= (ps2 - (ps2 / 4)))
            return;
        cpcX = sensibleCellDiv(cpcX, ps2);
        cpcY = sensibleCellDiv(cpcY, ps2);

        if (placingPen) {
            lcrX = cpcX;
            lcrY = cpcY;
            placingPen = false;
        } else {
            Runnable rr = optValidity(cpcX, cpcY);
            if (rr != null)
                rr.run();
        }
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return false;
    }
}
