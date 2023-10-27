/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.render.IGrDriver;
import gabien.ui.UIElement;
import gabien.ui.UILayer;
import gabien.uslx.append.Size;
import gabien.wsi.IPeripherals;

/**
 * Used as part of a palette.
 * October 8th, 2017
 */
public class UIColourSwatch extends UIElement {
    public int col;

    public UIColourSwatch(int icol) {
        col = icol;
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

    }

    @Override
    public void renderLayer(IGrDriver igd, UILayer layer) {
        if (layer != UILayer.Content)
            return;
        Size bounds = getSize();
        doRender(igd, col, 0, 0, bounds.width, bounds.height);
    }

    public static void doRender(IGrDriver igd, int col, int x, int y, int width, int height) {
        final int a = ((col & 0xFF000000) >> 24) & 0xFF;
        final int r = (col & 0xFF0000) >> 16;
        final int g = (col & 0xFF00) >> 8;
        final int b = (col & 0xFF);
        igd.clearRect(r, g, b, x, y, width / 2, height);
        igd.clearRect(a, a, a, (width / 2) + x, y, width - (width / 2), height);
    }
}
