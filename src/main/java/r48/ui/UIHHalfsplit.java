/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;

/**
 * Fractions, for users of all ages!*
 * <p/>
 * * And all everything else, for that matter.
 * If a cat knows fractions, then sure, they can use this class.
 * Created on 12/29/16.
 */
public class UIHHalfsplit extends UIPanel {
    public UIElement left, right;
    public int lP, lD;

    public UIHHalfsplit(int fP, int fD, UIElement l, UIElement r) {
        lP = fP;
        lD = fD;
        left = l;
        right = r;
        allElements.add(l);
        allElements.add(r);
        int height = l.getBounds().height;
        if (r.getBounds().height > height)
            height = r.getBounds().height;
        setBounds(new Rect(0, 0, 128, height));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        int major = (r.width / lD) * lP;
        int rem = r.width - major;
        left.setBounds(new Rect(0, 0, major, r.height));
        right.setBounds(new Rect(major, 0, rem, r.height));
    }
}
