/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.dialog;

import java.util.LinkedList;

import gabien.ui.UIElement;
import gabien.ui.UIElement.UIProxy;
import gabien.ui.elements.UIChatBox;
import gabien.ui.elements.UILabel;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import r48.cfg.Config;
import r48.minivm.MVMEnv;
import r48.minivm.MVMU;

/**
 * REPL for accessing MiniVM.
 * Created 28th February 2023.
 */
public class UIReadEvaluatePrintLoop extends UIProxy {
    public final Config c;
    public final UIScrollLayout view;
    public final LinkedList<UIElement> viewList = new LinkedList<>();
    public final UIChatBox text;
    public final String title;

    public UIReadEvaluatePrintLoop(Config c, MVMEnv vmCtx, String title) {
        this.c = c;
        this.title = title;
        text = new UIChatBox("", c.f.dialogWindowTH);
        text.onSubmit = (txt) -> {
            write("> " + txt);
            Object res = null;
            try {
                res = vmCtx.evalString(txt);
            } catch (Exception ex) {
                ex.printStackTrace();
                StringBuilder sb = new StringBuilder();
                sb.append("!");
                Throwable ex2 = ex;
                while (ex2 != null) {
                    sb.append(" ");
                    sb.append(ex2.getClass().getSimpleName());
                    sb.append(":");
                    sb.append(ex2.getLocalizedMessage());
                    ex2 = ex2.getCause();
                }
                write(sb.toString());
                return;
            }
            write("= " + MVMU.userStr(res));
        };
        view = new UIScrollLayout(true, c.f.generalS);
        write(title);
        proxySetElement(new UISplitterLayout(view, text, true, 1), false);
        Size sz = new Size(c.f.scaleGuess(400), c.f.scaleGuess(300));
        setWantedSize(sz);
        setForcedBounds(null, new Rect(sz));
    }

    @Override
    public String toString() {
        return title;
    }

    public void write(String string) {
        // for ease of copying
        System.out.println(string);
        viewList.add(new UILabel(string, c.f.dialogWindowTH));
        view.panelsSet(viewList);
    }
}
