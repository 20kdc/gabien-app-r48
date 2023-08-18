/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.search;

import r48.App;
import r48.search.USFROperationMode;
import r48.ui.UIChoiceButton;

/**
 * Created 18th August, 2023.
 */
public class UIUSFROperationModeButton extends UIChoiceButton<USFROperationMode> {
    public UIUSFROperationModeButton(App app, int h2) {
        this(app, h2, USFROperationMode.listForApp(app));
    }

    private UIUSFROperationModeButton(App app, int h2, USFROperationMode[] b) {
        super(app, h2, b[0], b);
    }

    @Override
    public String choiceToText(USFROperationMode choice) {
        return choice.translate(app);
    }
}
