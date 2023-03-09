/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.FontManager;
import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.*;
import r48.App;
import r48.RubyTable;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.specialized.tbleditors.ITableCellEditor;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIGrid;

/**
 * OLD:
 * Not really finished
 * Created on 12/29/16.
 * NEW:
 * Kind of finished
 * Worked on at 01/03/16, comment rewritten very, very early technically the next day,
 * after AutoTileRules was added
 * NEWER: Abstractified at midnight. That is, 0:00 February 18th 2017, where I count 8 hours after that as being the same day.
 * ---LATER STILL---
 * Checking for the monitorsSubelements requirement...
 * no, the resize does the correct corrections, I believe.
 */
public class RubyTableSchemaElement<TileHelper> extends BaseRubyTableSchemaElement {
    public final PathSyntax widthVar, heightVar;
    public final ITableCellEditor tableCellEditor;

    public boolean allowTextdraw = true;
    public boolean allowResize = true;

    // NOTE: Doesn't need SDB2-PS compat because it only just started using PS, thankfully
    public RubyTableSchemaElement(App app, PathSyntax iVar, PathSyntax wVar, PathSyntax hVar, int dim, int dw, int dh, int defL, ITableCellEditor tcl, int[] defV) {
        super(app, dw, dh, defL, dim, iVar, defV);
        widthVar = wVar;
        heightVar = hVar;
        tableCellEditor = tcl;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final IRIO targV = extractTarget(target);
        final RubyTable targ = new RubyTable(targV.getBuffer());
        final IRIO width = widthVar == null ? null : widthVar.get(target);
        final IRIO height = heightVar == null ? null : heightVar.get(target);

        final TileHelper initialTileHelper = baseInitializeHelper(target);
        Size gridSize = getGridSize(initialTileHelper);
        final UIGrid uig = new UIGrid(app, gridSize.width, gridSize.height, targ.width * targ.height) {
            private TileHelper tileHelper = initialTileHelper;

            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
                int tX = t % targ.width;
                int tY = t / targ.width;
                if (targ.outOfBounds(tX, tY))
                    return;
                tileHelper = baseTileDraw(target, t, x, y, igd, tileHelper);
                if (allowTextdraw) {
                    igd.clearRect(0, 0, 0, x, y, tileSizeW, app.f.gridTextHeight);
                    for (int i = 0; i < targ.planeCount; i++)
                        FontManager.drawString(igd, x, y + (i * app.f.gridTextHeight), Integer.toHexString(targ.getTiletype(t % targ.width, t / targ.width, i) & 0xFFFF), false, false, app.f.gridTextHeight);
                }
            }

            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
                // Lack of any better place.
                double v = uivScrollbar.scrollPoint;
                if (v <= 0)
                    v = 0;
                if (v >= 0.99)
                    v = 0.99;
                v += getSelected();
                launcher.setEmbedDouble(RubyTableSchemaElement.this, target, "blackbox", v);
                super.update(deltaTime, selected, peripherals);
            }
        };

        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(launcher, this, target);
        final int[] planeValues = new int[targ.planeCount];
        final Runnable editorOnSelChange = tableCellEditor.createEditor(uiSVL, planeValues, new Runnable() {
            @Override
            public void run() {
                int sel = uig.getSelected();
                int tX = sel % targ.width;
                int tY = sel / targ.width;
                for (int i = 0; i < planeValues.length; i++)
                    targ.setTiletype(tX, tY, i, (short) planeValues[i]);
                path.changeOccurred(false);
            }
        });
        uig.onSelectionChange = new Runnable() {
            int selectionOnLastCall = -1;

            @Override
            public void run() {
                int sel = uig.getSelected();
                int oldSel = selectionOnLastCall;
                selectionOnLastCall = sel;
                int tX = sel % targ.width;
                int tY = sel / targ.width;
                for (int i = 0; i < planeValues.length; i++)
                    planeValues[i] = targ.getTiletype(tX, tY, i);
                editorOnSelChange.run();
                if (oldSel == sel) {
                    short p = targ.getTiletype(tX, tY, 0);
                    short p2 = baseFlipBits(p);
                    if (p != p2) {
                        targ.setTiletype(tX, tY, 0, p2);
                        path.changeOccurred(false);
                    }
                }
            }
        };

        // and now time for your daily dose of magic:
        // the scroll value here holds both the selection & the scroll cache.
        Double selVal = (Double) launcher.getEmbedObject(RubyTableSchemaElement.this, target, "blackbox");
        if (selVal == null)
            selVal = -1d;
        // -1d + 0.99 = -0.01d
        // Clarify this situation first. Note that Math.floor extends to negative infinity, which is perfect for this
        int lastSelectionCache = (int) Math.floor(selVal);
        double lastScrollCache = selVal - lastSelectionCache;
        if (lastScrollCache <= 0)
            lastScrollCache = 0;
        if (lastScrollCache >= 0.985)
            lastScrollCache = 1;
        uig.setSelected(lastSelectionCache);
        uig.uivScrollbar.scrollPoint = lastScrollCache;

        if (allowResize) {
            final UINumberBox wNB = new UINumberBox(targ.width, app.f.tableSizeTextHeight);
            wNB.onEdit = new Runnable() {
                @Override
                public void run() {
                    if (wNB.number < 0)
                        wNB.number = 0;
                }
            };
            final UINumberBox hNB = new UINumberBox(targ.height, app.f.tableSizeTextHeight);
            hNB.onEdit = new Runnable() {
                @Override
                public void run() {
                    if (hNB.number < 0)
                        hNB.number = 0;
                }
            };
            UIElement uie = new UISplitterLayout(wNB, hNB, false, 1, 2);
            uiSVL.panelsAdd(uie);
            uiSVL.panelsAdd(new UITextButton(app.ts("Resize"), app.f.tableResizeTextHeight, new Runnable() {
                @Override
                public void run() {
                    int w = (int) wNB.number;
                    if (w < 0)
                        w = 0;
                    int h = (int) hNB.number;
                    if (h < 0)
                        h = 0;
                    RubyTable r2 = targ.resize(w, h, defVals);
                    if (width != null)
                        width.setFX(w);
                    if (height != null)
                        height.setFX(h);
                    targV.putBuffer(r2.innerBytes);
                    path.changeOccurred(false);
                }
            }));
        }
        return new UISplitterLayout(uig, uiSVL, false, 1);
    }

    // Overridden in super-special tileset versions of this.
    // The idea is that TileHelper can contain any helper object needed.
    public TileHelper baseTileDraw(IRIO target, int t, int x, int y, IGrDriver igd, TileHelper th) {
        return null;
    }

    public TileHelper baseInitializeHelper(IRIO target) {
        return null;
    }

    public short baseFlipBits(short p) {
        return p;
    }

    public Size getGridSize(TileHelper th) {
        int g = app.f.scaleGrid(32);
        return new Size(g, g);
    }
}
