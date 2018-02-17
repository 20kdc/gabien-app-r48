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
 * Created on 12/31/16.
 */
public class UITextPrompt extends UIElement.UIProxy {
    public UITextPrompt(final String s, final IConsumer<String> iConsumer) {
        proxySetElement(new UILabel("IPCRESS" ,16), true);
    }
    /*IPCRESS
    public UITextBox utb = new UITextBox(FontSizes.textDialogFieldTextHeight);
    public UIScrollLayout uiSVL = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public boolean wantClose = false;

    public UITextPrompt(final String s, final IConsumer<String> iConsumer) {
        allElements.add(uiSVL);
        uiSVL.panels.add(new UILabel(s, FontSizes.textDialogDescTextHeight));
        uiSVL.panels.add(new UISplitterLayout(utb, new UITextButton(FontSizes.textDialogFieldTextHeight, TXDB.get("OK"), new Runnable() {
            @Override
            public void run() {
                iConsumer.accept(utb.text);
                wantClose = true;
            }
        }), false, 4, 5));
        uiSVL.runLayout();
        setBounds(new Rect(0, 0, FontSizes.scaleGuess(320), uiSVL.scrollLength));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        uiSVL.setBounds(new Rect(0, 0, r.width, r.height));
    }

    @Override
    public boolean wantsSelfClose() {
        return wantClose;
    }

    @Override
    public void windowClosed() {

    }*/
}
