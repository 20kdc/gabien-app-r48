/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.util.LinkedList;
import java.util.List;

import gabien.ui.UIElement;
import gabien.ui.UIElement.UIProxy;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIListLayout;
import r48.cfg.Config;
import r48.ui.UIAppendButton;

/**
 * Created on 24th August 2022.
 */
public abstract class UISavableList<E extends UIElement, V> extends UIProxy {
    public final List<V> values;
    private UIListLayout layout;
    public final E text;
    private final UIAppendButton appendButton;
    public final Config c;

    public UISavableList(Config c, E editor, List<V> val) {
        this.c = c;
        text = editor;
        values = val;
        appendButton = new UIAppendButton("+", text, () -> {
            V v = getValue();
            if (!values.contains(v)) {
                values.add(v);
                modified();
            }
        }, c.f.launcherTH);
        layout = new UIListLayout(true);
        if (val.size() > 0) {
            V initText = val.get(val.size() - 1);
            putValue(initText);
        }
        refresh();
        proxySetElement(layout, true);
    }

    public abstract V getValue();
    public abstract void putValue(V value);

    public void refresh() {
        LinkedList<UIElement> uie = new LinkedList<>();
        for (final V v : values) {
            UITextButton mainButton = new UITextButton(v.toString(), c.f.launcherTH, () -> {
                putValue(v);
                values.remove(v);
                values.add(v);
                modified();
            });
            uie.add(new UIAppendButton(" - ", mainButton, () -> {
                values.remove(v);
                modified();
            }, c.f.launcherTH));
        }
        uie.add(appendButton);
        layout.panelsSet(uie);
    }

    public void modified() {
        refresh();
    }
}
