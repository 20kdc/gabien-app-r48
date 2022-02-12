/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrDriver;
import gabien.ui.Rect;
import gabien.ui.UIButton;

/**
 * Part of plan "Reduce translator work"
 * Created on January 28th, 2018
 */
public class UISymbolButton extends UIButton<UISymbolButton> {
    public Art.Symbol symbol;

    public UISymbolButton(Art.Symbol symbolIndex, int fontSize, Runnable runnable) {
        super(getRecommendedBorderWidth(fontSize));
        int margin = fontSize / 8;
        symbol = symbolIndex;
        onClick = runnable;
        // See rationale in gabien-core UILabel. Note, though, that the width is smaller.
        Rect sz = new Rect(0, 0, fontSize + (margin * 2), fontSize + margin);
        setWantedSize(sz);
        setForcedBounds(null, new Rect(sz));
    }

    @Override
    public void renderContents(boolean textBlack, IGrDriver igd) {
        int bw = getBorderWidth();
        int sw = getSize().width;
        int sh = getSize().height;
        int efs = Math.min(sw, sh);
        int x = (sw - efs) / 2;
        int y = (sh - efs) / 2;
        Art.drawSymbol(igd, symbol, bw + x, bw + y, efs - (bw * 2), false, textBlack);
    }
}
