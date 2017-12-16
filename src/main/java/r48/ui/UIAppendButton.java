/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
        setBounds(new Rect(0, 0, holder.getBounds().width + bgb.width, h));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        button.setBounds(UITextButton.getRecommendedSize(button.text, textHeight));
        Rect bgb = button.getBounds();
        button.setBounds(new Rect(r.width - bgb.width, 0, bgb.width, bgb.height));
        subElement.setBounds(new Rect(0, 0, r.width - bgb.width, r.height));
    }
}
