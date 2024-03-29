/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.tbleditors;

import java.util.LinkedList;

import gabien.ui.UIElement;
import gabien.ui.elements.UINumberBox;
import r48.App;

/**
 * Created on 18/2/17.
 */
public class DefaultTableCellEditor extends App.Svc implements ITableCellEditor {
    public DefaultTableCellEditor(App app) {
        super(app);
    }
    @Override
    public Runnable createEditor(final LinkedList<UIElement> base, final int[] planes, final Runnable changeOccurred) {
        final UINumberBox[] numbers = new UINumberBox[planes.length];
        for (int i = 0; i < planes.length; i++) {
            final int index = i;
            final UINumberBox box = new UINumberBox(planes[i], app.f.tableElementTH);
            numbers[i] = box;
            box.onEdit = new Runnable() {
                @Override
                public void run() {
                    planes[index] = (int) box.getNumber();
                    changeOccurred.run();
                }
            };
            base.add(box);
        }
        return new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < planes.length; i++)
                    numbers[i].setNumber(planes[i]);
            }
        };
    }
}

