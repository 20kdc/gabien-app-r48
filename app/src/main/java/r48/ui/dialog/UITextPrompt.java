/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dialog;

import java.util.function.Consumer;

import gabien.ui.UIElement;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import r48.ui.AppUI;

/**
 * Created on 12/31/16.
 */
public class UITextPrompt extends AppUI.Prx {

    public UITextBox utb = new UITextBox("", app.f.textDialogFieldTH);
    public boolean wantClose = false;

    public UITextPrompt(AppUI app, final String s, final Consumer<String> iConsumer) {
        super(app);
        UILabel label = new UILabel(s, app.f.textDialogDescTH);
        UIElement mainLayout = new UISplitterLayout(utb, new UITextButton(T.g.bOk, app.f.textDialogFieldTH, () -> {
            iConsumer.accept(utb.getText());
            wantClose = true;
        }), false, 4, 5);
        proxySetElement(new UIScrollLayout(true, app.f.generalS, label, mainLayout), true);
    }

    @Override
    public boolean requestsUnparenting() {
        return wantClose;
    }
}
