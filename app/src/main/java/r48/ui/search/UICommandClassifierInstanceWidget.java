/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.search;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIScrollLayout;
import gabien.ui.UIElement.UIProxy;
import r48.App;
import r48.search.ICommandClassifier;

/**
 * Created 19th August, 2023
 * Pivoted to UICommandClassifierInstanceWidget, 25th October, 2023
 */
public class UICommandClassifierInstanceWidget extends UIProxy {
    public final UIScrollLayout usl;
    public final ICommandClassifier.Instance instance;
    public Runnable onEdit;

    public UICommandClassifierInstanceWidget(App app, @NonNull ICommandClassifier.Instance inst) {
        this.instance = inst;
        usl = new UIScrollLayout(true, app.f.generalS);
        onEdit = () -> {
            usl.panelsClear();
            instance.setupEditor(usl, onEdit);
        };
        onEdit.run();
        proxySetElement(usl, true);
    }
}
