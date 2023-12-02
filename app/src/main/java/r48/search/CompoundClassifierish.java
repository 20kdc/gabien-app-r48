/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import java.util.LinkedList;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.ui.Art.Symbol;
import r48.ui.UIAppendButton;
import r48.ui.UIChoiceButton;

/**
 * Used as the root command classifier.
 * Created October 25th, 2023.
 */
public abstract class CompoundClassifierish<C extends IClassifierish<I>, I extends IClassifierish.BaseInstance> extends App.Svc implements IClassifierish.BaseInstance {
    private final C[] ents;
    public final C defaultEntry;
    public Entry[] entries;
    public final BooleanChainOperator defaultBCO;

    public CompoundClassifierish(App ac, final C[] e, C de, boolean hasFirst, BooleanChainOperator db) {
        super(ac);
        ents = e;
        defaultEntry = de;
        defaultBCO = db;
        if (hasFirst)
            entries = new Entry[] {new Entry(app, de, db)};
        else
            entries = new Entry[] {};
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setupEditor(LinkedList<UIElement> usl, Runnable onEdit) {
        final Entry[] currentEntriesArray = entries;
        for (int i = 0; i < entries.length; i++) {
            final Entry ent = entries[i];
            final int iFinal = i;
            // header row
            UIChoiceButton<C> ccs = new UIChoiceButton<C>(app, app.f.dialogWindowTH, (C) entries[i].cType, ents) {
                @Override
                public String choiceToText(C choice) {
                    return choice.getName(app);
                }
                @Override
                public void setSelected(C defChoice) {
                    super.setSelected(defChoice);
                    if (ent.cType == defChoice)
                        return;
                    ent.cType = defChoice;
                    ent.cInstance = ent.cType.instance(app);
                    onEdit.run();
                }
            };
            LinkedList<UIElement> interiorList = new LinkedList<>();
            interiorList.add(ccs);
            entries[i].cInstance.setupEditor(interiorList, onEdit);
            UIScrollLayout interiorScrollLayout = new UIScrollLayout(true, app.f.generalS, interiorList);
            // wrapping & such
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
            UIElement hLine = new UISplitterLayout(bco, interiorScrollLayout, false, 0);
            if (i > 0) {
                hLine = new UIAppendButton(" ^ ", hLine, () -> {
                    if (entries != currentEntriesArray)
                        return;
                    entries[iFinal] = entries[iFinal - 1];
                    entries[iFinal - 1] = ent;
                    onEdit.run();
                }, app.f.dialogWindowTH);
            }
            hLine = new UIAppendButton(Symbol.XRed, hLine, () -> {
                if (entries != currentEntriesArray)
                    return;
                System.arraycopy(entries, iFinal + 1, entries, iFinal, entries.length - (iFinal + 1));
                Entry[] mod = new Entry[entries.length - 1];
                System.arraycopy(entries, 0, mod, 0, mod.length);
                entries = mod;
                onEdit.run();
            }, app.f.dialogWindowTH);
            interiorList.add(hLine);
        }
        usl.add(new UITextButton(app.t.u.ccs_addCondition, app.f.dialogWindowTH, () -> {
            if (entries != currentEntriesArray)
                return;
            Entry[] entries2 = new Entry[entries.length + 1];
            System.arraycopy(entries, 0, entries2, 0, entries.length);
            entries2[entries.length] = new Entry(app, defaultEntry, defaultBCO);
            entries = entries2;
            onEdit.run();
        }));
    }

    public static class Entry {
        public IClassifierish<?> cType;
        public IClassifierish.BaseInstance cInstance;
        public BooleanChainOperator chain = BooleanChainOperator.And;

        public Entry(App app, IClassifierish<?> ct, BooleanChainOperator bco) {
            chain = bco;
            cType = ct;
            cInstance = cType.instance(app);
        }
    }
}
