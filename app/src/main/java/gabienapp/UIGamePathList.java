/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package gabienapp;

import gabien.ui.UIScrollLayout;
import gabien.ui.UITextBox;
import gabien.ui.UITextButton;

import java.util.List;

import gabien.ui.UIElement.UIProxy;
import r48.FontSizes;
import r48.ui.UIAppendButton;

/**
 * Created on 24th August 2022.
 */
public class UIGamePathList extends UIProxy {
    public final List<String> values;
    private UIScrollLayout layout;
    public final UITextBox text = new UITextBox("", FontSizes.launcherTextHeight);
    private final UIAppendButton appendButton = new UIAppendButton("+", text, new Runnable() {
        @Override
        public void run() {
            if (!values.contains(text.text)) {
                values.add(text.text);
                modified();
            }
        }
    }, FontSizes.launcherTextHeight);

    public UIGamePathList(List<String> val) {
        if (val.size() > 0)
            text.text = val.get(val.size() - 1);
        values = val;
        layout = new UIScrollLayout(true, FontSizes.generalScrollersize);
        refresh();
        proxySetElement(layout, true);
    }

    public void refresh() {
        layout.panelsClear();
        for (final String v : values) {
            UITextButton mainButton = new UITextButton(v, FontSizes.launcherTextHeight, new Runnable() {
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
            }, FontSizes.launcherTextHeight));
        }
        layout.panelsAdd(appendButton);
    }

    public void modified() {
        refresh();
    }
}
