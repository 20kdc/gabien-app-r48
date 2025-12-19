/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.uslx.append.*;

import java.util.function.Supplier;

import gabien.ui.UIElement;
import gabien.ui.dialogs.UIAutoclosingPopupMenu;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UIButton;
import gabien.ui.elements.UITextButton;
import r48.App;

/**
 * Shows a context menu.
 * Created on November 14, 2018.
 */
public class UIMenuButton extends UITextButton {
    public UIMenuButton(App app, String s, int h2, final Supplier<UIElement> runnable) {
        super(s, h2, null);
        core(app, this, runnable);
    }

    public UIMenuButton(App app, String s, int h2, final Supplier<Boolean> continued, final String[] text, final Runnable[] runnables) {
        super(s, h2, null);
        core(app, this, continued, text, runnables);
    }

    public UIMenuButton(App app, String s, int h2, final Supplier<Boolean> continued, UIPopupMenu.Entry[] runnables) {
        super(s, h2, null);
        core(app, this, continued, runnables);
    }

    public UIMenuButton(App app, String s, int h2, final Supplier<Boolean> continued, final Iterable<UIPopupMenu.Entry> runnables) {
        super(s, h2, null);
        core(app, this, continued, runnables);
    }

    /**
     * The core of UIMenuButton.
     */
    public static void core(final App app, final UIButton<?> button, final Supplier<UIElement> runnable) {
        button.toggle = true;
        button.onClick = () -> {
            button.state = true;
            UIElement basis = runnable.get();
            app.ui.wm.createMenu(button, new UIProxy(basis, false) {
                @Override
                public void onWindowClose() {
                    super.onWindowClose();
                    button.state = false;
                }
            });
        };
    }

    /**
     * The core of UIMenuButton.
     */
    public static void core(final App app, final UIButton<?> button, final Supplier<Boolean> continued, final String[] text, final Runnable[] runnables) {
        core(app, button, () -> coreMenuGen(app, continued, text, runnables));
    }

    /**
     * The core of UIMenuButton.
     */
    public static void core(final App app, final UIButton<?> button, final Supplier<Boolean> continued, UIPopupMenu.Entry[] runnables) {
        core(app, button, continued, new ArrayIterable<UIPopupMenu.Entry>(runnables));
    }

    /**
     * The core of UIMenuButton.
     */
    public static void core(final App app, final UIButton<?> button, final Supplier<Boolean> continued, final Iterable<UIPopupMenu.Entry> runnables) {
        core(app, button, () -> coreMenuGen(app, continued, runnables));
    }

    /**
     * The core of UIMenuButton; special 'post-hoc' version.
     */
    public static void corePostHoc(final App app, final UIButton<?> button, UIElement basis) {
        button.toggle = true;
        button.state = true;
        app.ui.wm.createMenu(button, new UIProxy(basis, false) {
            @Override
            public void onWindowClose() {
                super.onWindowClose();
                button.state = false;
            }
        });
    }

    /**
     * The core of UIMenuButton; special 'post-hoc' version.
     */
    public static void corePostHoc(final App app, final UIButton<?> button, final Supplier<Boolean> continued, final String[] text, final Runnable[] runnables) {
        corePostHoc(app, button, coreMenuGen(app, continued, text, runnables));
    }

    /**
     * The core of UIMenuButton; special 'post-hoc' version.
     */
    public static void corePostHoc(final App app, final UIButton<?> button, final Supplier<Boolean> continued, UIPopupMenu.Entry[] runnables) {
        corePostHoc(app, button, continued, new ArrayIterable<UIPopupMenu.Entry>(runnables));
    }

    /**
     * The core of UIMenuButton; special 'post-hoc' version.
     */
    public static void corePostHoc(final App app, final UIButton<?> button, final Supplier<Boolean> continued, final Iterable<UIPopupMenu.Entry> runnables) {
        corePostHoc(app, button, coreMenuGen(app, continued, runnables));
    }

    /**
     * Interior core.
     */
    public static UIElement coreMenuGen(final App app, final Supplier<Boolean> continued, final String[] text, final Runnable[] runnables) {
        return new UIAutoclosingPopupMenu(text, runnables, app.f.menuTH, app.f.menuS, true) {
            @Override
            public void optionExecute(int b) {
                if (continued != null)
                    if (!continued.get())
                        return;
                super.optionExecute(b);
            }
        };
    }

    /**
     * Interior core.
     */
    public static UIElement coreMenuGen(final App app, final Supplier<Boolean> continued, UIPopupMenu.Entry[] runnables) {
        return coreMenuGen(app, continued, new ArrayIterable<UIPopupMenu.Entry>(runnables));
    }

    /**
     * Interior core.
     */
    public static UIElement coreMenuGen(final App app, final Supplier<Boolean> continued, final Iterable<UIPopupMenu.Entry> runnables) {
        return new UIAutoclosingPopupMenu(runnables, app.f.menuTH, app.f.menuS, true) {
            @Override
            public void optionExecute(int b) {
                if (continued != null)
                    if (!continued.get())
                        return;
                super.optionExecute(b);
            }
        };
    }
}
