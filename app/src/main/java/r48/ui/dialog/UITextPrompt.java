/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.dialog;

import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;

/**
 * Created on 12/31/16.
 */
public class UITextPrompt extends UIElement.UIProxy {

    public UITextBox utb = new UITextBox("", FontSizes.textDialogFieldTextHeight);
    public UIScrollLayout uiSVL = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public boolean wantClose = false;

    public UITextPrompt(final String s, final IConsumer<String> iConsumer) {
        uiSVL.panelsAdd(new UILabel(s, FontSizes.textDialogDescTextHeight));
        uiSVL.panelsAdd(new UISplitterLayout(utb, new UITextButton(TXDB.get("OK"), FontSizes.textDialogFieldTextHeight, new Runnable() {
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
