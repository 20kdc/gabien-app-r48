/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.tbleditors;

import java.util.concurrent.atomic.AtomicReference;

import gabien.ui.IConsumer;
import gabien.ui.UINumberBox;
import gabien.ui.UIScrollLayout;
import r48.FontSizes;
import r48.RubyTable;
import r48.io.data.IRIO;
import r48.ui.UIGrid;

/**
 * Created on 18/2/17.
 */
public class DefaultTableCellEditor implements ITableCellEditor {
    @Override
    public Runnable createEditor(final UIScrollLayout base, final int[] planes, final Runnable changeOccurred) {
        final UINumberBox[] numbers = new UINumberBox[planes.length];
        for (int i = 0; i < planes.length; i++) {
            final int index = i;
            final UINumberBox box = new UINumberBox(planes[i], FontSizes.tableElementTextHeight);
            numbers[i] = box;
            box.onEdit = new Runnable() {
                @Override
                public void run() {
                    planes[index] = (int) box.number;
                    changeOccurred.run();
                }
            };
            base.panelsAdd(box);
        }
        return new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < planes.length; i++)
                    numbers[i].number = planes[i];
            }
        };
    }
}

