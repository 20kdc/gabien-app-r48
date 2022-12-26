/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dialog;

import gabien.ui.*;
import gabien.uslx.append.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.TXDB;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used for RPG Command Selection.
 * Created on 12/30/16.
 */
public class UIEnumChoice extends UIElement.UIProxy {
    private final UIScrollLayout[] categoryPanels;
    private final UITabPane mainPanel;
    private boolean wantsSelfClose = false;

    // entryText defaults to "Manual."
    public UIEnumChoice(final IConsumer<RubyIO> result, final LinkedList<Option> options, String entryText, EntryMode entryType) {
        this(result, new Category[] {new Category(TXDB.get("Options"), options)}, entryText, entryType);
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
                final UITextButton button = new UITextButton(o.textPrefix + o.textSuffix, FontSizes.enumChoiceTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        if (!wantsSelfClose)
                            result.accept(o.value);
                        wantsSelfClose = true;
                    }
                });
                UIElement element = button;
                if (o.editSuffix != null) {
                    final UIAppendButton switcheroo = new UIAppendButton(TXDB.get(" Name"), element, null, FontSizes.enumChoiceTextHeight);
                    final UITextBox textbox = new UITextBox(o.textSuffix, FontSizes.enumChoiceTextHeight);
                    final AtomicBoolean ab = new AtomicBoolean(false);
                    switcheroo.button.onClick = new Runnable() {
                        @Override
                        public void run() {
                            if (ab.get()) {
                                ab.set(false);
                                switcheroo.setSubElement(button);
                            } else {
                                ab.set(true);
                                switcheroo.setSubElement(textbox);
                            }
                        }
                    };
                    textbox.onEdit = new Runnable() {
                        @Override
                        public void run() {
                            button.text = o.textPrefix + textbox.text;
                            o.editSuffix.accept(textbox.text);
                            ab.set(false);
                            switcheroo.setSubElement(button);
                        }
                    };
                    element = switcheroo;
                }
                categoryPanels[i].panelsAdd(element);
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

    public static final class Category {
        public final String translatedName;
        public final Option[] options;

        public Category(String s, LinkedList<Option> o) {
            translatedName = s;
            // ... more Java nonsense
            options = o.toArray(new Option[0]);
        }
    }

    public static final Comparator<Option> COMPARATOR_OPTION = new Comparator<UIEnumChoice.Option>() {
        public int compare(UIEnumChoice.Option o1, UIEnumChoice.Option o2) {
            return UITest.natStrComp(o1.textMerged, o2.textMerged);
        }
    };

    public static final class Option {
        public final String textPrefix;
        public final String textSuffix;
        public final String textMerged;
        public final RubyIO value;
        public final @Nullable IConsumer<String> editSuffix;
        public final @Nullable SchemaPath furtherDataButton;

        public Option(String s, RubyIO integer) {
            textPrefix = s;
            textSuffix = "";
            textMerged = s;
            value = integer;
            editSuffix = null;
            furtherDataButton = null;
        }

        public Option(String pfx, String sfx, RubyIO integer, @Nullable IConsumer<String> edit, @Nullable SchemaPath fdb) {
            textPrefix = pfx;
            textSuffix = sfx;
            textMerged = pfx + sfx;
            value = integer;
            editSuffix = edit;
            furtherDataButton = fdb;
        }
    }
}
