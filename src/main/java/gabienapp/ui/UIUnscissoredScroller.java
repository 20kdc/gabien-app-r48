/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabienapp.ui;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;

/**
 * Used by help system stuff.
 * Created on 1/25/17.
 */
public class UIUnscissoredScroller extends UIPanel {
    private UIElement target;
    private boolean dragging = false;
    private int lastX, lastY;
    public UIUnscissoredScroller(UIElement inner) {
        target = inner;
        allElements.add(target);
        setBounds(target.getBounds());
    }

    @Override
    public void handleClick(int x, int y, int button) {
        if (button != 3) {
            dragging = false;
            super.handleClick(x, y, button);
        } else {
            dragging = true;
        }
        lastX = x;
        lastY = y;
    }

    @Override
    public void handleDrag(int x, int y) {
        if (!dragging) {
            super.handleDrag(x, y);
        } else {
            Rect r = target.getBounds();
            target.setBounds(new Rect(r.x + (x - lastX), r.y + (y - lastY), r.width, r.height));
        }
        lastX = x;
        lastY = y;
    }
}
