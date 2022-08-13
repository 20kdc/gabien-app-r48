/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.ui.dialog;

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
    private HashMap<String, String> settings = new HashMap<String, String>();

    private UITextBox adderK = new UITextBox("", FontSizes.dialogWindowTextHeight);
    private UITextBox adderV = new UITextBox("", FontSizes.dialogWindowTextHeight);

    private UIElement adderA = new UISplitterLayout(new UILabel("From: ", FontSizes.dialogWindowTextHeight), adderK, false, 0);
    private UIElement adderB = new UISplitterLayout(new UILabel("To: ", FontSizes.dialogWindowTextHeight), adderV, false, 0);
    private UIElement adderC = new UITextButton(TXDB.get("Add replacement"), FontSizes.dialogWindowTextHeight, new Runnable() {
        @Override
        public void run() {
            settings.put(adderK.text, adderV.text);
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
            IRIO replacements = replacer.getIVar("@replacements");
            for (IRIO hk : replacements.getHashKeys())
                settings.put(hk.decString(), replacements.getHashVal(hk).decString());
    
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
        }

        refreshContents();
        
        proxySetElement(new UISplitterLayout(layout, setSelector, false, 0.5), true);
    }

    @Override
    public boolean requestsUnparenting() {
        return done; 
    }

    private void refreshContents() {
        layout.panelsClear();

        final LinkedList<String> keys = new LinkedList<String>(settings.keySet());
        for (String key : keys) {
            final String keyF = key;
            String value = settings.get(key);
            layout.panelsAdd(new UIAppendButton("-", new UILabel(key + " -> " + value, FontSizes.dialogWindowTextHeight), new Runnable() {
                @Override
                public void run() {
                    settings.remove(keyF);
                    refreshContents();
                }
            }, FontSizes.dialogWindowTextHeight));
        }
        layout.panelsAdd(adderA);
        layout.panelsAdd(adderB);
        layout.panelsAdd(adderC);
        layout.panelsAdd(new UITextButton(TXDB.get("Save Config."), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                RubyIO rio = new RubyIO();
                rio.setObject("R48::UniversalStringLocatorSettings");
                RubyIO replacements = rio.addIVar("@replacements");
                replacements.setHash();
                for (String key : keys) {
                    RubyIO hkv = replacements.addHashVal(new RubyIO().setString(key, true));
                    hkv.setString(settings.get(key), true);
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
                LinkedList<ObjectInfo> objects = AppMain.getObjectInfos();

                int total = 0;
                int files = 0;
                String log = "";
                for (ObjectInfo objInfo : setSelector.getSet()) {
                    IObjectBackend.ILoadedObject rio = AppMain.objectDB.getObject(objInfo.idName);
                    SchemaElement se = AppMain.schemas.getSDBEntry(objInfo.schemaName);
                    if (rio != null) {
                        files++;
                        int count = BasicToolset.universalStringLocator(rio.getObject(), new IFunction<IRIO, Integer>() {
                            @Override
                            public Integer apply(IRIO rubyIO) {
                                String dec = rubyIO.decString();
                                String replace = settings.get(dec);
                                if (replace != null) {
                                    rubyIO.setString(replace);
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
                AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Made #A total string adjustments across #B files."), new IRIOFixnum(total), new IRIOFixnum(files)) + log);
                done = true;
            }
        }));
    }
}
