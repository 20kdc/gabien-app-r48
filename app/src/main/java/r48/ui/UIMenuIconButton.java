/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.uslx.append.*;

import java.util.function.Function;
import java.util.function.Supplier;

import gabien.ui.UIElement;
import gabien.ui.dialogs.UIAutoclosingPopupMenu;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UIIconButton;
import gabien.ui.theming.IIcon;
import r48.App;

/**
 * Shows a context menu.
 * Copied from UIMenuButton on November 29, 2024.
 */
public class UIMenuIconButton extends UIIconButton {
    public UIMenuIconButton(App app, Function<Boolean, IIcon> s, int h2, final Supplier<UIElement> runnable) {
        super(s, h2, null);
        toggle = true;
        onClick = () -> {
            state = true;
            UIElement basis = runnable.get();
            app.ui.wm.createMenu(UIMenuIconButton.this, new UIProxy(basis, false) {
                @Override
                public void onWindowClose() {
                    super.onWindowClose();
                    state = false;
                }
            });
        };
    }

    public UIMenuIconButton(App app, Function<Boolean, IIcon> s, int h2, final Supplier<Boolean> continued, final String[] text, final Runnable[] runnables) {
        this(app, s, h2, () -> {
            return new UIAutoclosingPopupMenu(text, runnables, app.f.menuTH, app.f.menuS, true) {
                @Override
                public void optionExecute(int b) {
                    if (continued != null)
                        if (!continued.get())
                            return;
                    super.optionExecute(b);
                }
            };
        });
    }

    public UIMenuIconButton(App app, Function<Boolean, IIcon> s, int h2, final Supplier<Boolean> continued, UIPopupMenu.Entry[] runnables) {
        this(app, s, h2, continued, new ArrayIterable<UIPopupMenu.Entry>(runnables));
    }

    public UIMenuIconButton(App app, Function<Boolean, IIcon> s, int h2, final Supplier<Boolean> continued, final Iterable<UIPopupMenu.Entry> runnables) {
        this(app, s, h2, () -> {
            return new UIAutoclosingPopupMenu(runnables, app.f.menuTH, app.f.menuS, true) {
                @Override
                public void optionExecute(int b) {
                    if (continued != null)
                        if (!continued.get())
                            return;
                    super.optionExecute(b);
                }
            };
        });
    }
}
