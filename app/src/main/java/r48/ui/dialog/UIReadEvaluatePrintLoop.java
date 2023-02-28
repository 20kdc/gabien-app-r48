/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.dialog;

import gabien.datum.DatumWriter;
import gabien.ui.Rect;
import gabien.ui.Size;
import gabien.ui.UIElement.UIProxy;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextBox;
import r48.cfg.Config;
import r48.minivm.MVMEnvironment;

/**
 * REPL for accessing MiniVM.
 * Created 28th February 2023.
 */
public class UIReadEvaluatePrintLoop extends UIProxy {
    public final Config c;
    public final UIScrollLayout view;
    public final UITextBox text;

    public UIReadEvaluatePrintLoop(Config c, MVMEnvironment vmCtx) {
        this.c = c;
        text = new UITextBox("", c.f.dialogWindowTextHeight).setNoDiscard();
        text.onEnter = () -> {
            String txt = text.text;
            text.text = "";
            write("> " + txt);
            Object res = null;
            try {
                res = vmCtx.evalString(txt);
            } catch (Exception ex) {
                ex.printStackTrace();
                write(ex.toString());
                return;
            }
            try {
                write("= " + DatumWriter.objectToString(res));
            } catch (Exception ex) {
                write("=? " + res.toString());
            }
        };
        view = new UIScrollLayout(true, c.f.generalScrollersize);
        proxySetElement(new UISplitterLayout(view, text, true, 1), false);
        Size sz = new Size(c.f.scaleGuess(400), c.f.scaleGuess(300));
        setWantedSize(sz);
        setForcedBounds(null, new Rect(sz));
    }

    public void write(String string) {
        view.panelsAdd(new UILabel(string, c.f.dialogWindowTextHeight));
    }
}
