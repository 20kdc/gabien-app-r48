/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
import r48.App;
import r48.FontSizes;
import r48.dbs.CMDB;
import r48.dbs.ObjectInfo;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.map.systems.IRMMapSystem;
import r48.maptools.UIMTEventPicker;
import r48.schema.AggregateSchemaElement;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.util.SchemaPath;
import r48.toolsets.utils.CommandSite;
import r48.toolsets.utils.RMFindTranslatables;
import r48.toolsets.utils.UICommandSites;
import r48.ui.UIMenuButton;
import r48.ui.dialog.UIRMUniversalStringLocator;
import r48.ui.dialog.UITextPrompt;
import r48.ui.dialog.UITranscriptControl;

import java.util.LinkedList;

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
        return new UIMenuButton(app, TXDB.get("RM-Tools"), FontSizes.menuTextHeight, null, new String[] {
                TXDB.get("Locate EventCommand in all Pages"),
                TXDB.get("Find Translatables in Common Events"),
                TXDB.get("See If Autocorrect Modifies Anything"),
                TXDB.get("Universal String Replace"),
                // 3:24 PM, third day of 2017.
                // This is now a viable option.
                // 3:37 PM, same day.
                // All EventCommands found in the maps of the test subject seem completed.
                // Still need to see to the CommonEvents.
                // next day, um, these tools aren't really doable post-further-modularization (stickynote)
                // 5th January 2017. Here we go.
                TXDB.get("MEV/CEV Transcript Dump (no Troop/Item/etc.)"),
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Code (or -1337 for any unknown) ?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                int i;
                                try {
                                    i = Integer.parseInt(s);
                                } catch (Exception e) {
                                    app.ui.launchDialog(TXDB.get("Not a valid number."));
                                    return;
                                }
                                for (IRMMapSystem.RMMapData rmd : mapSystem.getAllMaps()) {
                                    // Find event!
                                    IObjectBackend.ILoadedObject ilo = rmd.getILO(false);
                                    if (ilo == null)
                                        continue;
                                    IRIO mapEvObj = ilo.getObject().getIVar("@events");
                                    for (IRIO key : mapEvObj.getHashKeys()) {
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
                                app.ui.launchDialog(TXDB.get("Not found."));
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        final IObjectBackend.ILoadedObject ilo = mapSystem.getCommonEventRoot();
                        UICommandSites ucs = new UICommandSites(app, app.odb.getIdByObject(ilo), new ISupplier<CommandSite[]>() {
                            @Override
                            public CommandSite[] get() {
                                RMFindTranslatables rft = new RMFindTranslatables(app, ilo);
                                rft.addSitesFromCommonEvents(mapSystem.getAllCommonEvents());
                                return rft.toArray();
                            }
                        }, new IObjectBackend.ILoadedObject[] {
                            ilo
                        });
                        ucs.show();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        LinkedList<ObjectInfo> objects = app.getObjectInfos();
                        for (final ObjectInfo obj : objects) {
                            System.out.println(obj + "...");
                            IObjectBackend.ILoadedObject map = obj.getILO(false);
                            if (map == null)
                                continue;
                            IConsumer<SchemaPath> modListen = new IConsumer<SchemaPath>() {
                                @Override
                                public void accept(SchemaPath path) {
                                    // yup, and throw an exception to give the user an idea of the tree
                                    // Note that R48 during an error does NOT write over the main object DB for safety reasons
                                    //  (doing so could result in making the situation worse)
                                    // the important thing here is that this means autocorrect testing won't lead to the testing env. being poisoneds
                                    throw new RuntimeException("MODIFY " + obj + " " + path);
                                }
                            };
                            app.odb.registerModificationHandler(map, modListen);
                            SchemaPath sp = new SchemaPath(app.sdb.getSDBEntry(obj.schemaName), map);
                            sp.editor.modifyVal(map.getObject(), sp, false);
                            app.odb.deregisterModificationHandler(map, modListen);
                            System.out.println(obj + " done.");
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UIRMUniversalStringLocator(app));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITranscriptControl(app, mapSystem, commandsEvent));
                    }
                }
        }).centred();
    }
}
