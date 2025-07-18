/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets;

import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.dbs.CMDB;
import r48.dbs.ObjectInfo;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.map.systems.IRMMapSystem;
import r48.maptools.UIMTEventPicker;
import r48.schema.AggregateSchemaElement;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.util.SchemaPath;
import r48.search.CompoundCommandClassifier;
import r48.search.ICommandClassifier;
import r48.search.RMFindTranslatables;
import r48.toolsets.utils.UIIDChanger;
import r48.ui.dialog.UIRMUniversalStringExportImport;
import r48.ui.dialog.UIRMUniversalStringFinder;
import r48.ui.dialog.UIRMUniversalStringReplacer;
import r48.ui.dialog.UITranscriptControl;
import r48.ui.search.UIClassifierishInstanceWidget;
import r48.ui.search.UICommandSites;

import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Right now this breaks under R2k for various reasons, first being the versionId assumption.
 * Need to shuffle about versionIds and fix that.
 * Secondly, need to switch to using IRMLikeMapInfoBackend full-time.
 * (That was all done eventually.)
 *
 * Ok, list of assumptions made by using this:
 * 1. RPG::Event is a thing
 * 2. Data structures are similar to that of RPG Maker XP
 *
 * Created on 2/12/17.
 */
public class RMTools extends App.Svc implements Consumer<LinkedList<UIPopupMenu.Entry>> {
    private final CMDB commandsEvent;
    private final IRMMapSystem mapSystem;

    public RMTools(App app) {
        super(app);
        // If this errors, then this shouldn't have been constructed.
        mapSystem = (IRMMapSystem) app.system;

        commandsEvent = ((EventCommandArraySchemaElement) AggregateSchemaElement.extractField(app.sdb.getSDBEntry("EventListEditor"), null)).database;
    }

    @Override
    public void accept(LinkedList<UIPopupMenu.Entry> entries) {
        entries.add(new UIPopupMenu.Entry(T.u.mLocateEventCommand, () -> {
            app.ui.launchPrompt(T.u.rmCmdCodeRequest, (s) -> {
                int i;
                try {
                    i = Integer.parseInt(s);
                } catch (Exception e) {
                    app.ui.launchDialog(T.u.dlgBadNum);
                    return;
                }
                for (IRMMapSystem.RMMapData rmd : mapSystem.getAllMaps()) {
                    // Find event!
                    ObjectRootHandle ilo = rmd.getILO(false);
                    if (ilo == null)
                        continue;
                    IRIO mapEvObj = ilo.getObject().getIVar("@events");
                    for (DMKey key : mapEvObj.getHashKeys()) {
                        IRIO event = mapEvObj.getHashVal(key);
                        IRIO pages = event.getIVar("@pages");
                        int alen = pages.getALen();
                        for (int j = 0; j < alen; j++) {
                            IRIO page = pages.getAElem(j);
                            if (page.getType() == '0')
                                continue;
                            IRIO cmds = page.getIVar("@list");
                            int alen2 = cmds.getALen();
                            for (int k = 0; k < alen2; k++) {
                                long cod = cmds.getAElem(k).getIVar("@code").getFX();
                                boolean found;
                                if (i == -1337) {
                                    found = !commandsEvent.knownCommands.containsKey((int) cod);
                                } else {
                                    found = cod == i;
                                }
                                if (found) {
                                    UIMTEventPicker.showEventDivorced(app, key, ilo, event, app.sdb.getSDBEntry("RPG::Event"));
                                    return;
                                }
                            }
                        }
                    }
                }
                app.ui.launchDialog(T.u.notFound);
            });
        }));
        entries.add(new UIPopupMenu.Entry(T.u.mSearchCmdsCEV, () -> {
            ICommandClassifier.Instance ccc = CompoundCommandClassifier.I.instance(app);
            UIClassifierishInstanceWidget<ICommandClassifier.Instance> uiccs = new UIClassifierishInstanceWidget<>(app, ccc);
            UISplitterLayout uspl = new UISplitterLayout(uiccs, new UITextButton(T.g.bConfirm, app.f.dialogWindowTH, () -> {
                final ObjectRootHandle ilo = mapSystem.getCommonEventRoot();
                UICommandSites ucs = new UICommandSites(app, app.odb.getIdByObject(ilo), () -> {
                    RMFindTranslatables rft = new RMFindTranslatables(app, ilo);
                    rft.addSitesFromCommonEvents(mapSystem.getAllCommonEvents(), ccc);
                    return rft.toArray();
                }, new ObjectRootHandle[] {
                    ilo
                });
                ucs.show();
            }), true, 1);
            app.ui.wm.createWindow(uspl, "mSearchCmds");
        }));
        entries.add(new UIPopupMenu.Entry(T.u.mRunAutoCorrect, () -> {
            LinkedList<ObjectInfo> objects = app.getObjectInfos();
            for (final ObjectInfo obj : objects) {
                System.out.println(obj + "...");
                SchemaPath sp = obj.makePath(false);
                if (sp == null)
                    continue;
                Consumer<SchemaPath> modListen = (path) -> {
                    // yup, and throw an exception to give the user an idea of the tree
                    // Note that R48 during an error does NOT write over the main object DB for safety reasons
                    //  (doing so could result in making the situation worse)
                    // the important thing here is that this means autocorrect testing won't lead to the testing env. being poisoned
                    throw new RuntimeException("MODIFY " + obj + " " + path);
                };
                sp.root.registerModificationHandler(modListen);
                sp.editor.modifyVal(sp.targetElement, sp, false);
                sp.root.deregisterModificationHandler(modListen);
                System.out.println(obj + " done.");
            }
        }));
        entries.add(new UIPopupMenu.Entry(T.u.mUniversalStringFinder, () -> {
            app.ui.wm.createWindow(new UIRMUniversalStringFinder(app));
        }));
        entries.add(new UIPopupMenu.Entry(T.u.mUniversalStringReplacer, () -> {
            app.ui.wm.createWindow(new UIRMUniversalStringReplacer(app));
        }));
        entries.add(new UIPopupMenu.Entry(T.u.mUniversalStringExportImport, () -> {
            app.ui.wm.createWindow(new UIRMUniversalStringExportImport(app));
        }));
        entries.add(new UIPopupMenu.Entry(T.u.mTranscriptDump, () -> {
            app.ui.wm.createWindow(new UITranscriptControl(app, mapSystem, commandsEvent));
        }));
        entries.add(new UIPopupMenu.Entry(T.u.mIDChanger, () -> {
            if (app.idc.size() == 0) {
                app.ui.launchDialog(T.u.idc_unavailable);
                return;
            }
            app.ui.wm.createWindow(new UIIDChanger(app, null));
        }));
    }
}
