/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;

/**
 * Builds simple choice dialogs along the lines of:
 * <p>
 * I would like to hug your cat.
 * -----------------------------
 * Sure, go ahead|No, don't! >:(
 * <p>
 * Created on July 14th, 2018
 */
public class UIChoicesMenu extends UIElement.UIProxy {
    private boolean selfClose = false;

    public UIChoicesMenu(String s, String[] strings, final Runnable[] runnables) {
        UILabel topLabel = new UILabel(s, FontSizes.dialogWindowTextHeight);
        UIScrollLayout label = new UIScrollLayout(true, FontSizes.menuScrollersize);
        label.panelsAdd(topLabel);
        UIScrollLayout usl = new UIScrollLayout(false, FontSizes.menuScrollersize);
        for (int i = 0; i < strings.length; i++) {
            final int fi = i;
            usl.panelsAdd(new UITextButton(strings[i], FontSizes.dialogWindowTextHeight, new Runnable() {
                @Override
                public void run() {
                    runnables[fi].run();
                    selfClose = true;
                }
            }));
        }
        proxySetElement(new UISplitterLayout(label, usl, true, 1d), true);
    }

    @Override
    public boolean requestsUnparenting() {
        return selfClose;
    }

    @Override
    public String toString() {
        return TXDB.get("Please confirm...");
    }
}
