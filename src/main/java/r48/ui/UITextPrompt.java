/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.ui.*;
import r48.FontSizes;

/**
 * Created on 12/31/16.
 */
public class UITextPrompt extends UIPanel implements IWindowElement {
    public UITextBox utb = new UITextBox(FontSizes.textDialogFieldTextHeight);
    public UIScrollVertLayout uiSVL = new UIScrollVertLayout();
    public boolean wantClose = false;
    public UITextPrompt(final String s, final IConsumer<String> iConsumer) {
        allElements.add(uiSVL);
        uiSVL.panels.add(new UILabel(s, FontSizes.textDialogDescTextHeight));
        uiSVL.panels.add(new UIHHalfsplit(4, 5, utb, new UITextButton(FontSizes.textDialogFieldTextHeight, "OK", new Runnable() {
            @Override
            public void run() {
                iConsumer.accept(utb.text);
                wantClose = true;
            }
        })));
        setBounds(new Rect(0, 0, 320, 38));
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

    }
}
