/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.toolsets;

import gabien.GaBIEn;
import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import gabien.ui.UIPopupMenu;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.CMDB;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.maptools.UIMTEventPicker;
import r48.schema.ISchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.UITextPrompt;

import java.io.PrintStream;
import java.util.*;

/**
 * Created on 2/12/17.
 */
public class RMToolsToolset implements IToolset {
    final CMDB commandsEvent;
    final ISchemaElement commandEvent;

    public RMToolsToolset() {
        commandsEvent = AppMain.schemas.getCMDB("R" + StuffRenderer.versionId + "/Commands.txt");
        commandEvent = AppMain.schemas.getSDBEntry("EventCommandEditor");
    }

    @Override
    public String[] tabNames() {
        return new String[]{"Tools"};
    }

    @Override
    public UIElement[] generateTabs(final ISupplier<IConsumer<UIElement>> windowMaker) {
        return new UIElement[]{new UIPopupMenu(new String[]{
                "Locate EventCommand in all Pages",
                "See If Autocorrect Modifies Anything",
                // 3:24 PM, third day of 2017.
                // This is now a viable option.
                // 3:37 PM, same day.
                // All EventCommands found in the maps of the test subject seem completed.
                // Still need to see to the CommonEvents.
                // next day, um, these tools aren't really doable post-further-modularization (stickynote)
                // 5th January 2017. Here we go.
                "Locate incomplete ECs",
                "Locate incomplete ECs (CommonEvents)",
                "Run EC Creation Sanity Check",
                "MEV/CEV Transcript Dump (no Troop/Item/etc.)",
        }, new Runnable[]{
                new Runnable() {
                    @Override
                    public void run() {
                        windowMaker.get().accept(new UITextPrompt("Code?", new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                int i = Integer.parseInt(s);
                                for (RubyIO rio : AppMain.objectDB.getObject("MapInfos").hashVal.keySet()) {
                                    RubyIO map = AppMain.objectDB.getObject(UIMapView.getMapName((int) rio.fixnumVal));
                                    // Find event!
                                    for (RubyIO event : map.getInstVarBySymbol("@events").hashVal.values()) {
                                        for (RubyIO page : event.getInstVarBySymbol("@pages").arrVal) {
                                            for (RubyIO cmd : page.getInstVarBySymbol("@list").arrVal) {
                                                if (cmd.getInstVarBySymbol("@code").fixnumVal == i) {
                                                    UIMTEventPicker.showEvent(event.getInstVarBySymbol("@id").fixnumVal, map, event);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                                AppMain.launchDialog("nnnope");
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
                        for (RubyIO rio : AppMain.objectDB.getObject("MapInfos").hashVal.keySet()) {
                            objects.add(UIMapView.getMapName((int) rio.fixnumVal));
                            objectSchemas.add("RPG::Map");
                        }
                        Iterator<String> schemaIt = objectSchemas.iterator();
                        for (final String obj : objects) {
                            RubyIO map = AppMain.objectDB.getObject(obj);
                            Runnable modListen = new Runnable() {
                                @Override
                                public void run() {
                                    // yup, and throw an exception to give the user an idea of the tree
                                    throw new RuntimeException("MODIFY" + obj);
                                }
                            };
                            AppMain.objectDB.registerModificationHandler(map, modListen);
                            SchemaPath sp = new SchemaPath(AppMain.schemas.getSDBEntry(schemaIt.next()), map, null);
                            sp.editor.modifyVal(map, sp, false);
                            AppMain.objectDB.deregisterModificationHandler(map, modListen);
                        }
                        AppMain.launchDialog("nnnope");
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        for (RubyIO rio : AppMain.objectDB.getObject("MapInfos").hashVal.keySet()) {
                            RubyIO map = AppMain.objectDB.getObject(UIMapView.getMapName((int) rio.fixnumVal));
                            // Find event!
                            for (RubyIO event : map.getInstVarBySymbol("@events").hashVal.values()) {
                                for (RubyIO page : event.getInstVarBySymbol("@pages").arrVal) {
                                    for (RubyIO cmd : page.getInstVarBySymbol("@list").arrVal) {
                                        if (!commandsEvent.knownCommands.containsKey((int) cmd.getInstVarBySymbol("@code").fixnumVal)) {
                                            System.out.println(cmd.getInstVarBySymbol("@code").fixnumVal);
                                            UIMTEventPicker.showEvent(event.getInstVarBySymbol("@id").fixnumVal, map, event);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        AppMain.launchDialog("nnnope");
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        for (RubyIO rio : AppMain.objectDB.getObject("CommonEvents").arrVal) {
                            if (rio.type != '0') {
                                for (RubyIO cmd : rio.getInstVarBySymbol("@list").arrVal) {
                                    if (!commandsEvent.knownCommands.containsKey((int) cmd.getInstVarBySymbol("@code").fixnumVal)) {
                                        System.out.println(rio.getInstVarBySymbol("@id").fixnumVal);
                                        System.out.println(cmd.getInstVarBySymbol("@code").fixnumVal);
                                        AppMain.launchDialog("yup (chk.console)");
                                        return;
                                    }
                                }
                            }
                        }
                        AppMain.launchDialog("nnnope");
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        // This won't deal with the really complicated disambiguator events, but it's something.
                        for (int cc : commandsEvent.knownCommands.keySet()) {
                            RubyIO r = SchemaPath.createDefaultValue(commandEvent, new RubyIO().setFX(0));
                            SchemaPath sp = new SchemaPath(commandEvent, r, null).arrayHashIndex(new RubyIO().setFX(0), "BLAH");
                            // this is bad, but it works well enough
                            r.getInstVarBySymbol("@code").fixnumVal = cc;
                            commandEvent.modifyVal(r, sp, false);
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        PrintStream ps = new PrintStream(GaBIEn.getOutFile("transcript.html"));
                        RMTranscriptDumper dumper = new RMTranscriptDumper(ps);
                        dumper.start();
                        dumper.startFile("CommonEvents", "Common Events");
                        for (RubyIO rio : AppMain.objectDB.getObject("CommonEvents").arrVal)
                            if (rio.type != '0')
                                dumper.dump(rio.getInstVarBySymbol("@name").decString(), rio.getInstVarBySymbol("@list").arrVal, commandsEvent);
                        dumper.endFile();
                        // Order the maps so that it comes out coherently for valid diffs (OSER Equinox Comparison Project)
                        LinkedList<Integer> orderedMapInfos = new LinkedList<Integer>();
                        for (Map.Entry<RubyIO, RubyIO> rio2 : AppMain.objectDB.getObject("MapInfos").hashVal.entrySet())
                            orderedMapInfos.add((int) rio2.getKey().fixnumVal);
                        Collections.sort(orderedMapInfos);
                        for (int id : orderedMapInfos) {
                            String name = UIMapView.getMapName(id);
                            dumper.startFile(name, "Map:\"" + AppMain.objectDB.getObject("MapInfos").getHashVal(new RubyIO().setFX(id)).getInstVarBySymbol("@name").decString() + "\"");
                            RubyIO map = AppMain.objectDB.getObject(name);
                            LinkedList<Integer> orderedEVN = new LinkedList<Integer>();
                            for (RubyIO i : map.getInstVarBySymbol("@events").hashVal.keySet())
                                orderedEVN.add((int) i.fixnumVal);
                            Collections.sort(orderedEVN);
                            for (int k : orderedEVN) {
                                RubyIO event = map.getInstVarBySymbol("@events").getHashVal(new RubyIO().setFX(k));
                                String evp = "Ev." + k + " (" + event.getInstVarBySymbol("@name").decString() + "), Page ";
                                int pageId = 1;
                                for (RubyIO page : event.getInstVarBySymbol("@pages").arrVal) {
                                    dumper.dump(evp + pageId, page.getInstVarBySymbol("@list").arrVal, commandsEvent);
                                    pageId++;
                                }
                            }
                            dumper.endFile();
                        }

                        dumper.startFile("Items", "The list of items in the game.");
                        LinkedList<String> lls = new LinkedList<String>();
                        for (RubyIO page : AppMain.objectDB.getObject("Items").arrVal) {
                            if (page.type != '0') {
                                lls.add(page.getInstVarBySymbol("@name").decString());
                            } else {
                                lls.add("<NULL>");
                            }
                        }
                        dumper.dumpBasicList("Names", lls.toArray(new String[0]), 0);
                        dumper.endFile();

                        dumper.startFile("System", "System data (of any importance, anyway).");
                        RubyIO sys = AppMain.objectDB.getObject("System");

                        dumper.dumpHTML("Notably, switch and variable lists have a 0th index, but only indexes starting from 1 are actually allowed to be used.<br/>");
                        dumper.dumpHTML("Magic number is " + sys.getInstVarBySymbol("@magic_number").toString() + "<br/>");
                        dumper.dumpHTML("Magic number II is " + sys.getInstVarBySymbol("@_").toString() + "<br/>");

                        dumper.dumpSVList("@switches", sys.getInstVarBySymbol("@switches").arrVal, 0);
                        dumper.dumpSVList("@variables", sys.getInstVarBySymbol("@variables").arrVal, 0);
                        dumper.endFile();

                        dumper.end();
                        ps.close();
                    }
                }
        }, FontSizes.menuTextHeight, false)
        };
    }
}
