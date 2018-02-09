/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIButton;

/**
 * Part of plan "Reduce translator work"
 * Created on January 28th, 2018
 */
public class UISymbolButton extends UIButton {
    public Art.Symbol symbol;

    public UISymbolButton(int fontSize, Art.Symbol symbolIndex, Runnable runnable) {
        int margin = fontSize / 8;
        symbol = symbolIndex;
        // See rationale in gabien-core classes.
        setBounds(new Rect(0, 0, fontSize + (margin * 2), fontSize + margin));
        onClick = runnable;
    }


    @Override
    public void updateAndRender(int ox, int oy, double DeltaTime, boolean selected, IGrInDriver igd) {
        super.updateAndRender(ox, oy, DeltaTime, selected, igd);
        int po = getPressOffset(getBounds().height);
        ox += po;
        oy += po;
        if (state)
            oy += po * 3;
        Art.drawSymbol(igd, symbol, ox, oy, getBounds().height - (po * 2), false);
    }
}
