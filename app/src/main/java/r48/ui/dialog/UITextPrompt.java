/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dialog;

import gabien.ui.*;
import gabien.uslx.append.*;
import r48.App;

/**
 * Created on 12/31/16.
 */
public class UITextPrompt extends App.Prx {

    public UITextBox utb = new UITextBox("", app.f.textDialogFieldTH);
    public UIScrollLayout uiSVL = new UIScrollLayout(true, app.f.generalS);
    public boolean wantClose = false;

    public UITextPrompt(App app, final String s, final Consumer<String> iConsumer) {
        super(app);
        uiSVL.panelsAdd(new UILabel(s, app.f.textDialogDescTH));
        uiSVL.panelsAdd(new UISplitterLayout(utb, new UITextButton(T.g.bOk, app.f.textDialogFieldTH, new Runnable() {
            @Override
            public void run() {
                iConsumer.accept(utb.text);
                wantClose = true;
            }
        }), false, 4, 5));
        proxySetElement(uiSVL, true);
    }

    @Override
    public boolean requestsUnparenting() {
        return wantClose;
    }
}
