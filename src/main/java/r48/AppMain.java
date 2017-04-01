/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import r48.dbs.ATDB;
import r48.dbs.ObjectDB;
import r48.dbs.SDB;
import r48.io.IkaObjectBackend;
import r48.io.R48ObjectBackend;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.musicality.Musicality;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaHostImpl;
import r48.schema.util.SchemaPath;
import r48.toolsets.IToolset;
import r48.toolsets.MapToolset;
import r48.toolsets.RMToolsToolset;
import r48.ui.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Pre-release development notice. 31 Dec, 2016.
 * I'll finish some commands before releasing, but this is still going to be released a bit early.
 * Several schemas are missing. I guess it's okay enough that the schemas that do exist, well, exist...
 * ... but it would be nice if everything was in place. Oh well.
 * At least something good will come out of this year.
 * I've added the original Inspector (UITest) as a launchable thing so that examining data to write new schemas is possible.
 * Hopefully the system is flexible enough to support everything now, at least more or less.
 * In any case, if you're reading this you're examining the code.
 * This class holds the static members for several critical databases,
 * needed to keep the system running.
 * So, uh, don't lose it.
 * <p/>
 * -- NOTE: This is a 2017 version of the code,
 * since I decided to actually finish it.
 * If I do get around to releasing it,
 * well, you'll find the new features yourself,
 * I'm sure of it. --
 * <p/>
 * Created on 12/27/16.
 */
public class AppMain {
    // Where new windows go
    private static IConsumer<UIElement> windowMaker;

    private static UILabel uiStatusLabel;

    public static StuffRenderer stuffRenderer;

    public static UIElement nextMapTool = null;

    public static String rootPath = null;
    public static String dataPath = "";
    public static String dataExt = "";
    public static String odbBackend = "<you forgot to select a backend>";

    public static ObjectDB objectDB = null;

    // Databases
    public static ATDB[] autoTiles = new ATDB[0];
    public static SDB schemas = null;

    // State for in-system copy/paste
    public static RubyIO theClipboard = null;

    // Images
    public static IGrInDriver.IImage layerTabs = GaBIEn.getImage("layertab.png", 0, 0, 0);

    public static void initialize(String gamepack) throws IOException {
        rootPath = "";

        // initialize core resources

        schemas = new SDB();

        InputStreamReader fr = new InputStreamReader(GaBIEn.getFile(gamepack + "Schema.txt"));
        schemas.readFile(new BufferedReader(fr));
        fr.close();

        // initialize everything else that needs initializing, starting with ObjectDB

        if (odbBackend.equals("r48")) {
            objectDB = new ObjectDB(new R48ObjectBackend(rootPath + dataPath, dataExt, true));
        } else if (odbBackend.equals("ika")) {
            objectDB = new ObjectDB(new IkaObjectBackend(rootPath));
        } else {
            throw new IOException("Unknown backend " + odbBackend);
        }

        // Final internal consistency checks and reading in dictionaries from target
        //  before starting the UI, which can cause external consistency checks
        //  (...and potentially cause havoc in the process)

        schemas.updateDictionaries();
        schemas.confirmAllExpectationsMet();
    }

    public static IConsumer<Double> initializeAndRun(final IConsumer<UIElement> uiTicker) {

        // initialize UI
        final UIWindowView rootView = new UIWindowView();
        rootView.windowTextHeight = FontSizes.windowFrameHeight;
        windowMaker = rootView;
        rootView.setBounds(new Rect(0, 0, 640, 480));

        // Set up a default stuffRenderer for things to use.
        stuffRenderer = new StuffRenderer(null, "");

        rebuildInnerUI(rootView, uiTicker);

        // everything ready, start main window
        uiTicker.accept(rootView);

        return new IConsumer<Double>() {
            @Override
            public void accept(Double deltaTime) {
                uiStatusLabel.Text = objectDB.modifiedObjects.size() + " modified.";
                schemas.updateDictionaries();
                if (Musicality.running)
                    Musicality.update(deltaTime);
            }
        };
    }

    private static UITabPane initializeTabs(final UIWindowView rootView, final IConsumer<UIElement> uiTicker) {
        LinkedList<String> tabNames = new LinkedList<String>();
        LinkedList<UIElement> tabElems = new LinkedList<UIElement>();

        LinkedList<IToolset> toolsets = new LinkedList<IToolset>();
        // Until a future time, this is hard-coded as the classname of a map being created via MapInfos.
        // Probably simple enough to create a special alias, but meh.
        if (AppMain.schemas.hasSDBEntry("RPG::Map"))
            toolsets.add(new MapToolset());
        if (AppMain.schemas.hasSDBEntry("EventCommandEditor"))
            toolsets.add(new RMToolsToolset());

        // Initialize toolsets.
        ISupplier<IConsumer<UIElement>> wmg = new ISupplier<IConsumer<UIElement>>() {
            @Override
            public IConsumer<UIElement> get() {
                return windowMaker;
            }
        };
        for (IToolset its : toolsets) {
            String[] tabs = its.tabNames();
            UIElement[] tabContents = its.generateTabs(wmg);
            for (int i = 0; i < tabs.length; i++) {
                tabNames.add(tabs[i]);
                tabElems.add(tabContents[i]);
            }
        }

        tabNames.add("ObjectDB Monitor");
        tabElems.add(new UIObjectDBMonitor());
        tabNames.add("System Objects");
        tabElems.add(makeFileList());
        tabNames.add("System Tools");
        tabElems.add(new UIPopupMenu(new String[] {
                "Edit Object",
                "New Object via Schema, ODB'AnonObject'",
                "Inspect Object (no Schema needed)",
                "Set Internal Windows (good)",
                "Set External Windows (bad)",
                "Use normal in-built fonts",
                "Make text N.I.Z.X.-compatible",
                "Toggle calming sound",
                "Configure font sizes",
                "Rebuild UI",
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        windowMaker.accept(new UITextPrompt("Object Name?", new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final RubyIO rio = objectDB.getObject(s);
                                if (schemas.hasSDBEntry("File." + s)) {
                                    launchSchema("File." + s, rio);
                                    return;
                                }
                                if (rio != null) {
                                    if (rio.type == 'o') {
                                        if (rio.symVal != null) {
                                            if (schemas.hasSDBEntry(rio.symVal)) {
                                                launchSchema(rio.symVal, rio);
                                                return;
                                            }
                                        }
                                    }
                                    windowMaker.accept(new UITextPrompt("Schema ID?", new IConsumer<String>() {
                                        @Override
                                        public void accept(String s) {
                                            launchSchema(s, rio);
                                        }
                                    }));
                                } else {
                                    launchDialog("No file, or schema to create it.");
                                }
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        windowMaker.accept(new UITextPrompt("Schema ID?", new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                launchSchema(s, SchemaPath.createDefaultValue(schemas.getSDBEntry(s), new RubyIO().setFX(0)));
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        windowMaker.accept(new UITextPrompt("Object Name?", new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                windowMaker.accept(new UITest(objectDB.getObject(s)));
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        windowMaker = rootView;
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        windowMaker = uiTicker;
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        UILabel.iAmAbsolutelySureIHateTheFont = false;
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        UILabel.iAmAbsolutelySureIHateTheFont = true;
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        if (!Musicality.initialized)
                            Musicality.initialize();
                        if (Musicality.running) {
                            Musicality.kill();
                        } else {
                            Musicality.boot();
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        windowMaker.accept(new UIFontSizeConfigurator());
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        rebuildInnerUI(rootView, uiTicker);
                    }
                }
        }, FontSizes.menuTextHeight, false));
        return new UITabPane(tabNames.toArray(new String[0]), tabElems.toArray(new UIElement[0]), FontSizes.tabTextHeight);
    }

    private static void rebuildInnerUI(final UIWindowView rootView, final IConsumer<UIElement> uiTicker) {
        uiStatusLabel = new UILabel("Loading...", FontSizes.statusBarTextHeight);

        UIAppendButton workspace = new UIAppendButton("Save All Modified Files", uiStatusLabel, new Runnable() {
            @Override
            public void run() {
                objectDB.ensureAllSaved();
            }
        }, FontSizes.statusBarTextHeight);
        workspace = new UIAppendButton(" Help", workspace, new Runnable() {
            @Override
            public void run() {
                // exception to the rule
                UILabel uil = new UILabel("Blank Help Window", FontSizes.helpPathHeight);
                final UIHelpSystem uis = new UIHelpSystem(uil, null, null);
                final UIScrollVertLayout uus = new UIScrollVertLayout();
                uus.panels.add(uis);
                uus.setBounds(new Rect(0, 0, 560, 240));
                final UINSVertLayout topbar = new UINSVertLayout(new UIAppendButton("Index", uil, new Runnable() {
                    @Override
                    public void run() {
                        uis.loadPage(0);
                    }
                }, FontSizes.helpPathHeight), uus);
                uis.onLoad = new Runnable() {
                    @Override
                    public void run() {
                        Rect b = topbar.getBounds();
                        topbar.setBounds(new Rect(0, 0, 16, 16));
                        topbar.setBounds(b);
                    }
                };
                uis.loadPage(0);
                windowMaker.accept(topbar);
            }
        }, FontSizes.statusBarTextHeight);
        rootView.backing = new UINSVertLayout(workspace, initializeTabs(rootView, uiTicker));
    }

    private static UIElement makeFileList() {
        LinkedList<String> s = schemas.listFileDefs();
        LinkedList<Runnable> r = new LinkedList<Runnable>();
        for (final String s2 : s)
            r.add(new Runnable() {
                @Override
                public void run() {
                    launchSchema("File." + s2, objectDB.getObject(s2));
                }
            });
        return new UIPopupMenu(s.toArray(new String[0]), r.toArray(new Runnable[0]), FontSizes.menuTextHeight, false);
    }

    // this includes objects whose existence is defined by objects (maps)
    public static LinkedList<String> listAllObjects() {
        LinkedList<String> base = schemas.listFileDefs();
        for (RubyIO k : objectDB.getObject("MapInfos").hashVal.keySet())
            base.add(UIMapView.getMapName((int) k.fixnumVal));
        return base;
    }

    // Notably, you can't use this for non-roots because you'll end up bypassing ObjectDB.
    public static ISchemaHost launchSchema(String s, RubyIO rio) {
        // Responsible for keeping listeners in place so nothing breaks.
        SchemaHostImpl watcher = new SchemaHostImpl(windowMaker);
        watcher.switchObject(new SchemaPath(schemas.getSDBEntry(s), rio, watcher));
        return watcher;
    }

    public static ISchemaHost launchNonRootSchema(RubyIO root, String rootSchema, RubyIO array, String arraySchema, RubyIO arrayIndex, RubyIO element, String elementSchema, String indexText) {
        // produce a valid (and false) parent chain, that handles all required guarantees.
        ISchemaHost shi = launchSchema(rootSchema, root);
        SchemaPath sp = new SchemaPath(AppMain.schemas.getSDBEntry(rootSchema), root, shi);
        sp = sp.arrayEntry(array, AppMain.schemas.getSDBEntry(arraySchema));
        sp = sp.arrayHashIndex(arrayIndex, indexText);
        shi.switchObject(sp.newWindow(AppMain.schemas.getSDBEntry(elementSchema), element, shi));
        return shi;
    }

    public static void launchDialog(String s) {
        windowMaker.accept(new UILabel(s, FontSizes.dialogWindowTextHeight));
    }
}
