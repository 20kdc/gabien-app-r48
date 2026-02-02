/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.dialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.ui.layouts.UITabPane;
import gabien.ui.layouts.UITabBar.Tab;
import gabien.ui.layouts.UITabBar.TabIcon;
import gabien.ui.UIElement;
import r48.AdHocSaveLoad;
import r48.dbs.ObjectInfo;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.schema.util.SchemaPath;
import r48.search.USFROperationMode;
import r48.ui.AppUI;
import r48.ui.UIAppendButton;
import r48.ui.search.UIUSFROperationModeSelector;

/**
 * Universal string locator fun
 * Created on 13th August 2022.
 */
public class UIRMUniversalStringReplacer extends AppUI.Prx {
    private UIScrollLayout layout = new UIScrollLayout(true, app.f.generalS);
    private RListPanel settingsFull;
    private RListPanel settingsPartial;

    private UIObjectInfoSetSelector setSelector;
    private UIUSFROperationModeSelector modeSelector;

    public UIRMUniversalStringReplacer(AppUI app) {
        super(app);
        settingsFull = new RListPanel(app, T.u.usl_full);
        settingsPartial = new RListPanel(app, T.u.usl_partial);
        setSelector = new UIObjectInfoSetSelector(app);
        modeSelector = new UIUSFROperationModeSelector(app, app.f.dialogWindowTH);
        Set<ObjectInfo> setCopy = setSelector.getSet();

        // load config if possible
        IRIO replacer = AdHocSaveLoad.load("replacer");

        if (replacer != null) {
            IRIO files = replacer.getIVar("@files");
            Set<ObjectInfo> sset = new HashSet<ObjectInfo>();
            for (IRIO fk : files.getANewArray()) {
                String fileId = fk.decString();
                for (ObjectInfo oi2 : setCopy)
                    if (oi2.idName.equals(fileId))
                        sset.add(oi2);
            }
            setSelector.updateSet(sset);

            // before AVC 74, all replacements are either partial or not-partial
            RListPanel oldImportTarget = settingsFull;
            IRIO partial = replacer.getIVar("@partial");
            if (partial != null)
                if (partial.getType() == 'T')
                    oldImportTarget = settingsPartial;
            // old: AVC 71
            IRIO replacements = replacer.getIVar("@replacements");
            if (replacements != null)
                oldImportTarget.loadFromHash(replacements);
            // new: AVC 72
            replacements = replacer.getIVar("@replacements_list");
            if (replacements != null)
                oldImportTarget.loadFromList(replacements);
            // after AVC 74, replacements are split into two lists
            replacements = replacer.getIVar("@replacements_full");
            if (replacements != null)
                settingsFull.loadFromList(replacements);
            replacements = replacer.getIVar("@replacements_partial");
            if (replacements != null)
                settingsPartial.loadFromList(replacements);
        }

        refreshContents();
        
        proxySetElement(new UISplitterLayout(layout, setSelector, false, 0.5), false);
    }

    private void refreshContents() {
        LinkedList<UIElement> elms = new LinkedList<>();

        settingsFull.refreshContents();
        settingsPartial.refreshContents();

        elms.add(modeSelector);

        UITabPane utp = new UITabPane(app.f.finderTabS, false, false);
        utp.addTab(new Tab(settingsFull, new TabIcon[0]));
        utp.addTab(new Tab(settingsPartial, new TabIcon[0]));
        elms.add(utp);

        elms.add(new UITextButton(T.u.usl_saveConfig, app.f.dialogWindowTH, () -> {
            IRIO rio = new IRIOGeneric(app.ilg.adhocIOContext);
            rio.setObject("R48::UniversalStringLocatorSettings");
            settingsFull.saveTo(rio.addIVar("@replacements_full"));
            settingsPartial.saveTo(rio.addIVar("@replacements_partial"));
            IRIO files = rio.addIVar("@files");
            files.setArray();
            for (ObjectInfo oi : setSelector.getSet())
                files.addAElem(files.getALen()).setString(oi.idName);
            AdHocSaveLoad.save("replacer", rio);
        }));

        elms.add(new UITextButton(T.u.usl_confirmReplace, app.f.dialogWindowTH, new Runnable() {
            @Override
            public void run() {
                // full replacements
                final HashMap<String, String> mapFull = new HashMap<String, String>();
                for (Replacement r : settingsFull.settings)
                    mapFull.put(r.key, r.value);
                // partial replacements - longer first!
                final LinkedList<Replacement> ent = new LinkedList<Replacement>(settingsPartial.settings);
                Collections.sort(ent, new Comparator<Replacement>() {
                    public int compare(Replacement o1, Replacement o2) {
                        int l1 = o1.key.length();
                        int l2 = o2.key.length();
                        // note inverse
                        if (l1 < l2)
                            return 1;
                        if (l1 > l2)
                            return -1;
                        return 0;
                    }
                });
                // continue...
                int total = 0;
                int files = 0;
                String log = "";
                for (ObjectInfo objInfo : setSelector.getSet()) {
                    SchemaPath sp = objInfo.makePath(true);
                    if (sp != null) {
                        files++;
                        // now do it!
                        USFROperationMode mode = modeSelector.getSelected();
                        AtomicInteger finds = new AtomicInteger(0);
                        HashSet<IRIO> hsi = new HashSet<>();
                        mode.locate(app, sp, (element, target, path) -> {
                            if (!hsi.add(target))
                                return false;
                            StringBuilder res = new StringBuilder();
                            String dec = target.decString();
                            String fullReplace = mapFull.get(dec);
                            if (fullReplace != null) {
                                target.setString(fullReplace);
                                path.changeOccurred(false);
                                finds.incrementAndGet();
                                return false;
                            }
                            int pos = 0;
                            int len = dec.length();
                            while (pos < len) {
                                String foundChk = null;
                                int foundSkip = 0;
                                for (Replacement apply : ent) {
                                    String key = apply.key;
                                    // nope
                                    if (key.equals(""))
                                        continue;
                                    if (dec.startsWith(key, pos)) {
                                        foundChk = apply.value;
                                        foundSkip = key.length();
                                        break;
                                    }
                                }
                                if (foundChk != null) {
                                    res.append(foundChk);
                                    pos += foundSkip;
                                } else {
                                    res.append(dec.charAt(pos++));
                                }
                            }
                            String resStr = res.toString();
                            if (!resStr.equals(dec)) {
                                target.setString(resStr);
                                path.changeOccurred(false);
                                finds.incrementAndGet();
                                return false;
                            }
                            return false;
                        }, true);
                        int count = finds.get();
                        total += count;
                        if (count > 0)
                            log += "\n" + objInfo.toString() + ": " + count;
                    }
                }
                U.launchDialog(T.u.usl_completeReport.r(total, files) + log);
            }
        }));
        layout.panelsSet(elms);
    }

    public static class RListPanel extends AppUI.Prx {
        private UIScrollLayout layout = new UIScrollLayout(true, app.f.generalS);
        private LinkedList<Replacement> settings = new LinkedList<Replacement>();

        private UITextBox adderK = new UITextBox("", app.f.dialogWindowTH);
        private UITextBox adderV = new UITextBox("", app.f.dialogWindowTH);

        private UIElement adderA = new UISplitterLayout(new UILabel(T.u.usl_from, app.f.dialogWindowTH), adderK, false, 0);
        private UIElement adderB = new UISplitterLayout(new UILabel(T.u.usl_to, app.f.dialogWindowTH), adderV, false, 0);
        private UIElement adderC = new UITextButton(T.u.usl_addR, app.f.dialogWindowTH, () -> {
            String k = adderK.getText();
            String v = adderV.getText();
            settingsRemoveByKey(k);
            settings.add(new Replacement(k, v));
            refreshContents();
        });

        private final String title;

        public RListPanel(AppUI app, String string) {
            super(app);
            title = string;
            refreshContents();
            proxySetElement(layout, true);
        }

        @Override
        public String toString() {
            return title;
        }

        public void saveTo(IRIO replacements) {
            replacements.setArray();
            for (Replacement key : settings) {
                IRIO replacement = replacements.addAElem(replacements.getALen());
                replacement.setObject("R48::UniversalStringLocatorReplacement");
                replacement.addIVar("@key").setString(key.key);
                replacement.addIVar("@value").setString(key.value);
            }
        }

        private void loadFromHash(IRIO irio) {
            for (DMKey hk : irio.getHashKeys())
                settings.add(new Replacement(hk.decString(), irio.getHashVal(hk).decString()));
        }

        private void loadFromList(IRIO irio) {
            for (IRIO hk : irio.getANewArray())
                settings.add(new Replacement(hk.getIVar("@key").decString(), hk.getIVar("@value").decString()));
        }

        private void settingsRemoveByKey(String text) {
            Replacement res = null;
            for (Replacement r : settings)
                if (r.key.equals(text))
                    res = r;
            if (res != null)
                settings.remove(res);
        }

        private void refreshContents() {
            LinkedList<UIElement> elms = new LinkedList<>();

            for (final Replacement key : settings) {
                UIElement keyLine = new UILabel(key.key + " -> " + key.value, app.f.dialogWindowTH);
                keyLine = new UIAppendButton("-", keyLine, new Runnable() {
                    @Override
                    public void run() {
                        settings.remove(key);
                        refreshContents();
                    }
                }, app.f.dialogWindowTH);
                elms.add(keyLine);
            }
            elms.add(adderA);
            elms.add(adderB);
            elms.add(adderC);
            layout.panelsSet(elms);
        }
    }

    public static class Replacement {
        public final String key, value;
        public Replacement(String text, String text2) {
            key = text;
            value = text2;
        }
    }
}
