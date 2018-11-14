/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.ui.UIAutoclosingPopupMenu;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;

/**
 * Shows a context menu.
 * Created on November 14, 2018.
 */
public class UIMenuButton extends UITextButton {
    public UIMenuButton(String s, int h2, final String[] text, final Runnable[] runnables) {
        super(s, h2, null);
        toggle = true;
        onClick = new Runnable() {
            @Override
            public void run() {
                state = true;
                AppMain.window.createMenu(UIMenuButton.this, new UIAutoclosingPopupMenu(text, runnables, FontSizes.menuTextHeight, FontSizes.menuScrollersize, true) {
                    @Override
                    public void onWindowClose() {
                        super.onWindowClose();
                        state = false;
                    }
                });
            }
        };
    }
}
