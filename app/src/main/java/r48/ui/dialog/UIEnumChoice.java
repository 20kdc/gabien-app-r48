/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.dialog;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.TXDB;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Used for RPG Command Selection.
 * Created on 12/30/16.
 */
public class UIEnumChoice extends UIElement.UIProxy {
    private final UIScrollLayout[] categoryPanels;
    private final UITabPane mainPanel;
    private boolean wantsSelfClose = false;

    // entryText defaults to "Manual."
    public UIEnumChoice(final IConsumer<RubyIO> result, final HashMap<String, RubyIO> options, String entryText, EntryMode entryType) {
        this(result, new Category[] {new Category(TXDB.get("Options"), mapOptions(options))}, entryText, entryType);
    }

    private static LinkedList<Option> mapOptions(HashMap<String, RubyIO> o) {
        LinkedList<Option> llo = new LinkedList<Option>();
        for (String s : UITest.sortedKeysStr(o.keySet()))
            llo.add(new Option(s, o.get(s)));
        return llo;
    }

    public UIEnumChoice(final IConsumer<RubyIO> result, final Category[] order, String entryText, EntryMode entryType) {
        categoryPanels = new UIScrollLayout[order.length];
        for (int i = 0; i < categoryPanels.length; i++) {
            final String name = order[i].translatedName;
            categoryPanels[i] = new UIScrollLayout(true, FontSizes.generalScrollersize) {
                @Override
                public String toString() {
                    return name;
                }
            };
            for (final Option o : order[i].options) {
                categoryPanels[i].panelsAdd(new UITextButton(o.key, FontSizes.enumChoiceTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        if (!wantsSelfClose)
                            result.accept(o.value);
                        wantsSelfClose = true;
                    }
                }));
            }
        }

        if (entryText == null)
            entryText = TXDB.get("Manual.");

        UISplitterLayout finalSplit = null;
        if (entryType == EntryMode.STR) {
            final UITextBox nb = new UITextBox("", FontSizes.schemaFieldTextHeight);
            finalSplit = new UISplitterLayout(nb, new UITextButton(entryText, FontSizes.schemaFieldTextHeight, new Runnable() {
                @Override
                public void run() {
                    if (!wantsSelfClose)
                        result.accept(new RubyIO().setString(nb.text, false));
                    wantsSelfClose = true;
                }
            }), false, 1, 3);
        } else if (entryType == EntryMode.SYM) {
            final UITextBox nb = new UITextBox("", FontSizes.schemaFieldTextHeight);
            finalSplit = new UISplitterLayout(nb, new UITextButton(entryText, FontSizes.schemaFieldTextHeight, new Runnable() {
                @Override
                public void run() {
                    if (!wantsSelfClose) {
                        RubyIO rio = new RubyIO();
                        rio.type = ':';
                        rio.symVal = nb.text;
                        result.accept(rio);
                    }
                    wantsSelfClose = true;
                }
            }), false, 1, 3);
        } else if (entryType == EntryMode.INT) {
            final UINumberBox nb = new UINumberBox(0, FontSizes.schemaFieldTextHeight);
            finalSplit = new UISplitterLayout(nb, new UITextButton(entryText, FontSizes.schemaFieldTextHeight, new Runnable() {
                @Override
                public void run() {
                    if (!wantsSelfClose)
                        result.accept(new RubyIO().setFX(nb.number));
                    wantsSelfClose = true;
                }
            }), false, 1, 3);
        }
        if (finalSplit != null)
            categoryPanels[categoryPanels.length - 1].panelsAdd(finalSplit);

        mainPanel = new UITabPane(FontSizes.tabTextHeight, false, false);
        for (UIElement uie : categoryPanels)
            mainPanel.addTab(new UITabBar.Tab(uie, new UITabBar.TabIcon[] {}));
        mainPanel.handleIncoming();

        proxySetElement(mainPanel, false);
    }

    @Override
    public boolean requestsUnparenting() {
        return wantsSelfClose;
    }

    public enum EntryMode {
        LOCK,
        STR,
        SYM,
        INT
    }

    // The absolute advanced API for use by RPGCommand stuff

    public static final class Category<X> {
        public final String translatedName;
        public final Option[] options;

        public Category(String s, LinkedList<Option> o) {
            translatedName = s;
            // ... more Java nonsense
            options = o.toArray(new Option[0]);
        }
    }

    public static final class Option {
        public final String key;
        public final RubyIO value;

        public Option(String s, RubyIO integer) {
            key = s;
            value = integer;
        }
    }
}
