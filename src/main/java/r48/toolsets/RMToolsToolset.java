/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.toolsets;

import gabien.GaBIEn;
import gabien.ui.IConsumer;
import gabien.ui.IFunction;
import gabien.ui.UIElement;
import gabien.ui.UIPopupMenu;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.map.mapinfos.RXPRMLikeMapInfoBackend;
import r48.map.systems.IRMMapSystem;
import r48.maptools.UIMTEventPicker;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.dialog.UITextPrompt;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
public class RMToolsToolset implements IToolset {
    private final CMDB commandsEvent;
    private final IRMMapSystem mapSystem;

    public RMToolsToolset(String gamepak) {
        // If this errors, then this shouldn't have been constructed.
        mapSystem = (IRMMapSystem) AppMain.system;

        commandsEvent = AppMain.schemas.getCMDB(gamepak + "Commands.txt");
    }

    @Override
    public UIElement[] generateTabs() {
        return new UIElement[] {new UIPopupMenu(new String[] {
                TXDB.get("Locate EventCommand in all Pages"),
                TXDB.get("See If Autocorrect Modifies Anything"),
                TXDB.get("Universal String Replace"),
                // 3:24 PM, third day of 2017.
                // This is now a viable option.
                // 3:37 PM, same day.
                // All EventCommands found in the maps of the test subject seem completed.
                // Still need to see to the CommonEvents.
                // next day, um, these tools aren't really doable post-further-modularization (stickynote)
                // 5th January 2017. Here we go.
                TXDB.get("Locate incomplete ECs"),
                TXDB.get("Locate incomplete ECs (CommonEvents)"),
                TXDB.get("MEV/CEV Transcript Dump (no Troop/Item/etc.)"),
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Code?"), new IConsumer<String>() {
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
                                    for (Map.Entry<RubyIO, RubyIO> event : rmd.map.getInstVarBySymbol("@events").hashVal.entrySet()) {
                                        for (RubyIO page : event.getValue().getInstVarBySymbol("@pages").arrVal) {
                                            if (page.type == '0')
                                                continue;
                                            for (RubyIO cmd : page.getInstVarBySymbol("@list").arrVal) {
                                                if (cmd.getInstVarBySymbol("@code").fixnumVal == i) {
                                                    UIMTEventPicker.showEventDivorced(event.getKey(), rmd.map, rmd.schemaName, event.getValue(), "RPG::Event");
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
                        LinkedList<String> objects = new LinkedList<String>();
                        LinkedList<String> objectSchemas = new LinkedList<String>();
                        for (String s : AppMain.schemas.listFileDefs()) {
                            objects.add(s);
                            objectSchemas.add("File." + s);
                        }
                        for (IRMMapSystem.RMMapData rio : mapSystem.getAllMaps()) {
                            objects.add(rio.idName);
                            objectSchemas.add(rio.schemaName);
                        }
                        for (final String obj : objects) {
                            System.out.println(obj + "...");
                            RubyIO map = AppMain.objectDB.getObject(obj);
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
                            SchemaPath sp = new SchemaPath(AppMain.schemas.getSDBEntry(objectSchemas.removeFirst()), map);
                            sp.editor.modifyVal(map, sp, false);
                            AppMain.objectDB.deregisterModificationHandler(map, modListen);
                            System.out.println(obj + " done.");
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Find?"), new IConsumer<String>() {
                            @Override
                            public void accept(final String find) {
                                AppMain.window.createWindow(new UITextPrompt(TXDB.get("Replace?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(final String repl) {
                                        LinkedList<String> objects = new LinkedList<String>();
                                        LinkedList<String> objectSchemas = new LinkedList<String>();
                                        for (String s : AppMain.schemas.listFileDefs()) {
                                            objects.add(s);
                                            objectSchemas.add("File." + s);
                                        }
                                        for (IRMMapSystem.RMMapData rio : mapSystem.getAllMaps()) {
                                            objects.add(rio.idName);
                                            objectSchemas.add(rio.schemaName);
                                        }
                                        int total = 0;
                                        String log = "";
                                        for (String s : objects) {
                                            RubyIO rio = AppMain.objectDB.getObject(s);
                                            SchemaElement se = AppMain.schemas.getSDBEntry(objectSchemas.removeFirst());
                                            if (rio != null) {
                                                int count = BasicToolset.universalStringLocator(rio, new IFunction<RubyIO, Integer>() {
                                                    @Override
                                                    public Integer apply(RubyIO rubyIO) {
                                                        if (rubyIO.decString().equals(find)) {
                                                            rubyIO.encString(repl, false);
                                                            return 1;
                                                        }
                                                        return 0;
                                                    }
                                                }, true);
                                                total += count;
                                                if (count > 0) {
                                                    SchemaPath sp = new SchemaPath(se, rio);
                                                    sp.changeOccurred(false);
                                                    log += "\n" + s + ": " + count;
                                                }
                                            }
                                        }
                                        AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Made #A total string adjustments."), new RubyIO().setFX(total)) + log);
                                    }
                                }));
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        for (IRMMapSystem.RMMapData rmd : mapSystem.getAllMaps()) {
                            // Find event!
                            for (Map.Entry<RubyIO, RubyIO> event : rmd.map.getInstVarBySymbol("@events").hashVal.entrySet()) {
                                for (RubyIO page : event.getValue().getInstVarBySymbol("@pages").arrVal) {
                                    if (page.type == '0')
                                        continue;
                                    for (RubyIO cmd : page.getInstVarBySymbol("@list").arrVal) {
                                        if (!commandsEvent.knownCommands.containsKey((int) cmd.getInstVarBySymbol("@code").fixnumVal)) {
                                            System.out.println(cmd.getInstVarBySymbol("@code").fixnumVal);
                                            UIMTEventPicker.showEventDivorced(event.getKey(), rmd.map, rmd.schemaName, event.getValue(), "RPG::Event");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        AppMain.launchDialog(TXDB.get("Not found."));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        for (RubyIO rio : mapSystem.getAllCommonEvents())
                            if (rio.type != '0') {
                                for (RubyIO cmd : rio.getInstVarBySymbol("@list").arrVal) {
                                    if (!commandsEvent.knownCommands.containsKey((int) cmd.getInstVarBySymbol("@code").fixnumVal)) {
                                        System.out.println(rio.getInstVarBySymbol("@id").fixnumVal);
                                        System.out.println(cmd.getInstVarBySymbol("@code").fixnumVal);
                                        AppMain.launchDialog(TXDB.get("Found an unknown event command - Check the console."));
                                        return;
                                    }
                                }
                            }
                        AppMain.launchDialog(TXDB.get("Not found."));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        PrintStream ps = null;
                        try {
                            ps = new PrintStream(GaBIEn.getOutFile(AppMain.rootPath + "transcript.html"), false, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        RMTranscriptDumper dumper = new RMTranscriptDumper(ps);
                        dumper.start();
                        dumper.startFile("CommonEvents", TXDB.get("Common Events"));
                        for (RubyIO rio : mapSystem.getAllCommonEvents())
                            dumper.dump(rio.getInstVarBySymbol("@name").decString(), rio.getInstVarBySymbol("@list").arrVal, commandsEvent);
                        dumper.endFile();
                        // Order the maps so that it comes out coherently for valid diffs (OSER Solstice Comparison Project)
                        LinkedList<Integer> orderedMapInfos = new LinkedList<Integer>();
                        HashMap<Integer, IRMMapSystem.RMMapData> mapMap = new HashMap<Integer, IRMMapSystem.RMMapData>();
                        for (IRMMapSystem.RMMapData rmd : mapSystem.getAllMaps()) {
                            orderedMapInfos.add(rmd.id);
                            mapMap.put(rmd.id, rmd);
                        }
                        Collections.sort(orderedMapInfos);
                        for (int id : orderedMapInfos) {
                            IRMMapSystem.RMMapData rmd = mapMap.get(id);
                            dumper.startFile(RXPRMLikeMapInfoBackend.sNameFromInt(rmd.id), FormatSyntax.formatExtended(TXDB.get("Map:#A"), new RubyIO().setString(rmd.name, true)));
                            RubyIO map = rmd.map;
                            // We need to temporarily override map context.
                            // This'll fix itself by next frame...
                            AppMain.schemas.updateDictionaries(map);
                            AppMain.schemas.kickAllDictionariesForMapChange();
                            LinkedList<Integer> orderedEVN = new LinkedList<Integer>();
                            for (RubyIO i : map.getInstVarBySymbol("@events").hashVal.keySet())
                                orderedEVN.add((int) i.fixnumVal);
                            Collections.sort(orderedEVN);
                            for (int k : orderedEVN) {
                                RubyIO event = map.getInstVarBySymbol("@events").getHashVal(new RubyIO().setFX(k));
                                int pageId = 1;
                                for (RubyIO page : event.getInstVarBySymbol("@pages").arrVal) {
                                    if (page.type == '0')
                                        continue; // 0th page on R2k backend.
                                    dumper.dump(FormatSyntax.formatExtended(TXDB.get("Ev.#A #C, page #B"), new RubyIO().setFX(k), new RubyIO().setFX(pageId), event.getInstVarBySymbol("@name")), page.getInstVarBySymbol("@list").arrVal, commandsEvent);
                                    pageId++;
                                }
                            }
                            dumper.endFile();
                        }
                        // Prevent breakage
                        AppMain.schemas.updateDictionaries(null);

                        mapSystem.dumpCustomData(dumper);

                        dumper.end();
                        ps.close();
                        AppMain.launchDialog(TXDB.get("transcript.html was written to the target's folder."));
                    }
                }
        }, FontSizes.menuTextHeight, FontSizes.menuScrollersize, false) {
            @Override
            public String toString() {
                return TXDB.get("Tools");
            }
        }
        };
    }
}
