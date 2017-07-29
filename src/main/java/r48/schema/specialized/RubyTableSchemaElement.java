/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.schema.SchemaElement;
import r48.schema.specialized.tbleditors.ITableCellEditor;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIGrid;
import gabien.ui.UIScrollLayout;

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
public class RubyTableSchemaElement<TileHelper> extends SchemaElement {
    public final int defW;
    public final int defH;
    public final int planes;
    public final String iVar, widthVar, heightVar;
    public final ITableCellEditor tableCellEditor;

    // This is imperfect but should do the job.
    private int lastSelectionCache;
    private double lastScrollCache;

    public final int[] defVals;

    public RubyTableSchemaElement(String iVar, String wVar, String hVar, int dw, int dh, int defL, ITableCellEditor tcl, int[] defV) {
        this.iVar = iVar;
        widthVar = wVar;
        heightVar = hVar;
        defW = dw;
        defH = dh;
        planes = defL;
        tableCellEditor = tcl;
        defVals = defV;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final RubyIO targV = iVar == null ? target : target.getInstVarBySymbol(iVar);
        final RubyTable targ = new RubyTable(targV.userVal);
        final RubyIO width = widthVar == null ? null : target.getInstVarBySymbol(widthVar);
        final RubyIO height = heightVar == null ? null : target.getInstVarBySymbol(heightVar);
        final UIGrid uig = new UIGrid(32, 32, targ.width * targ.height) {
            private TileHelper tileHelper;

            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrInDriver igd) {
                int tX = t % targ.width;
                int tY = t / targ.width;
                if (targ.outOfBounds(tX, tY))
                    return;
                tileHelper = baseTileDraw(target, t, x, y, igd, tileHelper);
                igd.clearRect(0, 0, 0, x, y, tileSizeW, FontSizes.gridTextHeight);
                for (int i = 0; i < targ.planeCount; i++)
                    UILabel.drawString(igd, x, y + (i * FontSizes.gridTextHeight), Integer.toHexString(targ.getTiletype(t % targ.width, t / targ.width, i) & 0xFFFF), false, FontSizes.gridTextHeight);
            }
        };

        UIScrollLayout uiSVL = new UIScrollLayout(true);
        final Runnable editorOnSelChange = tableCellEditor.createEditor(uiSVL, targV, uig, new Runnable() {
            @Override
            public void run() {
                path.changeOccurred(false);
            }
        });
        uig.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                lastScrollCache = uig.uivScrollbar.scrollPoint;
                lastSelectionCache = uig.getSelected();
                editorOnSelChange.run();
            }
        };

        uig.uivScrollbar.scrollPoint = lastScrollCache;
        uig.setSelected(lastSelectionCache);
        uig.onSelectionChange.run();

        final UINumberBox wNB = new UINumberBox(FontSizes.tableSizeTextHeight);
        wNB.number = targ.width;
        wNB.onEdit = new Runnable() {
            @Override
            public void run() {
                if (wNB.number < 0)
                    wNB.number = 0;
            }
        };
        final UINumberBox hNB = new UINumberBox(FontSizes.tableSizeTextHeight);
        hNB.number = targ.height;
        hNB.onEdit = new Runnable() {
            @Override
            public void run() {
                if (hNB.number < 0)
                    hNB.number = 0;
            }
        };
        UIElement uie = new UISplitterLayout(wNB, hNB, false, 1, 2);
        uie.setBounds(new Rect(0, 0, 128, uie.getBounds().height));
        uiSVL.panels.add(uie);
        uiSVL.panels.add(new UITextButton(FontSizes.tableResizeTextHeight, TXDB.get("Resize"), new Runnable() {
            @Override
            public void run() {
                int w = wNB.number;
                if (w < 0)
                     w = 0;
                int h = hNB.number;
                if (h < 0)
                    h = 0;
                RubyTable r2 = targ.resize(wNB.number, hNB.number, defVals);
                if (width != null)
                    width.fixnumVal = wNB.number;
                if (height != null)
                    height.fixnumVal = hNB.number;
                targV.userVal = r2.innerBytes;
                path.changeOccurred(false);
            }
        }));

        UIElement r = new UISplitterLayout(uig, uiSVL, false, 6, 8);
        r.setBounds(new Rect(0, 0, 128, 128));
        return r;
    }

    // Overridden in super-special tileset versions of this.
    // The idea is that TileHelper can contain any helper object needed.
    public TileHelper baseTileDraw(RubyIO target, int t, int x, int y, IGrInDriver igd, TileHelper th) {
        return null;
    }

    @Override
    public int maxHoldingHeight() {
        throw new RuntimeException("Really, don't try this.");
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath index, boolean setDefault) {
        if (iVar != null) {
            RubyIO st = target.getInstVarBySymbol(iVar);
            if (st == null) {
                st = new RubyIO();
                target.iVars.put(iVar, st);
            }
            target = st;
        }
        // Not a clue, so re-initialize if all else fails.
        // (This will definitely trigger if the iVar was missing)
        if (target.type != 'u') {
            target.setUser("Table", new RubyTable(defW, defH, planes, defVals).innerBytes);
            index.changeOccurred(true);
        }
    }
}
