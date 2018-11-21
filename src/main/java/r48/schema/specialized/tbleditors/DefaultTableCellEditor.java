/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.tbleditors;

import gabien.ui.UINumberBox;
import gabien.ui.UIScrollLayout;
import r48.FontSizes;
import r48.RubyTable;
import r48.io.data.IRIO;
import r48.ui.UIGrid;

/**
 * Created on 2/18/17.
 */
public class DefaultTableCellEditor implements ITableCellEditor {
    @Override
    public Runnable createEditor(final UIScrollLayout panel, final IRIO targV, final UIGrid uig, final Runnable changeOccurred) {
        final RubyTable targ = new RubyTable(targV.getBuffer());
        final UINumberBox[] boxes = new UINumberBox[targ.planeCount];
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = new UINumberBox(targ.getTiletype(0, 0, i), FontSizes.tableElementTextHeight);
            boxes[i].onEdit = createOnEdit(targ, changeOccurred, 0, 0, i, boxes[i]);
            panel.panelsAdd(boxes[i]);
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

