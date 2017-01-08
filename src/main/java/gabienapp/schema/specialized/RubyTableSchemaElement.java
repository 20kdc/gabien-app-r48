/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema.specialized;

import gabien.IGrInDriver;
import gabien.ui.*;
import gabienapp.RubyIO;
import gabienapp.RubyTable;
import gabienapp.schema.ISchemaElement;
import gabienapp.schema.IntegerSchemaElement;
import gabienapp.schema.util.ISchemaHost;
import gabienapp.schema.util.SchemaPath;
import gabienapp.ui.UIAppendButton;
import gabienapp.ui.UIGrid;
import gabienapp.ui.UIHHalfsplit;
import gabienapp.ui.UIScrollVertLayout;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OLD:
 * Not really finished
 * Created on 12/29/16.
 * NEW:
 * Kind of finished
 * Worked on at 01/03/16, comment rewritten very, very early technically the next day,
 *  after AutoTileRules was added
 */
public class RubyTableSchemaElement<TileHelper> implements ISchemaElement {
    public final int defW;
    public final int defH;
    public final int planes;
    public final String iVar, widthVar, heightVar;
    public RubyTableSchemaElement(String iVar, String wVar, String hVar, int dw, int dh, int defL) {
        this.iVar = iVar;
        widthVar = wVar;
        heightVar = hVar;
        defW = dw;
        defH = dh;
        planes = defL;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final RubyIO width = widthVar == null ? null : target.getInstVarBySymbol(widthVar);
        final RubyIO height = heightVar == null ? null : target.getInstVarBySymbol(heightVar);
        final RubyIO targV = target.getInstVarBySymbol(iVar);
        final RubyTable targ = new RubyTable(targV.userVal);
        final UIScrollVertLayout uiSVL = new UIScrollVertLayout();
        final UIGrid uig = new UIGrid(32, targ.width * targ.height) {
            private TileHelper tileHelper;
            @Override
            protected void drawTile(int t, int x, int y, IGrInDriver igd) {
                int tX = t % targ.width;
                int tY = t / targ.width;
                if (targ.outOfBounds(tX, tY))
                    return;
                tileHelper = baseTileDraw(target, t, x, y, igd, tileHelper);
                for (int i = 0; i < targ.planeCount; i++)
                    UILabel.drawString(igd, x, y + (i * 8), Integer.toHexString(targ.getTiletype(t % targ.width, t / targ.width, i) & 0xFFFF), false, false);
            }
        };
        final UINumberBox[] boxes = new UINumberBox[targ.planeCount];
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = new UINumberBox(false);
            boxes[i].number = targ.getTiletype(0, 0, i);
            boxes[i].onEdit = createOnEdit(targ, path, 0, 0, i, boxes[i]);
            uiSVL.panels.add(boxes[i]);
        }
        uig.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                int sel = uig.getSelected();
                int selX = sel % targ.width;
                int selY = sel / targ.width;
                boolean oob = targ.outOfBounds(selX, selY);
                for (int i = 0; i < boxes.length; i++) {
                    if (oob) {
                        boxes[i].number = 0;
                        boxes[i].onEdit = new Runnable() {
                            @Override
                            public void run() {
                            }
                        };
                        boxes[i].readOnly = true;
                        continue;
                    }
                    boxes[i].number = targ.getTiletype(selX, selY, i);
                    boxes[i].onEdit = createOnEdit(targ, path, selX, selY, i, boxes[i]);
                    boxes[i].readOnly = false;
                }
            }
        };
        final UINumberBox wNB = new UINumberBox(false);
        wNB.number = targ.width;
        final UINumberBox hNB = new UINumberBox(false);
        hNB.number = targ.height;
        UIElement uie = new UIHHalfsplit(1, 2, wNB, hNB);
        uie.setBounds(new Rect(0, 0, 128, 9));
        uiSVL.panels.add(uie);
        uiSVL.panels.add(new UITextButton(false, "Resize", new Runnable() {
            @Override
            public void run() {
                RubyTable r2 = new RubyTable(wNB.number, hNB.number, targ.planeCount);
                if (width != null)
                    width.fixnumVal = wNB.number;
                if (height != null)
                    height.fixnumVal = hNB.number;
                targV.userVal = r2.innerBytes;
                path.changeOccurred(false);
            }
        }));
        UIElement r = new UIHHalfsplit(7, 8, uig, uiSVL);
        r.setBounds(new Rect(0, 0, 128, 128));
        return r;
    }

    // Overridden in super-special tileset versions of this.
    // The idea is that TileHelper can contain any helper object needed.
    public TileHelper baseTileDraw(RubyIO target, int t, int x, int y, IGrInDriver igd, TileHelper th) {
        return null;
    }

    private Runnable createOnEdit(final RubyTable targ, final SchemaPath path, final int x, final int y, final int p, final UINumberBox unb) {
        return new Runnable() {
            @Override
            public void run() {
                targ.setTiletype(x, y, p, (short) unb.number);
                path.changeOccurred(false);
            }
        };
    }


    @Override
    public int maxHoldingHeight() {
        throw new RuntimeException("Really, don't try this.");
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath index, boolean setDefault) {
        RubyIO st = target.getInstVarBySymbol(iVar);
        if (st == null) {
            st = new RubyIO();
            target.iVars.put(iVar, st);
        }
        target = st;
        // Not a clue, so re-initialize if all else fails.
        // (This will definitely trigger if the iVar was missing)
        if (IntegerSchemaElement.ensureType(target, 'u', setDefault)) {
            target.userVal = new RubyTable(defW, defH, planes).innerBytes;
            target.symVal = "Table";
            index.changeOccurred(true);
        }
    }
}
