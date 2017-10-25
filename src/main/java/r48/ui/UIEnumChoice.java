/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.ui.*;
import r48.FontSizes;
import r48.UITest;
import r48.dbs.TXDB;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Used for RPG Command Selection.
 * Created on 12/30/16.
 */
public class UIEnumChoice extends UIPanel implements IWindowElement {
    private final UIScrollLayout[] categoryPanels;
    private final UITabPane mainPanel;
    private UISplitterLayout finalSplit;
    private UINumberBox nb;
    private boolean wantsSelfClose = false;

    public UIEnumChoice(final IConsumer<Integer> result, final HashMap<String, Integer> options, String buttonText) {
        this(result, new Category[] {new Category(TXDB.get("Options"), mapOptions(options))}, buttonText);
    }

    private static LinkedList<Option> mapOptions(HashMap<String, Integer> o) {
        LinkedList<Option> llo = new LinkedList<Option>();
        for (String s : UITest.sortedKeysStr(o.keySet()))
            llo.add(new Option(s, o.get(s)));
        return llo;
    }

    public UIEnumChoice(final IConsumer<Integer> result, final Category[] order, String buttonText) {
        categoryPanels = new UIScrollLayout[order.length];
        for (int i = 0; i < categoryPanels.length; i++) {
            categoryPanels[i] = new UIScrollLayout(true, FontSizes.generalScrollersize);
            for (final Option o : order[i].options) {
                categoryPanels[i].panels.add(new UITextButton(FontSizes.enumChoiceTextHeight, o.key, new Runnable() {
                    @Override
                    public void run() {
                        if (!wantsSelfClose)
                            result.accept(o.value);
                        wantsSelfClose = true;
                    }
                }));
            }
        }

        nb = new UINumberBox(FontSizes.schemaFieldTextHeight);
        finalSplit = new UISplitterLayout(nb, new UITextButton(FontSizes.schemaButtonTextHeight, buttonText, new Runnable() {
            @Override
            public void run() {
                if (!wantsSelfClose)
                    result.accept(nb.number);
                wantsSelfClose = true;
            }
        }), false, 1, 3);
        if (buttonText.length() != 0)
            categoryPanels[categoryPanels.length - 1].panels.add(finalSplit);

        String[] strs = new String[categoryPanels.length];
        for (int i = 0; i < strs.length; i++)
            strs[i] = order[i].translatedName;
        mainPanel = new UITabPane(strs, categoryPanels, FontSizes.tabTextHeight);

        allElements.add(mainPanel);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        mainPanel.setBounds(new Rect(0, 0, r.width, r.height));
    }

    @Override
    public boolean wantsSelfClose() {
        return wantsSelfClose;
    }

    @Override
    public void windowClosed() {

    }

    // The absolute advanced API for use by RPGCommand stuff

    public static final class Category {
        public final String translatedName;
        public final Option[] options;

        public Category(String s, LinkedList<Option> o) {
            translatedName = s;
            options = o.toArray(new Option[0]);
        }
    }

    public static final class Option {
        public final String key;
        public final int value;

        public Option(String s, Integer integer) {
            key = s;
            value = integer;
        }
    }
}
