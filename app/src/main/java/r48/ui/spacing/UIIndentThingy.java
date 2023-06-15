/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.spacing;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.IPointer;
import gabien.ui.IPointerReceiver;
import gabien.ui.UIBorderedElement;
import gabien.ui.UIElement;
import gabien.ui.theming.Theme;

/**
 * Provides the correct amount of spacing to indicate an indentation level,
 * optionally providing a visual indicator of a selected area.
 * Created on July 18th, 2018
 */
public class UIIndentThingy extends UIElement {
    public static final int SELECTED_NONE = 0;
    public static final int SELECTED_NOT_THIS = 32;
    public static final int SELECTED_HEAD = 255;
    public static final int SELECTED_TRAIL = 192;

    public final int indent, unit, selectUnit;
    /**
     * Selection force. 0 is minimum, 255 is maximum.
     */
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
        Theme theme = getTheme();
        UIBorderedElement.drawBorder(theme, igd, (selected > 32) ? 1 : 0, bW, 0, 0, selectUnit, height);
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
