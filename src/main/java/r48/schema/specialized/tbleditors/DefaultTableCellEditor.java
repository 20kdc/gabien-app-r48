/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.tbleditors;

import gabien.ui.UINumberBox;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.ui.UIGrid;
import gabien.ui.UIScrollLayout;

/**
 * Created on 2/18/17.
 */
public class DefaultTableCellEditor implements ITableCellEditor {
    @Override
    public Runnable createEditor(final UIScrollLayout panel, final RubyIO targV, final UIGrid uig, final Runnable changeOccurred) {
        final RubyTable targ = new RubyTable(targV.userVal);
        final UINumberBox[] boxes = new UINumberBox[targ.planeCount];
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = new UINumberBox(FontSizes.tableElementTextHeight);
            boxes[i].number = targ.getTiletype(0, 0, i);
            boxes[i].onEdit = createOnEdit(targ, changeOccurred, 0, 0, i, boxes[i]);
            panel.panels.add(boxes[i]);
        }
        return new Runnable() {
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
                    boxes[i].onEdit = createOnEdit(targ, changeOccurred, selX, selY, i, boxes[i]);
                    boxes[i].readOnly = false;
                }
            }
        };
    }

    private Runnable createOnEdit(final RubyTable targ, final Runnable changeOccurred, final int x, final int y, final int p, final UINumberBox unb) {
        return new Runnable() {
            @Override
            public void run() {
                targ.setTiletype(x, y, p, (short) unb.number);
                changeOccurred.run();
            }
        };
    }
}

