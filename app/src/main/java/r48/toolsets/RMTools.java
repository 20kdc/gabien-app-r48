/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets;

import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.App;
import r48.dbs.CMDB;
import r48.dbs.ObjectInfo;
import r48.io.IObjectBackend;
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
import r48.ui.UIMenuButton;
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
public class RMTools extends App.Svc {
    private final CMDB commandsEvent;
    private final IRMMapSystem mapSystem;

    public RMTools(App app) {
        super(app);
        // If this errors, then this shouldn't have been constructed.
        mapSystem = (IRMMapSystem) app.system;

        commandsEvent = ((EventCommandArraySchemaElement) AggregateSchemaElement.extractField(app.sdb.getSDBEntry("EventListEditor"), null)).database;
    }

    public UIElement genButton() {
        return new UIMenuButton(app, T.u.mRMTools, app.f.menuTH, null, new String[] {
                T.u.mLocateEventCommand,
                T.u.mSearchCmdsCEV,
                T.u.mRunAutoCorrect,
                T.u.mUniversalStringFinder,
                T.u.mUniversalStringReplacer,
                // 3:24 PM, third day of 2017.
                // This is now a viable option.
                // 3:37 PM, same day.
                // All EventCommands found in the maps of the test subject seem completed.
                // Still need to see to the CommonEvents.
                // next day, um, these tools aren't really doable post-further-modularization (stickynote)
                // 5th January 2017. Here we go.
                T.u.mTranscriptDump,
                T.u.mIDChanger
        }, new Runnable[] {
                () -> {
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
                            IObjectBackend.ILoadedObject ilo = rmd.getILO(false);
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
                                            UIMTEventPicker.showEventDivorced(app, key, ilo, rmd.schemaName, event, "RPG::Event");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        app.ui.launchDialog(T.u.notFound);
                    });
                },
                () -> {
                    ICommandClassifier.Instance ccc = CompoundCommandClassifier.I.instance(app);
                    UIClassifierishInstanceWidget<ICommandClassifier.Instance> uiccs = new UIClassifierishInstanceWidget<>(app, ccc);
                    UISplitterLayout uspl = new UISplitterLayout(uiccs, new UITextButton(T.g.bConfirm, app.f.dialogWindowTH, () -> {
                        final IObjectBackend.ILoadedObject ilo = mapSystem.getCommonEventRoot();
                        UICommandSites ucs = new UICommandSites(app, app.odb.getIdByObject(ilo), () -> {
                            RMFindTranslatables rft = new RMFindTranslatables(app, ilo);
                            rft.addSitesFromCommonEvents(mapSystem.getAllCommonEvents(), ccc);
                            return rft.toArray();
                        }, new IObjectBackend.ILoadedObject[] {
                            ilo
                        });
                        ucs.show();
                    }), true, 1);
                    app.ui.wm.createWindow(uspl, "mSearchCmds");
                },
                () -> {
                    LinkedList<ObjectInfo> objects = app.getObjectInfos();
                    for (final ObjectInfo obj : objects) {
                        System.out.println(obj + "...");
                        SchemaPath sp = obj.makePath(false);
                        if (sp == null)
                            continue;
                        Consumer<SchemaPath> modListen = new Consumer<SchemaPath>() {
                            @Override
                            public void accept(SchemaPath path) {
                                // yup, and throw an exception to give the user an idea of the tree
                                // Note that R48 during an error does NOT write over the main object DB for safety reasons
                                //  (doing so could result in making the situation worse)
                                // the important thing here is that this means autocorrect testing won't lead to the testing env. being poisoneds
                                throw new RuntimeException("MODIFY " + obj + " " + path);
                            }
                        };
                        app.odb.registerModificationHandler(sp.root, modListen);
                        sp.editor.modifyVal(sp.targetElement, sp, false);
                        app.odb.deregisterModificationHandler(sp.root, modListen);
                        System.out.println(obj + " done.");
                    }
                },
                () -> {
                    app.ui.wm.createWindow(new UIRMUniversalStringFinder(app));
                },
                () -> {
                    app.ui.wm.createWindow(new UIRMUniversalStringReplacer(app));
                },
                () -> {
                    app.ui.wm.createWindow(new UITranscriptControl(app, mapSystem, commandsEvent));
                },
                () -> {
                    if (app.idc.size() == 0) {
                        app.ui.launchDialog(T.u.idc_unavailable);
                        return;
                    }
                    app.ui.wm.createWindow(new UIIDChanger(app, null));
                }
        }).centred();
    }
}
