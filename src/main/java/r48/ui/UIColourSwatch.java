/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;

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
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        Rect bounds = getBounds();
        final int a = ((col & 0xFF000000) >> 24) & 0xFF;
        final int r = (col & 0xFF0000) >> 16;
        final int g = (col & 0xFF00) >> 8;
        final int b = (col & 0xFF);
        igd.clearRect(r, g, b, ox, oy, bounds.width / 2, bounds.height);
        igd.clearRect(a, a, a, ox + (bounds.width / 2), oy, bounds.width - (bounds.width / 2), bounds.height);
    }
}
