/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.search;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement.UIProxy;
import r48.App;
import r48.search.ICommandClassifier;
import r48.ui.UIChoiceButton;

/**
 * Created 19th August, 2023
 */
public class UICommandClassifierSelector extends UIProxy {
    private final UIChoiceButton<ICommandClassifier> ccs;
    public UICommandClassifierSelector(App app, @Nullable ICommandClassifier preferred) {
        ICommandClassifier[] ents = app.cmdClassifiers.toArray(new ICommandClassifier[0]);
        if (preferred == null)
            preferred = ents[0];
        ccs = new UIChoiceButton<ICommandClassifier>(app, app.f.dialogWindowTH, preferred, ents) {
            @Override
            public String choiceToText(ICommandClassifier choice) {
                return choice.getName();
            }
        };
        proxySetElement(ccs, true);
    }

    public ICommandClassifier getClassifier() {
        return ccs.getSelected();
    }
}
