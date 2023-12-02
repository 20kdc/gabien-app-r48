/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextBox;
import r48.App;

/**
 * This is definitely a good idea. Yup.
 * Created October 26th, 2023.
 */
public enum TextOperator implements ITextAnalyzer {
    // Order is also order in UI
    ContainsInsensitive, Contains, Equals, EqualsInsensitive;

    private TextOperator() {
    }

    @Override
    @NonNull
    public String getName(App app) {
        switch (this) {
        case Contains:
            return app.t.u.ccs_tContains;
        case ContainsInsensitive:
            return app.t.u.ccs_tContainsI;
        case Equals:
            return app.t.u.ccs_tEquals;
        case EqualsInsensitive:
            return app.t.u.ccs_tEqualsI;
        }
        // is this even possible
        return name();
    }

    @Override
    @NonNull
    public Instance instance(App app) {
        return new Instance() {
            String needle = "";
            String needleTLC = "";

            @Override
            public void setupEditor(@NonNull LinkedList<UIElement> usl, @NonNull Runnable onEdit) {
                UITextBox uil = new UITextBox(needle, app.f.dialogWindowTH);
                uil.onEdit = () -> {
                    needle = uil.getText();
                    needleTLC = needle.toLowerCase();
                };
                usl.add(uil);
            }

            @Override
            public boolean matches(String haystack) {
                switch (TextOperator.this) {
                case Contains:
                    return haystack.contains(needle);
                case ContainsInsensitive:
                    return haystack.toLowerCase().contains(needleTLC);
                case Equals:
                    return haystack.equals(needle);
                case EqualsInsensitive:
                    return haystack.toLowerCase().equals(needleTLC);
                }
                return false;
            }
        };
    }
}
