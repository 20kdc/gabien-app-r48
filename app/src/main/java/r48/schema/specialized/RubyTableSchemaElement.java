/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import java.util.LinkedList;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.ui.*;
import gabien.ui.elements.UINumberBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.Size;
import gabien.uslx.io.ByteArrayMemoryish;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.RubyTable;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.specialized.tbleditors.ITableCellEditor;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.EmbedDataSlot;
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
    public final EmbedDataKey<Double> scrollPointKey = new EmbedDataKey<>();
    public final EmbedDataKey<Integer> gridSelectionKey = new EmbedDataKey<>();
    public final EmbedDataKey<Double> gridScrollKey = new EmbedDataKey<>();

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
    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final IRIO targV = extractTarget(target);
        final RubyTable targ = new RubyTable(targV.editUser());
        final IRIO width = widthVar == null ? null : widthVar.getRW(target);
        final IRIO height = heightVar == null ? null : heightVar.getRW(target);

        final EmbedDataSlot<Double> gridScrollSlot = launcher.embedSlot(target, gridScrollKey, 0.0d);
        final EmbedDataSlot<Integer> gridSelectionSlot = launcher.embedSlot(target, gridSelectionKey, 0);

        final TileHelper initialTileHelper = baseInitializeHelper(target);
        Size gridSize = getGridSize(initialTileHelper);
        final UIGrid uig = new UIGrid(app, gridSize.width, gridSize.height, targ.width * targ.height) {
            private TileHelper tileHelper = initialTileHelper;

            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
                int tX = t % targ.width;
                int tY = t / targ.width;
                if (!targ.coordAccessible(tX, tY))
                    return;
                tileHelper = baseTileDraw(target, t, x, y, igd, tileHelper);
                if (allowTextdraw) {
                    igd.clearRect(0, 0, 0, x, y, tileSizeW, app.f.gridTH);
                    for (int i = 0; i < targ.planeCount; i++)
                        GaBIEn.engineFonts.drawString(igd, x, y + (i * app.f.gridTH), Integer.toHexString(targ.getTiletype(t % targ.width, t / targ.width, i) & 0xFFFF), false, false, app.f.gridTH);
                }
            }

            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
                // Lack of any better place.
                gridScrollSlot.value = uivScrollbar.scrollPoint;
                gridSelectionSlot.value = getSelected();
                super.update(deltaTime, selected, peripherals);
            }
        };

        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(launcher, scrollPointKey, target);
        LinkedList<UIElement> uiSVLList = new LinkedList<>();
        final int[] planeValues = new int[targ.planeCount];
        final Runnable editorOnSelChange = tableCellEditor.createEditor(uiSVLList, planeValues, () -> {
            int sel = uig.getSelected();
            int tX = sel % targ.width;
            int tY = sel / targ.width;
            for (int i = 0; i < planeValues.length; i++)
                targ.setTiletype(tX, tY, i, (short) planeValues[i]);
            path.changeOccurred(false);
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
                    int p = targ.getTiletype(tX, tY, 0);
                    int p2 = baseFlipBits(p);
                    if (p != p2) {
                        targ.setTiletype(tX, tY, 0, p2);
                        path.changeOccurred(false);
                    }
                }
            }
        };

        uig.setSelected(gridSelectionSlot.value);
        uig.uivScrollbar.scrollPoint = gridScrollSlot.value;

        if (allowResize) {
            final UINumberBox wNB = new UINumberBox(targ.width, app.f.tableSizeTH);
            wNB.onEdit = () -> {
                if (wNB.getNumber() < 0)
                    wNB.setNumber(0);
            };
            final UINumberBox hNB = new UINumberBox(targ.height, app.f.tableSizeTH);
            hNB.onEdit = () -> {
                if (hNB.getNumber() < 0)
                    hNB.setNumber(0);
            };
            UIElement uie = new UISplitterLayout(wNB, hNB, false, 1, 2);
            uiSVLList.add(uie);
            uiSVLList.add(new UITextButton(T.g.bResize, app.f.tableResizeTH, () -> {
                int w = (int) wNB.getNumber();
                if (w < 0)
                    w = 0;
                int h = (int) hNB.getNumber();
                if (h < 0)
                    h = 0;
                ByteArrayMemoryish r2 = targ.resize(w, h, defVals);
                if (width != null)
                    width.setFX(w);
                if (height != null)
                    height.setFX(h);
                targV.putBuffer(r2.data);
                path.changeOccurred(false);
            }));
        }
        uiSVL.panelsSet(uiSVLList);
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

    public int baseFlipBits(int p) {
        return p;
    }

    public Size getGridSize(TileHelper th) {
        int g = app.f.scaleGrid(32);
        return new Size(g, g);
    }
}
