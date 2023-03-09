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

import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITabBar.Tab;
import gabien.ui.UITabBar.TabIcon;
import gabien.ui.UITabPane;
import gabien.ui.UITextBox;
import gabien.ui.UITextButton;
import gabien.IPeripherals;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.uslx.append.IConsumer;
import gabien.uslx.append.IFunction;
import r48.AdHocSaveLoad;
import r48.App;
import r48.RubyIO;
import r48.dbs.ObjectInfo;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.toolsets.BasicToolset;
import r48.ui.UIAppendButton;
import r48.ui.UISetSelector;

/**
 * Universal string locator fun
 * Created on 13th August 2022.
 */
public class UIRMUniversalStringLocator extends App.Prx {
    private UIScrollLayout layout = new UIScrollLayout(true, app.f.generalScrollersize);
    private RListPanel settingsFull = new RListPanel(app, T.u.usl_full);
    private RListPanel settingsPartial = new RListPanel(app, T.u.usl_partial);

    private UISetSelector<ObjectInfo> setSelector;
    private boolean scheduleSetSelectorUpdate = false;
    private IConsumer<SchemaPath> refreshOnObjectChange = new IConsumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath t) {
            scheduleSetSelectorUpdate = true;
        }
    };

    public UIRMUniversalStringLocator(App app) {
        super(app);
        Iterable<ObjectInfo> oi = app.getObjectInfos();
        setSelector = new UISetSelector<ObjectInfo>(app, oi);
        for (ObjectInfo ii : oi)
            app.odb.registerModificationHandler(ii.idName, refreshOnObjectChange);

        // load config if possible
        IRIO replacer = AdHocSaveLoad.load("replacer");

        if (replacer != null) {
            IRIO files = replacer.getIVar("@files");
            Set<ObjectInfo> sset = new HashSet<ObjectInfo>();
            for (IRIO fk : files.getANewArray()) {
                String fileId = fk.decString();
                for (ObjectInfo oi2 : oi) {
                    if (oi2.idName.equals(fileId)) {
                        sset.add(oi2);
                    }
                }
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

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        if (scheduleSetSelectorUpdate) {
            scheduleSetSelectorUpdate = false;
            setSelector.refreshButtonText();
        }
        super.update(deltaTime, selected, peripherals);
    }

    private void refreshContents() {
        layout.panelsClear();

        settingsFull.refreshContents();
        settingsPartial.refreshContents();

        UITabPane utp = new UITabPane(app.f.schemaPagerTabScrollersize, false, false);
        utp.addTab(new Tab(settingsFull, new TabIcon[0]));
        utp.addTab(new Tab(settingsPartial, new TabIcon[0]));
        layout.panelsAdd(utp);

        layout.panelsAdd(new UITextButton(T.g.wordSave, app.f.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                RubyIO rio = new RubyIO();
                rio.setObject("R48::UniversalStringLocatorSettings");
                settingsFull.saveTo(rio.addIVar("@replacements_full"));
                settingsPartial.saveTo(rio.addIVar("@replacements_partial"));
                RubyIO files = rio.addIVar("@files");
                files.setArray();
                for (ObjectInfo oi : setSelector.getSet())
                    files.addAElem(files.getALen()).setString(oi.idName, true);
                AdHocSaveLoad.save("replacer", rio);
            }
        }));

        layout.panelsAdd(new UITextButton(T.u.usl_confirmReplace, app.f.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                int total = 0;
                int files = 0;
                String log = "";
                for (ObjectInfo objInfo : setSelector.getSet()) {
                    IObjectBackend.ILoadedObject rio = app.odb.getObject(objInfo.idName);
                    SchemaElement se = app.sdb.getSDBEntry(objInfo.schemaName);
                    if (rio != null) {
                        files++;
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
                        // now do it!
                        int count = BasicToolset.universalStringLocator(app, rio.getObject(), new IFunction<IRIO, Integer>() {
                            @Override
                            public Integer apply(IRIO rubyIO) {
                                StringBuilder res = new StringBuilder();
                                String dec = rubyIO.decString();
                                String fullReplace = mapFull.get(dec);
                                if (fullReplace != null) {
                                    rubyIO.setString(fullReplace);
                                    return 1;
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
                                    rubyIO.setString(resStr);
                                    return 1;
                                }
                                return 0;
                            }
                        }, true);
                        total += count;
                        if (count > 0) {
                            SchemaPath sp = new SchemaPath(se, rio);
                            sp.changeOccurred(false);
                            log += "\n" + objInfo.toString() + ": " + count;
                        }
                    }
                }
                app.ui.launchDialog(T.u.usl_completeReport.r(total, files) + log);
            }
        }));
    }

    public static class RListPanel extends App.Prx {
        private UIScrollLayout layout = new UIScrollLayout(true, app.f.generalScrollersize);
        private LinkedList<Replacement> settings = new LinkedList<Replacement>();

        private UITextBox adderK = new UITextBox("", app.f.dialogWindowTextHeight);
        private UITextBox adderV = new UITextBox("", app.f.dialogWindowTextHeight);

        private UIElement adderA = new UISplitterLayout(new UILabel(T.u.usl_from, app.f.dialogWindowTextHeight), adderK, false, 0);
        private UIElement adderB = new UISplitterLayout(new UILabel(T.u.usl_to, app.f.dialogWindowTextHeight), adderV, false, 0);
        private UIElement adderC = new UITextButton(T.u.usl_addR, app.f.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                settingsRemoveByKey(adderK.text);
                settings.add(new Replacement(adderK.text, adderV.text));
                refreshContents();
            }
        });

        private final String title;

        public RListPanel(App app, String string) {
            super(app);
            title = string;
            refreshContents();
            proxySetElement(layout, true);
        }

        @Override
        public String toString() {
            return title;
        }

        public void saveTo(RubyIO replacements) {
            replacements.setArray();
            for (Replacement key : settings) {
                RubyIO replacement = replacements.addAElem(replacements.getALen());
                replacement.setObject("R48::UniversalStringLocatorReplacement");
                replacement.addIVar("@key").setString(key.key, true);
                replacement.addIVar("@value").setString(key.value, true);
            }
        }

        private void loadFromHash(IRIO irio) {
            for (IRIO hk : irio.getHashKeys())
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
            layout.panelsClear();

            for (final Replacement key : settings) {
                UIElement keyLine = new UILabel(key.key + " -> " + key.value, app.f.dialogWindowTextHeight);
                keyLine = new UIAppendButton("-", keyLine, new Runnable() {
                    @Override
                    public void run() {
                        settings.remove(key);
                        refreshContents();
                    }
                }, app.f.dialogWindowTextHeight);
                layout.panelsAdd(keyLine);
            }
            layout.panelsAdd(adderA);
            layout.panelsAdd(adderB);
            layout.panelsAdd(adderC);
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
