/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.ui.*;

/**
 * Just a useful tool for constructing UI stuff.
 * Created on 12/29/16.
 */
public class UIAppendButton extends UIElement.UIPanel {
    public final UIButton button;
    public final UIElement subElement;

    public UIAppendButton(String s, UIElement holder, Runnable runnable, int h2) {
        this(new UITextButton(s, h2, runnable), holder);
    }

    public UIAppendButton(Art.Symbol s, UIElement holder, Runnable runnable, int h2) {
        this(new UISymbolButton(s, h2, runnable), holder);
    }

    public UIAppendButton(UIButton s, UIElement holder) {
        button = s;
        subElement = holder;
        // This specific order was chosen because labels on the left can overflow and get in the way of the button.
        layoutAddElement(subElement);
        layoutAddElement(button);

        runLayout();
        setForcedBounds(null, new Rect(getWantedSize()));
    }

    @Override
    public void runLayout() {
        Size r = getSize();

        Size bgb1 = button.getWantedSize();

        button.setForcedBounds(this, new Rect(r.width - bgb1.width, 0, bgb1.width, bgb1.height));
        subElement.setForcedBounds(this, new Rect(0, 0, r.width - bgb1.width, r.height));

        // In case of change.
        bgb1 = button.getWantedSize();
        Size bgb2 = subElement.getWantedSize();
        setWantedSize(new Size(bgb1.width + bgb2.width, Math.max(bgb1.height, bgb2.height)));
    }
}
