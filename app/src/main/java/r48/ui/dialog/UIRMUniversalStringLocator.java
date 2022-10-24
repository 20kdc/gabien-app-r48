/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.ui.dialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextBox;
import gabien.ui.UITextButton;
import gabien.ui.UIElement;
import gabien.ui.UIElement.UIProxy;
import gabien.ui.UILabel;
import gabien.uslx.append.ArrayIterable;
import gabien.uslx.append.IFunction;
import r48.AdHocSaveLoad;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.ObjectInfo;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.map.systems.IDynobjMapSystem;
import r48.map.systems.IRMMapSystem;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.toolsets.BasicToolset;
import r48.ui.UIAppendButton;
import r48.ui.UISetSelector;

/**
 * Universal string locator fun
 * Created on 13th August 2022.
 */
public class UIRMUniversalStringLocator extends UIProxy {
    private UIScrollLayout layout = new UIScrollLayout(true, FontSizes.generalScrollersize);
    private boolean done = false;
    private boolean partialMatches = false;
    private LinkedList<Replacement> settings = new LinkedList<Replacement>();

    private UITextBox adderK = new UITextBox("", FontSizes.dialogWindowTextHeight);
    private UITextBox adderV = new UITextBox("", FontSizes.dialogWindowTextHeight);

    private UIElement adderA = new UISplitterLayout(new UILabel("From: ", FontSizes.dialogWindowTextHeight), adderK, false, 0);
    private UIElement adderB = new UISplitterLayout(new UILabel("To: ", FontSizes.dialogWindowTextHeight), adderV, false, 0);
    private UIElement adderC = new UITextButton(TXDB.get("Add replacement"), FontSizes.dialogWindowTextHeight, new Runnable() {
        @Override
        public void run() {
            settingsRemoveByKey(adderK.text);
            settings.add(new Replacement(adderK.text, adderV.text));
            refreshContents();
        }
    });

    private UISetSelector<ObjectInfo> setSelector;

    public UIRMUniversalStringLocator() {
        Iterable<ObjectInfo> oi = AppMain.getObjectInfos();
        setSelector = new UISetSelector<ObjectInfo>(oi);

        // load config if possible
        IRIO replacer = AdHocSaveLoad.load("replacer");

        if (replacer != null) {
            // old: AVC 71
            IRIO replacements = replacer.getIVar("@replacements");
            if (replacements != null)
                for (IRIO hk : replacements.getHashKeys())
                    settings.add(new Replacement(hk.decString(), replacements.getHashVal(hk).decString()));
            // new: AVC 72
            replacements = replacer.getIVar("@replacements_list");
            if (replacements != null)
                for (IRIO hk : replacements.getANewArray())
                    settings.add(new Replacement(hk.getIVar("@key").decString(), hk.getIVar("@value").decString()));
    
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

            IRIO partial = replacer.getIVar("@partial");
            if (partial != null) {
                partialMatches = partial.getType() == 'T';
            }
        }

        refreshContents();
        
        proxySetElement(new UISplitterLayout(layout, setSelector, false, 0.5), true);
    }

    private void settingsRemoveByKey(String text) {
        Replacement res = null;
        for (Replacement r : settings)
            if (r.key.equals(text))
                res = r;
        if (res != null)
            settings.remove(res);
    }

    @Override
    public boolean requestsUnparenting() {
        return done; 
    }

    private void refreshContents() {
        layout.panelsClear();

        for (final Replacement key : settings) {
            layout.panelsAdd(new UIAppendButton("-", new UILabel(key.key + " -> " + key.value, FontSizes.dialogWindowTextHeight), new Runnable() {
                @Override
                public void run() {
                    settings.remove(key);
                    refreshContents();
                }
            }, FontSizes.dialogWindowTextHeight));
        }
        layout.panelsAdd(adderA);
        layout.panelsAdd(adderB);
        layout.panelsAdd(adderC);
        layout.panelsAdd(new UITextButton(TXDB.get("Partial Replace (dangerous)"), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                partialMatches = !partialMatches;
            }
        }).togglable(partialMatches));
        layout.panelsAdd(new UITextButton(TXDB.get("Save Config."), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                RubyIO rio = new RubyIO();
                rio.setObject("R48::UniversalStringLocatorSettings");
                RubyIO partial = rio.addIVar("@partial");
                partial.setBool(partialMatches);
                RubyIO replacements = rio.addIVar("@replacements_list");
                replacements.setArray();
                for (Replacement key : settings) {
                    RubyIO replacement = replacements.addAElem(replacements.getALen());
                    replacement.setObject("R48::UniversalStringLocatorReplacement");
                    replacement.addIVar("@key").setString(key.key, true);
                    replacement.addIVar("@value").setString(key.value, true);
                }
                RubyIO files = rio.addIVar("@files");
                files.setArray();
                for (ObjectInfo oi : setSelector.getSet())
                    files.addAElem(files.getALen()).setString(oi.idName, true);
                AdHocSaveLoad.save("replacer", rio);
            }
        }));

        layout.panelsAdd(new UITextButton(TXDB.get("Confirm & Replace"), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                int total = 0;
                int files = 0;
                String log = "";
                for (ObjectInfo objInfo : setSelector.getSet()) {
                    IObjectBackend.ILoadedObject rio = AppMain.objectDB.getObject(objInfo.idName);
                    SchemaElement se = AppMain.schemas.getSDBEntry(objInfo.schemaName);
                    if (rio != null) {
                        files++;
                        int count;
                        if (partialMatches) {
                            final LinkedList<Replacement> ent = new LinkedList<Replacement>(settings);
                            // longer first!
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
                            count = BasicToolset.universalStringLocator(rio.getObject(), new IFunction<IRIO, Integer>() {
                                @Override
                                public Integer apply(IRIO rubyIO) {
                                    StringBuilder res = new StringBuilder();
                                    String dec = rubyIO.decString();
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
                        } else {
                            final HashMap<String, String> map = new HashMap<String, String>();
                            for (Replacement r : settings)
                                map.put(r.key, r.value);
                            count = BasicToolset.universalStringLocator(rio.getObject(), new IFunction<IRIO, Integer>() {
                                @Override
                                public Integer apply(IRIO rubyIO) {
                                    String dec = rubyIO.decString();
                                    String replace = map.get(dec);
                                    if (replace != null) {
                                        rubyIO.setString(replace);
                                        return 1;
                                    }
                                    return 0;
                                }
                            }, true);
                        }
                        total += count;
                        if (count > 0) {
                            SchemaPath sp = new SchemaPath(se, rio);
                            sp.changeOccurred(false);
                            log += "\n" + objInfo.toString() + ": " + count;
                        }
                    }
                }
                AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Made #A total string adjustments across #B files."), new IRIOFixnum(total), new IRIOFixnum(files)) + log);
                done = true;
            }
        }));
    }

    public class Replacement {
        public final String key, value;
        public Replacement(String text, String text2) {
            key = text;
            value = text2;
        }
    }
}
