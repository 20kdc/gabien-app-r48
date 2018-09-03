/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.ui;

import gabien.ui.Rect;
import gabien.ui.Size;
import gabien.ui.UIElement;

/**
 * Doesn't draw anything, just a sort of thing that's there
 * Created on September 03, 2018.
 */
public class UIBorderedSubpanel extends UIElement.UIPanel {
    private UIElement innerPanel;
    private int bw;

    public UIBorderedSubpanel(UIElement ip, int border) {
        innerPanel = ip;
        layoutAddElement(ip);
        bw = border;
    }

    @Override
    public void runLayout() {
        Size s = getSize();
        Rect plannedSize = new Rect(bw, bw, s.width - (bw * 2), s.height - (bw * 2));
        boolean cannotSFB = innerPanel.getSize().sizeEquals(plannedSize);
        if (!cannotSFB) {
            innerPanel.setForcedBounds(this, plannedSize);
        } else {
            innerPanel.runLayoutLoop();
        }
        Size s2 = innerPanel.getWantedSize();
        setWantedSize(new Size(s2.width + (bw * 2), s2.height + (bw * 2)));
    }
}
