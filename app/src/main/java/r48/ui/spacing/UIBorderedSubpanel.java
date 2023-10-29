/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.spacing;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;

/**
 * Doesn't draw anything, just a sort of thing that's there
 * Created on September 03, 2018.
 */
public class UIBorderedSubpanel extends UIElement.UIPanel {
    private UIElement innerPanel;
    private int bw;
    private boolean enableBorder = true;

    public UIBorderedSubpanel(UIElement ip, int border) {
        innerPanel = ip;
        layoutAddElement(ip);
        bw = border;
    }

    @Override
    public int layoutGetHForW(int width) {
        int wm = width - (bw * 2);
        if (wm < innerPanel.getWantedSize().width)
            return innerPanel.layoutGetHForW(width);
        return innerPanel.layoutGetHForW(wm) + (bw * 2);
    }

    @Override
    public int layoutGetWForH(int height) {
        int hm = height - (bw * 2);
        if (hm < innerPanel.getWantedSize().height)
            return innerPanel.layoutGetWForH(height);
        return innerPanel.layoutGetWForH(hm) + (bw * 2);
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        Size s2 = innerPanel.getWantedSize();
        return new Size(s2.width + (bw * 2), s2.height + (bw * 2));
    }

    @Override
    protected void layoutRunImpl() {
        enableBorder = true;

        Size s = getSize();
        Size s2 = innerPanel.getWantedSize();
        Rect plannedSize = new Rect(bw, bw, s.width - (bw * 2), s.height - (bw * 2));
        if ((s2.width > plannedSize.width) || (s2.height > plannedSize.height))
            enableBorder = false;
        if (!enableBorder) {
            innerPanel.setForcedBounds(this, new Rect(s));
        } else {
            innerPanel.setForcedBounds(this, plannedSize);
        }
    }
}
