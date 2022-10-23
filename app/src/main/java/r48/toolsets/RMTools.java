/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.toolsets;

import gabien.GaBIEn;
import gabien.uslx.append.*;
import gabien.uslx.append.*;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.FormatSyntax;
import r48.dbs.ObjectInfo;
import r48.dbs.RPGCommand;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.IObjectBackend.ILoadedObject;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.map.IMapToolContext;
import r48.map.mapinfos.RXPRMLikeMapInfoBackend;
import r48.map.systems.IRMMapSystem;
import r48.maptools.UIMTEventPicker;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.toolsets.utils.CommandSite;
import r48.toolsets.utils.RMFindTranslatables;
import r48.toolsets.utils.RMTranscriptDumper;
import r48.toolsets.utils.UICommandSites;
import r48.ui.UIMenuButton;
import r48.ui.dialog.UIRMUniversalStringLocator;
import r48.ui.dialog.UITextPrompt;
import r48.ui.dialog.UITranscriptControl;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
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
public class RMTools {
    private final CMDB commandsEvent;
    private final IRMMapSystem mapSystem;

    public RMTools() {
        // If this errors, then this shouldn't have been constructed.
        mapSystem = (IRMMapSystem) AppMain.system;

        commandsEvent = ((EventCommandArraySchemaElement) AggregateSchemaElement.extractField(AppMain.schemas.getSDBEntry("EventListEditor"), null)).database;
    }

    public UIElement genButton() {
        return new UIMenuButton(TXDB.get("RM-Tools"), FontSizes.menuTextHeight, null, new String[] {
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
                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Code (or -1337 for any unknown) ?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                int i;
                                try {
                                    i = Integer.parseInt(s);
                                } catch (Exception e) {
                                    AppMain.launchDialog(TXDB.get("Not a valid number."));
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
                                                    UIMTEventPicker.showEventDivorced(key, ilo, rmd.schemaName, event, "RPG::Event");
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                                AppMain.launchDialog(TXDB.get("Not found."));
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        final IObjectBackend.ILoadedObject ilo = mapSystem.getCommonEventRoot();
                        UICommandSites ucs = new UICommandSites(AppMain.objectDB.getIdByObject(ilo), new ISupplier<CommandSite[]>() {
                            @Override
                            public CommandSite[] get() {
                                RMFindTranslatables rft = new RMFindTranslatables(ilo);
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
                        LinkedList<ObjectInfo> objects = AppMain.getObjectInfos();
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
                            AppMain.objectDB.registerModificationHandler(map, modListen);
                            SchemaPath sp = new SchemaPath(AppMain.schemas.getSDBEntry(obj.schemaName), map);
                            sp.editor.modifyVal(map.getObject(), sp, false);
                            AppMain.objectDB.deregisterModificationHandler(map, modListen);
                            System.out.println(obj + " done.");
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UIRMUniversalStringLocator());
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UITranscriptControl(mapSystem, commandsEvent));
                    }
                }
        }).centred();
    }
}
