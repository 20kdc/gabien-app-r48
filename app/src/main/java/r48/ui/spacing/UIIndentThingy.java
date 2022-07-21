/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.spacing;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.IPointer;
import gabien.ui.IPointerReceiver;
import gabien.ui.UIBorderedElement;
import gabien.ui.UIElement;

/**
 * Provides the correct amount of spacing to indicate an indentation level,
 * optionally providing a visual indicator of a selected area.
 * Created on July 18th, 2018
 */
public class UIIndentThingy extends UIElement {
    public final int indent, unit, selectUnit;
    public int selected;
    public Runnable onClick;

    public UIIndentThingy(int u, int su, int i, int s, Runnable on) {
        super((i * u) + su, u);
        indent = i;
        unit = u;
        selectUnit = su;
        selected = s;
        onClick = on;
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
    }

    @Override
    public void render(IGrDriver igd) {
        int bW = Math.max(selectUnit / 6, 1);
        int height = getSize().height;
        UIBorderedElement.drawBorder(igd, (selected > 32) ? 1 : 0, bW, 0, 0, selectUnit, height);
        igd.clearRect(selected / 3, (selected * 2) / 3, selected, bW, bW, selectUnit - (bW * 2), height - (bW * 2));
        igd.clearRect(selected / 2, (selected * 5) / 6, selected, bW * 2, bW * 2, selectUnit- (bW * 4), height - (bW * 4));
    }

    @Override
    public IPointerReceiver handleNewPointer(IPointer state) {
        if (state.getType() == IPointer.PointerType.Generic)
            if (onClick != null)
                onClick.run();
        return super.handleNewPointer(state);
    }
}
