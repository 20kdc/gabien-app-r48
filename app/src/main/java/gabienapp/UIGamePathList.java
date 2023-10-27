/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.util.List;

import gabien.ui.UIElement.UIProxy;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import r48.cfg.Config;
import r48.ui.UIAppendButton;

/**
 * Created on 24th August 2022.
 */
public class UIGamePathList extends UIProxy {
    public final List<String> values;
    private UIScrollLayout layout;
    public final UITextBox text;
    private final UIAppendButton appendButton;
    public final Config c;

    public UIGamePathList(Config c, List<String> val) {
        this.c = c;
        text = new UITextBox("", c.f.launcherTH);
        appendButton = new UIAppendButton("+", text, new Runnable() {
            @Override
            public void run() {
                if (!values.contains(text.text)) {
                    values.add(text.text);
                    modified();
                }
            }
        }, c.f.launcherTH);
        if (val.size() > 0)
            text.text = val.get(val.size() - 1);
        values = val;
        layout = new UIScrollLayout(true, c.f.generalS);
        refresh();
        proxySetElement(layout, true);
    }

    public void refresh() {
        layout.panelsClear();
        for (final String v : values) {
            UITextButton mainButton = new UITextButton(v, c.f.launcherTH, new Runnable() {
                @Override
                public void run() {
                    text.text = v;
                    values.remove(v);
                    values.add(v);
                    modified();
                }
            });
            layout.panelsAdd(new UIAppendButton(" - ", mainButton, new Runnable() {
                @Override
                public void run() {
                    values.remove(v);
                    modified();
                }
            }, c.f.launcherTH));
        }
        layout.panelsAdd(appendButton);
    }

    public void modified() {
        refresh();
    }
}
