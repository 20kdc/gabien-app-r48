/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import r48.dbs.*;
import r48.io.IkaObjectBackend;
import r48.io.R2kObjectBackend;
import r48.io.R48ObjectBackend;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaHostImpl;
import r48.schema.util.SchemaPath;
import r48.map.systems.*;
import r48.toolsets.BasicToolset;
import r48.toolsets.IToolset;
import r48.toolsets.MapToolset;
import r48.toolsets.RMToolsToolset;
import r48.ui.*;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;

import java.io.IOException;
import java.util.HashSet;
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

    // Scheduled tasks
    public static HashSet<Runnable> pendingRunnables = new HashSet<Runnable>();

    private static UILabel uiStatusLabel;

    public static UIElement nextMapTool = null;

    public static String rootPath = null;
    public static String dataPath = "";
    public static String dataExt = "";
    public static String odbBackend = "<you forgot to select a backend>";
    // Null system backend will always "work"
    public static String sysBackend = "null";

    // Databases
    public static ObjectDB objectDB = null;
    public static ATDB[] autoTiles = new ATDB[0];
    public static SDB schemas = null;

    // Backend Services

    // The global context-independent stuffRenderer. *Only use outside of maps.*
    public static StuffRenderer stuffRendererIndependent;
    public static MapSystem system;
    public static IMapContext mapContext;

    // State for in-system copy/paste
    public static RubyIO theClipboard = null;

    // Images
    public static IGrInDriver.IImage layerTabs = GaBIEn.getImageCK("layertab.png", 0, 0, 0);
    public static IGrInDriver.IImage noMap = GaBIEn.getImageCK("nomad.png", 0, 0, 0);

    public static IConsumer<Double> initializeAndRun(final String rp, final String gamepak, final IConsumer<UIElement> uiTicker) throws IOException {
        rootPath = rp;
        // initialize core resources

        schemas = new SDB();

        schemas.readFile(gamepak + "Schema.txt"); // This does a lot of IO, for one line.

        // initialize everything else that needs initializing, starting with ObjectDB

        if (odbBackend.equals("r48")) {
            objectDB = new ObjectDB(new R48ObjectBackend(rootPath + dataPath, dataExt, true));
        } else if (odbBackend.equals("ika")) {
            objectDB = new ObjectDB(new IkaObjectBackend(rootPath));
        } else if (odbBackend.equals("lcf2000")) {
            objectDB = new ObjectDB(new R2kObjectBackend(rootPath));
        } else {
            throw new IOException("Unknown ODB backend " + odbBackend);
        }

        if (sysBackend.equals("null")) {
            system = new NullSystem();
        } else if (sysBackend.equals("RXP")) {
            system = new RXPSystem();
        } else if (sysBackend.equals("RVXA")) {
            system = new RVXASystem();
        } else if (sysBackend.equals("Ika")) {
            system = new IkaSystem();
        } else if (sysBackend.equals("R2k")) {
            system = new R2kSystem();
        } else {
            throw new IOException("Unknown MapSystem backend " + sysBackend);
        }

        // Final internal consistency checks and reading in dictionaries from target
        //  before starting the UI, which can cause external consistency checks
        //  (...and potentially cause havoc in the process)

        schemas.startupSanitizeDictionaries(); // in case an object using dictionaries has to be created to use dictionaries
        schemas.updateDictionaries(null);
        schemas.confirmAllExpectationsMet();

        // initialize UI
        final UIWindowView rootView = new UIWindowView() {
            @Override
            public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
                Coco.run(igd);
                super.updateAndRender(ox, oy, deltaTime, selected, igd);
            }
        };
        rootView.windowTextHeight = FontSizes.windowFrameHeight;
        windowMaker = rootView;
        rootView.setBounds(new Rect(0, 0, 800, 600));

        // Set up a default stuffRenderer for things to use.
        stuffRendererIndependent = system.rendererFromMap(null);

        rebuildInnerUI(gamepak, rootView, uiTicker);

        // everything ready, start main window
        uiTicker.accept(rootView);

        return new IConsumer<Double>() {
            @Override
            public void accept(Double deltaTime) {
                // Why throw the full format syntax parser on this? Consistency, plus I can extend this format further if need be.

                uiStatusLabel.Text = FormatSyntax.formatExtended(TXDB.get("#A modified. Clipboard: #B"), new RubyIO().setFX(objectDB.modifiedObjects.size()), (theClipboard == null) ? new RubyIO().setNull() : theClipboard);
                if (mapContext != null) {
                    String mapId = mapContext.getCurrentMap();
                    RubyIO map = null;
                    if (mapId != null)
                        map = objectDB.getObject(mapId);
                    schemas.updateDictionaries(map);
                } else {
                    schemas.updateDictionaries(null);
                }

                LinkedList<Runnable> runs = new LinkedList<Runnable>(pendingRunnables);
                pendingRunnables.clear();
                for (Runnable r : runs)
                    r.run();
            }
        };
    }

    private static UITabPane initializeTabs(final String gamepak, final UIWindowView rootView, final IConsumer<UIElement> uiTicker) {
        LinkedList<String> tabNames = new LinkedList<String>();
        LinkedList<UIElement> tabElems = new LinkedList<UIElement>();

        LinkedList<IToolset> toolsets = new LinkedList<IToolset>();
        // Until a future time, this is hard-coded as the classname of a map being created via MapInfos.
        // Probably simple enough to create a special alias, but meh.
        if (AppMain.schemas.hasSDBEntry("RPG::Map")) {
            MapToolset mapController = new MapToolset();
            // Really just restricts access to prevent a hax pileup
            mapContext = mapController.getContext();
            toolsets.add(mapController);
        } else {
            mapContext = null;
        }
        if (AppMain.schemas.hasSDBEntry("EventListEditor"))
            toolsets.add(new RMToolsToolset(gamepak));
        toolsets.add(new BasicToolset(rootView, uiTicker, new IConsumer<IConsumer<UIElement>>() {
            @Override
            public void accept(IConsumer<UIElement> uiElementIConsumer) {
                windowMaker = uiElementIConsumer;
            }
        }));

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
            // NOTE: This allows skipping out on actually generating tabs at the end, if you dare.
            for (int i = 0; i < tabContents.length; i++) {
                tabNames.add(tabs[i]);
                tabElems.add(tabContents[i]);
            }
        }

        return new UITabPane(tabNames.toArray(new String[0]), tabElems.toArray(new UIElement[0]), FontSizes.tabTextHeight);
    }

    private static void rebuildInnerUI(final String gamepak, final UIWindowView rootView, final IConsumer<UIElement> uiTicker) {
        uiStatusLabel = new UILabel(TXDB.get("Loading..."), FontSizes.statusBarTextHeight);

        UIAppendButton workspace = new UIAppendButton(TXDB.get("Save All Modified Files"), uiStatusLabel, new Runnable() {
            @Override
            public void run() {
                objectDB.ensureAllSaved();
            }
        }, FontSizes.statusBarTextHeight);
        workspace = new UIAppendButton(TXDB.get("Clipboard"), workspace, new Runnable() {
            @Override
            public void run() {
                if (theClipboard == null) {
                    launchDialog(TXDB.get("There is nothing in the clipboard."));
                } else {
                    windowMaker.accept(new UITest(theClipboard));
                }
            }
        }, FontSizes.statusBarTextHeight);
        workspace = new UIAppendButton(TXDB.get("Help"), workspace, new Runnable() {
            @Override
            public void run() {
                startHelp(0);
            }
        }, FontSizes.statusBarTextHeight);
        rootView.backing = new UINSVertLayout(workspace, initializeTabs(gamepak, rootView, uiTicker));
    }

    // Notably, you can't use this for non-roots because you'll end up bypassing ObjectDB.
    public static ISchemaHost launchSchema(String s, RubyIO rio, UIMapView context) {
        // Responsible for keeping listeners in place so nothing breaks.
        SchemaHostImpl watcher = new SchemaHostImpl(windowMaker, context);
        watcher.switchObject(new SchemaPath(schemas.getSDBEntry(s), rio, watcher));
        return watcher;
    }

    public static ISchemaHost launchNonRootSchema(RubyIO root, String rootSchema, RubyIO arrayIndex, RubyIO element, String elementSchema, String indexText, UIMapView context) {
        // produce a valid (and false) parent chain, that handles all required guarantees.
        ISchemaHost shi = launchSchema(rootSchema, root, context);
        SchemaPath sp = new SchemaPath(AppMain.schemas.getSDBEntry(rootSchema), root, shi);
        sp = sp.arrayHashIndex(arrayIndex, indexText);
        shi.switchObject(sp.newWindow(AppMain.schemas.getSDBEntry(elementSchema), element, shi));
        return shi;
    }

    public static void launchDialog(String s) {
        UIHelpSystem uhs = new UIHelpSystem();
        for (String st : s.split("\n"))
            uhs.page.add(new UIHelpSystem.HelpElement('.', st.split(" ")));
        UIScrollLayout svl = new UIScrollLayout(true) {
            @Override
            public String toString() {
                return TXDB.get("Information");
            }
        };
        svl.panels.add(uhs);
        uhs.setBounds(uhs.getBounds());
        int h = uhs.getBounds().height;
        if (h > 500)
            h = 500;
        svl.setBounds(new Rect(0, 0, uhs.getBounds().width, h));
        windowMaker.accept(svl);
    }

    public static void startHelp(Integer integer) {
        // exception to the rule
        UILabel uil = new UILabel("", FontSizes.helpPathHeight);
        final UIHelpSystem uis = new UIHelpSystem();
        final HelpSystemController hsc = new HelpSystemController(uil, null, uis);
        uis.onLinkClick = new IConsumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                hsc.loadPage(integer);
            }
        };
        final UIScrollLayout uus = new UIScrollLayout(true);
        uus.panels.add(uis);
        uus.setBounds(new Rect(0, 0, 612, 240));
        final UINSVertLayout topbar = new UINSVertLayout(new UIAppendButton(TXDB.get("Index"), uil, new Runnable() {
            @Override
            public void run() {
                hsc.loadPage(0);
            }
        }, FontSizes.helpPathHeight), uus) {
            @Override
            public String toString() {
                return TXDB.get("Help Window");
            }
        };
        hsc.onLoad = new Runnable() {
            @Override
            public void run() {
                uus.scrollbar.scrollPoint = 0;
                Rect b = topbar.getBounds();
                topbar.setBounds(new Rect(0, 0, 16, 16));
                topbar.setBounds(b);
            }
        };
        hsc.loadPage(integer);
        windowMaker.accept(topbar);
    }

    public static void shutdown() {
        windowMaker = null;
        pendingRunnables.clear();
        uiStatusLabel = null;
        nextMapTool = null;
        rootPath = null;
        dataPath = "";
        dataExt = "";
        odbBackend = "<you forgot to select a backend>";
        sysBackend = "null";
        objectDB = null;
        autoTiles = new ATDB[0];
        schemas = null;
        stuffRendererIndependent = null;
        system = null;
        if (mapContext != null)
            mapContext.freeOsbResources();
        mapContext = null;
        theClipboard = null;
        TXDB.flushNameDB();
        GaBIEn.hintFlushAllTheCaches();
    }
}
