/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.dialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.vfs.FSBackend;
import gabien.ui.UIElement;
import r48.AdHocSaveLoad;
import r48.App;
import r48.dbs.ObjectInfo;
import r48.dbs.RPGCommand;
import r48.io.JsonObjectBackend;
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.schema.SchemaElement;
import r48.schema.specialized.cmgb.RPGCommandSchemaElement;
import r48.schema.util.SchemaPath;
import r48.search.USFROperationMode;
import r48.ui.search.UIUSFROperationModeSelector;

/**
 * Universal string locator fun
 * Created on 15th July, 2025.
 */
public class UIRMUniversalStringExportImport extends App.Prx {
    private UIScrollLayout layout = new UIScrollLayout(true, app.f.generalS);

    private UIObjectInfoSetSelector setSelector;
    private UIUSFROperationModeSelector modeSelector;

    private static final DMKey fieldText = DMKey.ofStr("text");
    private static final DMKey fieldPath = DMKey.ofStr("path");

    public UIRMUniversalStringExportImport(App app) {
        super(app);
        setSelector = new UIObjectInfoSetSelector(app);
        modeSelector = new UIUSFROperationModeSelector(app, app.f.dialogWindowTH);
        Set<ObjectInfo> setCopy = setSelector.getSet();

        // load config if possible
        IRIO replacer = AdHocSaveLoad.load("us_exportimport");

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
        }

        refreshContents();
        
        proxySetElement(new UISplitterLayout(layout, setSelector, false, 0.5), false);
    }

    private void refreshContents() {
        LinkedList<UIElement> elms = new LinkedList<>();

        elms.add(modeSelector);

        elms.add(new UITextButton(T.u.usl_saveConfig, app.f.dialogWindowTH, () -> {
            IRIO rio = new IRIOGeneric(app.ilg.adhocIOContext);
            rio.setObject("R48::UniversalStringExportImportSettings");
            IRIO files = rio.addIVar("@files");
            files.setArray();
            for (ObjectInfo oi : setSelector.getSet())
                files.addAElem(files.getALen()).setString(oi.idName);
            AdHocSaveLoad.save("us_exportimport", rio);
        }));

        elms.add(new UITextButton(T.u.usl_exportJSONDir, app.f.dialogWindowTH, () -> {
            doTheThing(true);
        }));
        elms.add(new UITextButton(T.u.usl_importJSONDir, app.f.dialogWindowTH, () -> {
            doTheThing(false);
        }));
        layout.panelsSet(elms);
    }

    private void doTheThing(boolean export) {
        FSBackend jsonDir = app.gameRoot.intoRelPath("jsonTextExchange");
        jsonDir.mkdirs();
        JsonObjectBackend exchange = new JsonObjectBackend(jsonDir, "", ".json");
        DMContext exchangeCtx = AdHocSaveLoad.newContext();
        HashSet<String> paths = new HashSet<>();
        HashMap<SchemaElement, String> elemToName = app.sdb.getElementToNameCache();
        // continue...
        int total = 0;
        int files = 0;
        StringBuilder log = new StringBuilder();
        USFROperationMode mode = modeSelector.getSelected();
        for (ObjectInfo objInfo : setSelector.getSet()) {
            SchemaPath sp = objInfo.makePath(false);
            if (sp == null)
                continue;
            files++;
            // if we're importing, we try to load the file.
            // if we're exporting, we; don't.
            IRIO jsonFile = null;
            if (export) {
                jsonFile = exchange.newObjectO(objInfo.idName, exchangeCtx);
                jsonFile.setHash();
            } else {
                jsonFile = exchange.loadObjectFromFile(objInfo.idName, exchangeCtx);
            }
            if (jsonFile == null)
                continue;
            final IRIO jsonFileFinal = jsonFile;
            // now do it!
            AtomicInteger finds = new AtomicInteger(0);
            HashSet<IRIO> hsi = new HashSet<>();
            mode.locate(app, sp, (element, target, path) -> {
                if (!hsi.add(target))
                    return false;
                String key = path.toString();
                if (!paths.add(key)) {
                    log.append(T.u.usl_duplicatePath.r(key));
                    return false;
                }
                finds.incrementAndGet();
                if (export) {
                    IRIO irio = jsonFileFinal.addHashVal(DMKey.ofStr(key));
                    irio.setHash();
                    reifyPath(irio.addHashVal(fieldPath), path, elemToName);
                    irio.addHashVal(fieldText).setString(target.decString());
                } else {
                    IRIO irio = jsonFileFinal.getHashVal(DMKey.ofStr(key));
                    if (irio != null) {
                        IRIO res = irio.getHashVal(fieldText);
                        target.setString(res.decString());
                        path.changeOccurred(false);
                    }
                }
                return false;
            }, true);
            // if exporting, save!
            if (export)
                try {
                    exchange.saveObjectToFile(objInfo.idName, jsonFileFinal);
                } catch (IOException e) {
                    app.ui.launchDialog(e);
                }
            int count = finds.get();
            total += count;
        }
        app.ui.launchDialog(T.u.usl_completeReportUSE.r(total, files) + log);
    }

    private void reifyPath(IRIO target, @Nullable SchemaPath path, HashMap<SchemaElement, String> elemToName) {
        target.setArray();
        while (path != null) {
            reifyPathCrumb(target.addAElem(0), path, elemToName);
            path = path.parent;
        }
    }
    private void reifyPathCrumb(IRIO target, @NonNull SchemaPath path, HashMap<SchemaElement, String> elemToName) {
        target.setArray(3);
        reifyPathElement(target.getAElem(0), path, elemToName);
        if (path.hrIndex != null) {
            target.getAElem(1).setString(path.hrIndex);
        } else {
            target.getAElem(1).setNull();
        }
        if (path.lastArrayIndex != null) {
            target.getAElem(2).setDeepClone(path.lastArrayIndex);
        } else {
            target.getAElem(2).setNull();
        }
    }
    private void reifyPathElement(IRIO target, @NonNull SchemaPath path, HashMap<SchemaElement, String> elemToName) {
        SchemaElement se = path.editor;
        if (se == null) {
            target.setNull();
            return;
        }
        if (path.editor instanceof RPGCommandSchemaElement) {
            RPGCommandSchemaElement rc = (RPGCommandSchemaElement) path.editor;
            RPGCommand rm = rc.getRPGCommand(path.targetElement);
            if (rm != null) {
                target.setFX(rm.commandId);
                return;
            }
        }
        String name = elemToName.get(se);
        if (name != null) {
            target.setString(name);
        } else {
            target.setString(se.getClass().getName());
        }
    }
}
