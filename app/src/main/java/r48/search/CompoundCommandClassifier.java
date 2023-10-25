/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.App;
import r48.dbs.RPGCommand;
import r48.ui.UIAppendButton;
import r48.ui.UIChoiceButton;

/**
 * Used as the root command classifier.
 * Created October 25th, 2023.
 */
public class CompoundCommandClassifier extends App.Svc implements ICommandClassifier {
    public CompoundCommandClassifier(App ac) {
        super(ac);
    }

    @Override
    public String getName() {
        return "Compound classifier [INTERNAL]";
    }

    @Override
    public Instance instance() {
        final ICommandClassifier[] ents = app.cmdClassifiers.toArray(new ICommandClassifier[0]);
        return new Instance() {
            public Entry[] entries = new Entry[] {new Entry()};
            @Override
            public void setupEditor(UIScrollLayout usl, Runnable onEdit) {
                final Entry[] currentEntriesArray = entries;
                for (int i = 0; i < entries.length; i++) {
                    final Entry ent = entries[i];
                    final int iFinal = i;
                    // header row
                    UIChoiceButton<ICommandClassifier> ccs = new UIChoiceButton<ICommandClassifier>(app, app.f.dialogWindowTH, entries[i].cType, ents) {
                        @Override
                        public String choiceToText(ICommandClassifier choice) {
                            return choice.getName();
                        }
                        @Override
                        public void setSelected(ICommandClassifier defChoice) {
                            super.setSelected(defChoice);
                            if (ent.cType == defChoice)
                                return;
                            ent.cType = defChoice;
                            ent.cInstance = ent.cType.instance();
                            onEdit.run();
                        }
                    };
                    UIChoiceButton<BooleanChainOperator> bco = new UIChoiceButton<BooleanChainOperator>(app, app.f.dialogWindowTH, ent.chain, BooleanChainOperator.values()) {
                        @Override
                        public String choiceToText(BooleanChainOperator choice) {
                            return choice.getTranslatedName(app);
                        }
                        @Override
                        public void setSelected(BooleanChainOperator defChoice) {
                            super.setSelected(defChoice);
                            ent.chain = defChoice;
                        }
                    };
                    UIElement hLine = new UISplitterLayout(bco, ccs, false, 0);
                    if (i > 0) {
                        hLine = new UIAppendButton(" ^ ", hLine, () -> {
                            if (entries != currentEntriesArray)
                                return;
                            entries[iFinal] = entries[iFinal - 1];
                            entries[iFinal - 1] = ent;
                            onEdit.run();
                        }, app.f.dialogWindowTH);
                    }
                    hLine = new UIAppendButton("-", hLine, () -> {
                        if (entries != currentEntriesArray)
                            return;
                        System.arraycopy(entries, iFinal + 1, entries, iFinal, entries.length - (iFinal + 1));
                        Entry[] mod = new Entry[entries.length - 1];
                        System.arraycopy(entries, 0, mod, 0, mod.length);
                        entries = mod;
                        onEdit.run();
                    }, app.f.dialogWindowTH);
                    usl.panelsAdd(hLine);
                    // body
                    entries[i].cInstance.setupEditor(usl, onEdit);
                }
                usl.panelsAdd(new UITextButton(app.t.u.ccs_addCondition, app.f.dialogWindowTH, () -> {
                    if (entries != currentEntriesArray)
                        return;
                    Entry[] entries2 = new Entry[entries.length + 1];
                    System.arraycopy(entries, 0, entries2, 0, entries.length);
                    entries2[entries.length] = new Entry();
                    entries = entries2;
                    onEdit.run();
                }));
            }
            
            @Override
            public boolean matches(@Nullable RPGCommand target) {
                boolean value = true;
                for (Entry ent : entries)
                    value = ent.chain.evaluate(value, ent.cInstance.matches(target));
                return value;
            }
        };
    }

    public class Entry {
        public ICommandClassifier cType;
        public ICommandClassifier.Instance cInstance;
        public BooleanChainOperator chain = BooleanChainOperator.And;

        public Entry() {
            cType = app.commandTags.get("translatable");
            cInstance = cType.instance();
        }
    }
}
