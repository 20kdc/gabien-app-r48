/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.specialized.textboxes;

import r48.App;
import r48.ui.UIAppendButton;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;

/**
 * Created 29th July, 2023.
 */
public class UITextStuffMenu extends App.Prx {
    private final Supplier<String[]> getter;
    private final Consumer<String[]> editor;
    private final TextRules textRules;
    private final int fieldWidth;
    private boolean didAnything = false;

    public UITextStuffMenu(App app, Supplier<String[]> getter, Consumer<String[]> editor, TextRules textRules, int fw) {
        super(app);
        this.getter = getter;
        this.editor = editor;
        this.textRules = textRules;
        this.fieldWidth = fw;
        UIScrollLayout usl = new UIScrollLayout(true, app.f.menuS);
        UIElement uie = new UITextButton(T.s.align_left, app.f.menuTH, () -> alignLeft());
        uie = new UIAppendButton(T.s.align_centre, uie, () -> alignCentre(), app.f.menuTH);
        uie = new UIAppendButton(T.s.align_right, uie, () -> alignRight(), app.f.menuTH);
        usl.panelsAdd(uie);
        usl.panelsAdd(new UITextButton(T.s.bCopyTextToClipboard, app.f.menuTH, () -> {
            StringBuilder total = new StringBuilder();
            String[] contents = getter.get();
            for (int i = 0; i < contents.length; i++) {
                total.append(contents[i]);
                total.append('\n');
            }
            GaBIEn.clipboard.copyText(total.toString());
        }));
        proxySetElement(usl, true);
    }

    @Override
    public boolean requestsUnparenting() {
        return didAnything;
    }

    private void editSimple(Function<String, String> editDetail) {
        didAnything = true;
        String[] res = getter.get();
        for (int i = 0; i < res.length; i++)
            res[i] = editDetail.apply(res[i]);
        editor.accept(res);
    }
    private void alignLeft() {
        didAnything = true;
        editSimple((s) -> s.trim());
    }
    private void alignCentre() {
        didAnything = true;
        editSimple((s) -> {
            s = s.trim();
            int startWidth = textRules.countCells(s);
            int expectWidth = ((fieldWidth - startWidth) / 2) + startWidth;
            for (int i = 0; i < fieldWidth; i++) {
                if (textRules.countCells(s) >= expectWidth)
                    break;
                s = " " + s;
            }
            return s;
        });
    }
    private void alignRight() {
        didAnything = true;
        editSimple((s) -> {
            s = s.trim();
            for (int i = 0; i < fieldWidth; i++) {
                if (textRules.countCells(s) >= fieldWidth)
                    break;
                s = " " + s;
            }
            return s;
        });
    }
}
