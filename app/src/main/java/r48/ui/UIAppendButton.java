/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.*;
import gabien.ui.elements.UIButton;
import gabien.ui.elements.UIIconButton;
import gabien.ui.elements.UITextButton;
import gabien.uslx.append.*;
import r48.App;

/**
 * Just a useful tool for constructing UI stuff.
 * Created on 12/29/16.
 */
public class UIAppendButton extends UIElement.UIPanel {
    public final UIButton<?> button;
    private UIElement subElement;

    public UIAppendButton(String s, UIElement holder, Runnable runnable, int h2) {
        this(new UITextButton(s, h2, runnable), holder);
    }

    public UIAppendButton(Art.Symbol s, UIElement holder, Runnable runnable, int h2) {
        this(new UIIconButton(s, h2, runnable), holder);
    }

    public UIAppendButton(App app, String s, UIElement holder, Supplier<Boolean> continued, String[] text, Runnable[] runnables, int h2) {
        this(new UIMenuButton(app, s, h2, continued, text, runnables), holder);
    }

    public UIAppendButton(UIButton<?> s, UIElement holder) {
        button = s;
        subElement = holder;
        // This specific order was chosen because labels on the left can overflow and get in the way of the button.
        layoutAddElement(subElement);
        layoutAddElement(button);

        forceToRecommended();
    }

    public void setSubElement(UIElement n) {
        layoutRemoveElement(subElement);
        subElement = n;
        layoutAddElement(n);
        layoutRecalculateMetrics();
    }

    public UIElement getSubElement() {
        return subElement;
    }

    @Override
    public int layoutGetHForW(int width) {
        Size bgb1 = subElement.getWantedSize();
        Size bgb2 = button.getWantedSize();
        if (bgb1.width + bgb2.width <= width) {
            return Math.max(bgb1.height, bgb2.height);
        } else {
            return bgb1.height + bgb2.height;
        }
    }

    @Override
    protected void layoutRunImpl() {
        Size r = getSize();

        Size bgb1 = subElement.getWantedSize();
        Size bgb2 = button.getWantedSize();
        if (bgb1.width + bgb2.width <= r.width) {
            subElement.setForcedBounds(this, new Rect(0, 0, r.width - bgb2.width, r.height));
            button.setForcedBounds(this, new Rect(r.width - bgb2.width, 0, bgb2.width, r.height));
        } else {
            subElement.setForcedBounds(this, new Rect(0, 0, r.width, r.height - bgb2.height));
            button.setForcedBounds(this, new Rect(0, r.height - bgb2.height, r.width, bgb2.height));
            // Not a typo! If the width constraint is loosened we could be forced into going back & forth between cases.
            // This WILL cause an infinite loop in the layout code.
            // See issue #38 for more details.
        }
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        Size bgb1 = subElement.getWantedSize();
        Size bgb2 = button.getWantedSize();
        return new Size(bgb1.width + bgb2.width, Math.max(bgb1.height, bgb2.height));
    }

    public UIAppendButton togglable(boolean gridST) {
        button.togglable(gridST);
        return this;
    }
}
