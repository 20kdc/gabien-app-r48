/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.search;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import gabien.ui.UIElement.UIProxy;
import gabien.ui.layouts.UIScrollLayout;
import r48.search.IClassifierish;
import r48.ui.AppUI;

/**
 * Created 19th August, 2023
 * Pivoted to UIClassifierishInstanceWidget, 25th October, 2023
 */
public class UIClassifierishInstanceWidget<I extends IClassifierish.BaseInstance> extends UIProxy {
    public final UIScrollLayout usl;
    public final I instance;
    public Runnable onEdit;

    public UIClassifierishInstanceWidget(AppUI app, @NonNull I inst) {
        this.instance = inst;
        usl = new UIScrollLayout(true, app.f.generalS);
        onEdit = () -> {
            LinkedList<UIElement> elms = new LinkedList<>();
            instance.setupEditor(app, elms, onEdit);
            usl.panelsSet(elms);
        };
        onEdit.run();
        proxySetElement(usl, true);
    }
}
