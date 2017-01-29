/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import gabien.ui.UITextButton;

/**
 * Just a useful tool for constructing UI stuff.
 * Created on 12/29/16.
 */
public class UIAppendButton extends UIPanel {
    public UITextButton button;
    public UIElement subElement;
    public int textHeight;
    public UIAppendButton(String s, UIElement holder, Runnable runnable, int h2) {
        textHeight = h2;
        button = new UITextButton(h2, s, runnable);
        subElement = holder;
        // This specific order was chosen because labels on the left can overflow and get in the way of the button.
        allElements.add(subElement);
        allElements.add(button);
        Rect bgb = button.getBounds();
        int h = holder.getBounds().height;
        if (bgb.height > h)
            h = bgb.height;
        setBounds(new Rect(0, 0, bgb.width, h));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        button.setBounds(UITextButton.getRecommendedSize(button.Text, textHeight));
        Rect bgb = button.getBounds();
        button.setBounds(new Rect(r.width - bgb.width, 0, bgb.width, bgb.height));
        subElement.setBounds(new Rect(0, 0, r.width - bgb.width, r.height));
    }
}
