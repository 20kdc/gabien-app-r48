/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dialog;

import java.util.LinkedList;

import gabien.ui.UIElement;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;

/**
 * Builds simple choice dialogs along the lines of:
 * <p>
 * I would like to hug your cat.
 * -----------------------------
 * Sure, go ahead|No, don't! >:(
 * <p>
 * Created on July 14th, 2018
 */
public class UIChoicesMenu extends App.Prx {
    private boolean selfClose = false;

    public UIChoicesMenu(App app, String s, String[] strings, final Runnable[] runnables) {
        super(app);
        UILabel topLabel = new UILabel(s, app.f.dialogWindowTH);
        UIScrollLayout label = new UIScrollLayout(true, app.f.menuS);
        LinkedList<UIElement> elms = new LinkedList<>();
        elms.add(topLabel);
        for (int i = 0; i < strings.length; i++) {
            final int fi = i;
            elms.add(new UITextButton(strings[i], app.f.dialogWindowTH, () -> {
                runnables[fi].run();
                selfClose = true;
            }));
        }
        UIScrollLayout usl = new UIScrollLayout(false, app.f.menuS, elms);
        proxySetElement(new UISplitterLayout(label, usl, true, 1d), true);
    }

    @Override
    public boolean requestsUnparenting() {
        return selfClose;
    }

    @Override
    public String toString() {
        return T.t.confirm;
    }
}
