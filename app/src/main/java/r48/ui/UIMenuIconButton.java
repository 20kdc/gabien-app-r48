/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import java.util.function.Function;
import java.util.function.Supplier;

import gabien.render.IDrawable;
import gabien.ui.UIElement;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UIIconButton;
import r48.App;

/**
 * Shows a context menu.
 * Copied from UIMenuButton on November 29, 2024.
 */
public class UIMenuIconButton extends UIIconButton {
    public UIMenuIconButton(App app, Function<Boolean, IDrawable> s, int h2, final Supplier<UIElement> runnable) {
        super(s, h2, null);
        UIMenuButton.core(app, this, runnable);
    }

    public UIMenuIconButton(App app, Function<Boolean, IDrawable> s, int h2, final Supplier<Boolean> continued, final String[] text, final Runnable[] runnables) {
        super(s, h2, null);
        UIMenuButton.core(app, this, continued, text, runnables);
    }

    public UIMenuIconButton(App app, Function<Boolean, IDrawable> s, int h2, final Supplier<Boolean> continued, UIPopupMenu.Entry[] runnables) {
        super(s, h2, null);
        UIMenuButton.core(app, this, continued, runnables);
    }

    public UIMenuIconButton(App app, Function<Boolean, IDrawable> s, int h2, final Supplier<Boolean> continued, final Iterable<UIPopupMenu.Entry> runnables) {
        super(s, h2, null);
        UIMenuButton.core(app, this, continued, runnables);
    }
}
