/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.uslx.append.*;
import gabien.ui.UIAutoclosingPopupMenu;
import gabien.ui.UIElement;
import gabien.ui.UIPopupMenu;
import gabien.ui.UITextButton;
import r48.App;
import r48.AppMain;
import r48.FontSizes;

/**
 * Shows a context menu.
 * Created on November 14, 2018.
 */
public class UIMenuButton extends UITextButton {
    public UIMenuButton(App app, String s, int h2, final ISupplier<UIElement> runnable) {
        super(s, h2, null);
        toggle = true;
        onClick = new Runnable() {
            @Override
            public void run() {
                state = true;
                UIElement basis = runnable.get();
                app.window.createMenu(UIMenuButton.this, new UIProxy(basis, false) {
                    @Override
                    public void onWindowClose() {
                        super.onWindowClose();
                        state = false;
                    }
                });
            }
        };
    }

    public UIMenuButton(App app, String s, int h2, final ISupplier<Boolean> continued, final String[] text, final Runnable[] runnables) {
        this(app, s, h2, new ISupplier<UIElement>() {
            @Override
            public UIElement get() {
                return new UIAutoclosingPopupMenu(text, runnables, FontSizes.menuTextHeight, FontSizes.menuScrollersize, true) {
                    @Override
                    public void optionExecute(int b) {
                        if (continued != null)
                            if (!continued.get())
                                return;
                        super.optionExecute(b);
                    }
                };
            }
        });
    }

    public UIMenuButton(App app, String s, int h2, final ISupplier<Boolean> continued, UIPopupMenu.Entry[] runnables) {
        this(app, s, h2, continued, new ArrayIterable<UIPopupMenu.Entry>(runnables));
    }

    public UIMenuButton(App app, String s, int h2, final ISupplier<Boolean> continued, final Iterable<UIPopupMenu.Entry> runnables) {
        this(app, s, h2, new ISupplier<UIElement>() {
            @Override
            public UIElement get() {
                return new UIAutoclosingPopupMenu(runnables, FontSizes.menuTextHeight, FontSizes.menuScrollersize, true) {
                    @Override
                    public void optionExecute(int b) {
                        if (continued != null)
                            if (!continued.get())
                                return;
                        super.optionExecute(b);
                    }
                };
            }
        });
    }
}
