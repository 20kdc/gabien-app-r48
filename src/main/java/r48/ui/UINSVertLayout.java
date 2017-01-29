/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;

/**
 * Used for cases where there's an upper "bar" section, and a lower "main" section.
 * Created on 1/6/17.
 */
public class UINSVertLayout extends UIPanel {
    public UIElement upper;
    public UIElement lower;
    public UINSVertLayout(UIElement a, UIElement b) {
        Rect ab = a.getBounds();
        Rect bb = b.getBounds();
        int w = Math.max(ab.width, bb.width);
        int h = ab.height + bb.height;
        upper = a;
        lower = b;
        setBounds(new Rect(0, 0, w, h));
        allElements.add(upper);
        allElements.add(lower);
    }
    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        int uh = upper.getBounds().height;
        upper.setBounds(new Rect(0, 0, r.width, uh));
        lower.setBounds(new Rect(0, uh, r.width, r.height - uh));
    }
}
