/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.ui.UIElement;
import gabien.ui.dialogs.UIAutoclosingPopupMenu;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UITextButton;
import r48.R48;

/**
 * Combo button!
 * Created 18th August, 2023.
 */
public abstract class UIChoiceButton<T> extends UITextButton {
    public final R48 app;
    private T currentChoice;
    private T[] choices;

    public UIChoiceButton(AppUI U, int h2, T defChoice, T[] choices) {
        super("", h2, null);
        this.app = U.app;
        setText(choiceToText(defChoice));
        forceToRecommended();
        currentChoice = defChoice;
        this.choices = choices;
        toggle = true;
        onClick = () -> {
            state = true;
            UIElement basis = genMenu();
            U.wm.createMenu(UIChoiceButton.this, new UIProxy(basis, false) {
                @Override
                public void onWindowClose() {
                    super.onWindowClose();
                    state = false;
                }
            });
        };
    }

    private UIElement genMenu() {
        final T[] choiceListAtTime = choices;
        UIPopupMenu.Entry[] entries = new UIPopupMenu.Entry[choiceListAtTime.length];
        for (int i = 0; i < entries.length; i++) {
            final T thisChoice = choiceListAtTime[i];
            entries[i] = new UIPopupMenu.Entry(choiceToText(thisChoice), () -> {
                if (choiceListAtTime == choices)
                    setSelected(thisChoice);
            });
        }
        return new UIAutoclosingPopupMenu(entries, app.f.menuTH, app.f.menuS, true);
    }

    public void setContents(T defChoice, T[] choices) {
        this.choices = choices;
        setSelected(defChoice);
    }

    public void setSelected(T defChoice) {
        currentChoice = defChoice;
        setText(choiceToText(defChoice));
    }

    public T getSelected() {
        return currentChoice;
    }

    public abstract String choiceToText(T choice);
}
