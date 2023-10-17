/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.render.IGrDriver;
import gabien.ui.UIButton;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;

/**
 * Needed for Demetrius's 'edit colour' request
 * Created on August 04, 2018.
 */
public class UIColourSwatchButton extends UIButton<UIColourSwatchButton> {
    public int col = 0x80FF00FF;

    public UIColourSwatchButton(int paletteRGB, int fontSize, Runnable runnable) {
        super(getRecommendedBorderWidth(fontSize));
        col = paletteRGB;
        int margin = fontSize / 8;
        onClick = runnable;
        // See rationale in gabien-core UILabel. Regarding width: as fontSize includes a margin, two fontSizes include two margins.
        Rect sz = new Rect(0, 0, fontSize * 2, fontSize + margin);
        setWantedSize(sz);
        setForcedBounds(null, new Rect(sz));
    }

    @Override
    public void renderContents(boolean drawBlack, IGrDriver igd) {
        int bw = getBorderWidth();
        Size sz = getSize();
        UIColourSwatch.doRender(igd, col, bw, bw, sz.width - (bw * 2), sz.height - (bw * 2));
    }
}
