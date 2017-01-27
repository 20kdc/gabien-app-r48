/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;

import java.util.LinkedList;

/**
 * BLAH^
 * BLAH|
 * BLAHV
 * Created on 12/29/16.
 */
public class UIScrollVertLayout extends UIPanel {
    public UIVScrollbar scrollbar;
    public LinkedList<UIElement> panels = new LinkedList<UIElement>();
    public int scrollLength = 0;
    public UIScrollVertLayout() {
        scrollbar = new UIVScrollbar();
    }
    public void runLayout() {
        Rect r = getBounds();
        allElements.clear();
        scrollbar.setBounds(new Rect(r.width - 32, 0, 32, r.height));
        allElements.add(scrollbar);
        scrollLength = 0;
        for (UIElement p : panels)
            scrollLength += p.getBounds().height;
    }

    @Override
    public void updateAndRender(int ox, int oy, double DeltaTime, boolean select, IGrInDriver igd) {
        allElements.clear();
        Rect bounds = getBounds();
        int scrollHeight = scrollLength - bounds.height;
        int appliedScrollbarWidth = 32;
        if (scrollHeight <= 0) {
            scrollHeight = 0;
            // no need for the scrollbar
            appliedScrollbarWidth = 0;
        } else {
            allElements.add(scrollbar);
        }
        int rY = (int) (-scrollbar.scrollPoint * scrollHeight);
        for (UIElement p : panels) {
            Rect b = p.getBounds();
            p.setBounds(new Rect(0, rY, bounds.width - appliedScrollbarWidth, b.height));
            int oRY = rY;
            rY += b.height;
            if (oRY < 0)
                continue;
            if (rY > bounds.height)
                continue;
            allElements.add(p);
        }
        super.updateAndRender(ox, oy, DeltaTime, select, igd);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        runLayout();
    }
}
