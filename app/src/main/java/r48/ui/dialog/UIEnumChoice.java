/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dialog;

import gabien.ui.*;
import gabien.ui.elements.UINumberBox;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.ui.layouts.UITabBar;
import gabien.ui.layouts.UITabPane;
import r48.App;
import r48.UITest;
import r48.io.data.DMKey;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;
import r48.ui.UIAppendButton;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used for RPG Command Selection.
 * Created on 12/30/16.
 */
public class UIEnumChoice extends App.Prx {
    private final UIScrollLayout[] categoryPanels;
    private final UITabPane mainPanel;
    private boolean wantsSelfClose = false;

    // entryText defaults to "Manual."
    public UIEnumChoice(App app, final Consumer<DMKey> result, final LinkedList<Option> options, String entryText, EntryMode entryType) {
        this(app, result, new Category[] {new Category(app.t.u.enumOptions, options)}, entryText, entryType);
    }

    public UIEnumChoice(App app, final Consumer<DMKey> result, final Category[] order, String entryText, EntryMode entryType) {
        super(app);
        categoryPanels = new UIScrollLayout[order.length];
        for (int i = 0; i < categoryPanels.length; i++) {
            final String name = order[i].translatedName;
            categoryPanels[i] = new UIScrollLayout(true, app.f.generalS) {
                @Override
                public String toString() {
                    return name;
                }
            };
            for (final Option o : order[i].options) {
                final UITextButton button = new UITextButton(o.getTextMerged(), app.f.enumChoiceTH, new Runnable() {
                    @Override
                    public void run() {
                        if (!wantsSelfClose)
                            result.accept(o.value);
                        wantsSelfClose = true;
                    }
                });
                UIElement element = button;
                if (o.editSuffix != null) {
                    final UIAppendButton switcheroo = new UIAppendButton(T.u.bEnumRename, element, null, app.f.enumChoiceTH);
                    final UITextBox textbox = new UITextBox(o.textSuffix.r(), app.f.enumChoiceTH);
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
                            String txt = textbox.getText();
                            button.setText(o.textPrefix + txt);
                            o.editSuffix.accept(txt);
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
            entryText = T.u.bEnumManual;

        UISplitterLayout finalSplit = null;
        if (entryType == EntryMode.STR) {
            final UITextBox nb = new UITextBox("", app.f.schemaFieldTH);
            finalSplit = new UISplitterLayout(nb, new UITextButton(entryText, app.f.schemaFieldTH, new Runnable() {
                @Override
                public void run() {
                    if (!wantsSelfClose)
                        result.accept(DMKey.ofStr(nb.getText()));
                    wantsSelfClose = true;
                }
            }), false, 1, 3);
        } else if (entryType == EntryMode.SYM) {
            final UITextBox nb = new UITextBox("", app.f.schemaFieldTH);
            finalSplit = new UISplitterLayout(nb, new UITextButton(entryText, app.f.schemaFieldTH, new Runnable() {
                @Override
                public void run() {
                    if (!wantsSelfClose) {
                        result.accept(DMKey.ofSym(nb.getText()));
                    }
                    wantsSelfClose = true;
                }
            }), false, 1, 3);
        } else if (entryType == EntryMode.INT) {
            final UINumberBox nb = new UINumberBox(0, app.f.schemaFieldTH);
            finalSplit = new UISplitterLayout(nb, new UITextButton(entryText, app.f.schemaFieldTH, new Runnable() {
                @Override
                public void run() {
                    if (!wantsSelfClose)
                        result.accept(DMKey.of(nb.getNumber()));
                    wantsSelfClose = true;
                }
            }), false, 1, 3);
        }
        if (finalSplit != null)
            categoryPanels[categoryPanels.length - 1].panelsAdd(finalSplit);

        mainPanel = new UITabPane(app.f.tabTH, false, false);
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
            return UITest.natStrComp(o1.getTextMerged(), o2.getTextMerged());
        }
    };

    public static final class Option {
        public final String textPrefix;
        public final FF0 textSuffix;
        private String textMerged;
        public final DMKey value;
        public final @Nullable Consumer<String> editSuffix;
        public final @Nullable SchemaPath furtherDataButton;

        public Option(String s, DMKey integer) {
            textPrefix = s;
            textSuffix = () -> "";
            value = integer;
            editSuffix = null;
            furtherDataButton = null;
        }

        public Option(String pfx, FF0 sfx, DMKey integer, @Nullable Consumer<String> edit, @Nullable SchemaPath fdb) {
            textPrefix = pfx;
            textSuffix = sfx;
            value = integer;
            editSuffix = edit;
            furtherDataButton = fdb;
        }

        public synchronized String getTextMerged() {
            String got = textMerged;
            if (got == null) {
                got = textPrefix + textSuffix.r();
                textMerged = got;
            }
            return got;
        }
    }
}
