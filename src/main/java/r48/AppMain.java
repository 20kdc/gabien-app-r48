/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.GaBIEn;
import gabien.ui.*;
import gabienapp.Application;
import gabienapp.UIFancyInit;
import r48.dbs.ATDB;
import r48.dbs.ObjectDB;
import r48.dbs.SDB;
import r48.dbs.TXDB;
import r48.imagefx.ImageFXCache;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.map.systems.IDynobjMapSystem;
import r48.map.systems.MapSystem;
import r48.maptools.UIMTBase;
import r48.schema.OpaqueSchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.R2kSystemDefaultsInstallerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaHostImpl;
import r48.schema.util.SchemaPath;
import r48.toolsets.BasicToolset;
import r48.toolsets.IToolset;
import r48.toolsets.MapToolset;
import r48.ui.Art;
import r48.ui.UIAppendButton;
import r48.ui.UINSVertLayout;
import r48.ui.UISymbolButton;
import r48.ui.dialog.UIChoicesMenu;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;
import r48.ui.utilitybelt.ImageEditorController;
import r48.wm.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.*;

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
    public static WindowManager window;

    // Scheduled tasks
    public static HashSet<Runnable> pendingRunnables = new HashSet<Runnable>();

    //private static UILabel uiStatusLabel;

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
    public static HashMap<Integer, String> osSHESEDB;

    // Backend Services

    // The global context-independent stuffRenderer. *Only use outside of maps.*
    public static StuffRenderer stuffRendererIndependent;
    public static MapSystem system;

    // ONLY this class should refer to this (I think?). Can be null.
    private static IMapContext mapContext;

    // State for in-system copy/paste
    public static RubyIO theClipboard = null;

    // All active schema hosts
    private static LinkedList<ISchemaHost> activeHosts;
    // All active image editors
    public static LinkedList<ImageEditorController> imgContext;
    // All magical bindings in use
    public static WeakHashMap<IRIO, HashMap<IMagicalBinder, WeakReference<RubyIO>>> magicalBindingCache;

    // Image cache
    public static ImageFXCache imageFXCache = null;

    public static void initializeCore(final String rp, final String gamepak) {
        rootPath = rp;

        // initialize core resources

        schemas = new SDB();
        magicalBindingCache = new WeakHashMap<IRIO, HashMap<IMagicalBinder, WeakReference<RubyIO>>>();

        schemas.readFile(gamepak + "Schema.txt"); // This does a lot of IO, for one line.

        // initialize everything else that needs initializing, starting with ObjectDB

        objectDB = new ObjectDB(IObjectBackend.Factory.create(odbBackend, rootPath, dataPath, dataExt), new IConsumer<String>() {
            @Override
            public void accept(String s) {
                if (system != null)
                    system.saveHook(s);
            }
        });

        system = MapSystem.create(sysBackend);

        // Final internal consistency checks and reading in dictionaries from target
        //  before starting the UI, which can cause external consistency checks
        //  (...and potentially cause havoc in the process)

        schemas.startupSanitizeDictionaries(); // in case an object using dictionaries has to be created to use dictionaries
        UIFancyInit.submitToConsoletron(TXDB.get("Initializing dictionaries & creating objects..."));
        schemas.updateDictionaries(null);
        schemas.confirmAllExpectationsMet();
    }

    public static ISupplier<IConsumer<Double>> initializeUI(final WindowCreatingUIElementConsumer uiTicker) {
        GaBIEn.setBrowserDirectory(rootPath);

        // Initialize imageFX before doing anything graphical
        imageFXCache = new ImageFXCache();

        activeHosts = new LinkedList<ISchemaHost>();
        imgContext = new LinkedList<ImageEditorController>();

        // Set up a default stuffRenderer for things to use.
        stuffRendererIndependent = system.rendererFromTso(null);

        UIFancyInit.submitToConsoletron(TXDB.get("Initializing UI..."));

        // initialize UI
        final UISymbolButton sym = new UISymbolButton(Art.Symbol.Save, FontSizes.tabTextHeight, new Runnable() {
            @Override
            public void run() {
                saveAllModified();
            }
        });
        final UISymbolButton sym2 = new UISymbolButton(Art.Symbol.Back, FontSizes.tabTextHeight, createLaunchConfirmation(TXDB.get("Reverting changes will lose all unsaved work and will reset many windows."), new Runnable() {
            @Override
            public void run() {
                performSystemDump(false);
                // Shutdown schema hosts
                for (ISchemaHost ish : activeHosts) {
                    ish.shutdown();
                }
                // We're prepared for revert, do the thing
                objectDB.revertEverything();
                // Map editor will have fixed itself because it watches the roots and does full reinits when anything even remotely changes
                // But do this as well
                schemas.kickAllDictionariesForMapChange();
            }
        }));
        UISplitterLayout usl = new UISplitterLayout(sym, sym2, false, 0.5);
        window = new WindowManager(uiTicker, null, usl);

        initializeTabs();

        UIFancyInit.submitToConsoletron(TXDB.get("Finishing up initialization..."));

        // start possible recommended directory nagger
        final LinkedList<String> createDirs = new LinkedList<String>();
        for (String s : schemas.recommendedDirs)
            if (!GaBIEn.dirExists(PathUtils.autoDetectWindows(rootPath + s)))
                createDirs.add(s);

        // Only trigger create directories prompt if the database is *clearly* missing objects.
        // Do not do so otherwise (see: OneShot)
        if (objectDB.modifiedObjects.size() > 0) {
            if (createDirs.size() > 0) {
                window.createWindow(new UIAutoclosingPopupMenu(new String[] {
                        TXDB.get("This appears to be newly created. Click to create directories.")
                }, new Runnable[] {
                        new Runnable() {
                            @Override
                            public void run() {
                                for (String st : createDirs)
                                    GaBIEn.makeDirectories(PathUtils.autoDetectWindows(rootPath + st));
                                launchDialog(TXDB.get("Done!"));
                            }
                        }
                }, FontSizes.menuTextHeight, FontSizes.menuScrollersize, true));
            }
        }

        return new ISupplier<IConsumer<Double>>() {
            @Override
            public IConsumer<Double> get() {
                window.finishInitialization();
                return new IConsumer<Double>() {
                    @Override
                    public void accept(Double deltaTime) {
                        sym.symbol = hasModified() ? Art.Symbol.Save : Art.Symbol.SaveDisabled;
                        if (mapContext != null) {
                            String mapId = mapContext.getCurrentMapObject();
                            IObjectBackend.ILoadedObject map = null;
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

                        LinkedList<ISchemaHost> newActive = new LinkedList<ISchemaHost>();
                        for (ISchemaHost ac : activeHosts)
                            if (ac.isActive())
                                newActive.add(ac);
                        activeHosts = newActive;
                    }
                };
            }
        };
    }

    public static void performFullImageFlush() {
        if (mapContext != null)
            mapContext.performCacheFlush();
    }

    private static void initializeTabs() {
        LinkedList<IToolset> toolsets = new LinkedList<IToolset>();

        toolsets.add(new BasicToolset());

        if (system.enableMapSubsystem) {
            UIFancyInit.submitToConsoletron(TXDB.get("Looking for maps and saves (this'll take a while)..."));
            MapToolset mapController = new MapToolset();
            // Really just restricts access to prevent a hax pileup
            mapContext = mapController.getContext();
            toolsets.add(mapController);
        } else {
            mapContext = null;
        }

        Runnable runVisFrame = new Runnable() {
            @Override
            public void run() {
                double keys = objectDB.objectMap.keySet().size();
                if (keys < 1) {
                    window.setOrange(0.0d);
                } else {
                    window.setOrange(objectDB.modifiedObjects.size() / keys);
                }
                pendingRunnables.add(this);
            }
        };
        pendingRunnables.add(runVisFrame);

        UIElement firstTab = null;
        // Initialize toolsets.
        for (IToolset its : toolsets) {
            UIFancyInit.submitToConsoletron(TXDB.get("Initializing tab...") + "\n" + its.toString());
            for (UIElement uie : its.generateTabs()) {
                if (firstTab == null)
                    firstTab = uie;
                window.createWindow(uie, true, true);
            }
        }
        window.selectFirstTab();
    }

    // Notably, you can't use this for non-roots because you'll end up bypassing ObjectDB.
    public static ISchemaHost launchSchema(String s, IObjectBackend.ILoadedObject rio, UIMapView context) {
        // Responsible for keeping listeners in place so nothing breaks.
        SchemaHostImpl watcher = new SchemaHostImpl(context);
        watcher.pushObject(new SchemaPath(schemas.getSDBEntry(s), rio));
        return watcher;
    }

    public static ISchemaHost launchNonRootSchema(IObjectBackend.ILoadedObject root, String rootSchema, IRIO arrayIndex, IRIO element, String elementSchema, String indexText, UIMapView context) {
        // produce a valid (and false) parent chain, that handles all required guarantees.
        ISchemaHost shi = launchSchema(rootSchema, root, context);
        SchemaPath sp = new SchemaPath(AppMain.schemas.getSDBEntry(rootSchema), root);
        sp = sp.arrayHashIndex(arrayIndex, indexText);
        shi.pushObject(sp.newWindow(AppMain.schemas.getSDBEntry(elementSchema), element));
        return shi;
    }

    public static void launchDialog(String s) {
        UILabel ul = new UILabel(s, FontSizes.textDialogDescTextHeight);
        UIScrollLayout svl = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public String toString() {
                return TXDB.get("Information");
            }
        };
        svl.panelsAdd(ul);
        resizeDialogAndTruelaunch(svl);
    }

    public static Runnable createLaunchConfirmation(final String s, final Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                UITextButton accept = new UITextButton(TXDB.get("Accept"), FontSizes.dialogWindowTextHeight, null).centred();
                UITextButton cancel = new UITextButton(TXDB.get("Cancel"), FontSizes.dialogWindowTextHeight, null).centred();
                UIElement uie = new UISplitterLayout(new UILabel(s, FontSizes.dialogWindowTextHeight),
                        new UISplitterLayout(accept, cancel, false, 0.5d), true, 1d);
                final UIMTBase mtb = UIMTBase.wrap(null, uie);
                mtb.titleOverride = TXDB.get("Please confirm...");
                accept.onClick = new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                        mtb.selfClose = true;
                    }
                };
                cancel.onClick = new Runnable() {
                    @Override
                    public void run() {
                        mtb.selfClose = true;
                    }
                };
                resizeDialogAndTruelaunch(mtb);
            }
        };
    }

    private static void resizeDialogAndTruelaunch(UIElement mtb) {
        // This logic makes sense since we're trying to force a certain width but not a certain height.
        // It is NOT a bug in gabien-common so long as this code works (that is, the first call immediately prepares a correct wanted size).
        Size rootSize = window.getRootSize();
        Size validSize = new Size((rootSize.width * 3) / 4, (rootSize.height * 3) / 4);
        mtb.setForcedBounds(null, new Rect(validSize));
        Size recSize = mtb.getWantedSize();

        int w = Math.min(recSize.width, validSize.width);
        int h = Math.min(recSize.height, validSize.height);

        mtb.setForcedBounds(null, new Rect(0, 0, w, h));
        window.createWindow(mtb);
    }

    public static void startHelp(String base, String link) {
        // exception to the rule
        UILabel uil = new UILabel("", FontSizes.helpPathHeight);
        final UIHelpSystem uis = new UIHelpSystem();
        final HelpSystemController hsc = new HelpSystemController(uil, base, uis);
        uis.onLinkClick = hsc;
        final UIScrollLayout uus = new UIScrollLayout(true, FontSizes.generalScrollersize);
        uus.panelsAdd(uis);
        Size rootSize = window.getRootSize();
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
            }
        };
        topbar.setForcedBounds(null, new Rect(0, 0, (rootSize.width / 3) * 2, rootSize.height / 2));
        window.createWindow(topbar);
        hsc.accept(link);
    }

    public static void startImgedit() {
        // Registers & unregisters self
        resizeDialogAndTruelaunch(new ImageEditorController().rootView);
    }

    private static void fileCopier(String[] mkdirs, String[] fileCopies) {
        for (String s : mkdirs)
            GaBIEn.makeDirectories(PathUtils.autoDetectWindows(AppMain.rootPath + s));
        for (int i = 0; i < fileCopies.length; i += 2) {
            String src = fileCopies[i];
            String dst = fileCopies[i + 1];
            InputStream inp = GaBIEn.getResource(src);
            if (inp != null) {
                String tgt = PathUtils.autoDetectWindows(rootPath + dst);
                if (GaBIEn.fileOrDirExists(tgt)) {
                    System.err.println("Didn't write " + dst + " as it is already present as " + tgt + ".");
                    try {
                        inp.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                OutputStream oup = GaBIEn.getOutFile(tgt);
                if (oup != null) {
                    try {
                        byte[] b = new byte[2048];
                        while (inp.available() > 0)
                            oup.write(b, 0, inp.read(b));
                    } catch (IOException ioe) {

                    }
                    try {
                        oup.close();
                    } catch (IOException ioe) {

                    }
                }
                try {
                    inp.close();
                } catch (IOException ioe) {
                }
            } else {
                System.err.println("Didn't write " + dst + " as " + src + " missing.");
            }
        }
    }

    // R2kSystemDefaultsInstallerSchemaElement uses this to indirectly access several things a SchemaElement isn't allowed to access.
    public static void r2kProjectCreationHelperFunction() {
        final Runnable deploy2k = new Runnable() {
            @Override
            public void run() {
                // Perform all mkdirs
                String[] mkdirs = {
                        "Backdrop",
                        "Battle",
                        "Battle2",
                        "BattleCharSet",
                        "BattleWeapon",
                        "CharSet",
                        "ChipSet",
                        "FaceSet",
                        "Frame",
                        "GameOver",
                        "Monster",
                        "Music",
                        "Panorama",
                        "Picture",
                        "Sound",
                        "System",
                        "System2",
                        "Title"
                };
                String[] fileCopies = {
                        "R2K/char.png", "CharSet/char.png",
                        "R2K/backdrop.png", "Backdrop/backdrop.png",
                        "R2K/System.png", "System/System.png",
                        "R2K/templatetileset.png", "ChipSet/templatetileset.png",
                        "R2K/slime.png", "Monster/monster.png",
                        "R2K/templateconfig.ini", "RPG_RT.ini"
                };
                fileCopier(mkdirs, fileCopies);
                // Load map 1, save everything
                mapContext.loadMap("Map.1");
                objectDB.ensureAllSaved();
                launchDialog(TXDB.get("The synthesis was completed successfully."));
            }
        };
        resizeDialogAndTruelaunch(new UIChoicesMenu(TXDB.get("Would you like a basic template, and if so, compatible with RPG Maker 2000 or 2003? All assets used for this are part of R48, and thus CC0 (Public Domain)."), new String[] {
                TXDB.get("2000 Template"),
                TXDB.get("2003 Template"),
                TXDB.get("Do Nothing")
        }, new Runnable[] {
                deploy2k,
                new Runnable() {
                    @Override
                    public void run() {
                        IObjectBackend.ILoadedObject root = objectDB.getObject("RPG_RT.ldb");
                        R2kSystemDefaultsInstallerSchemaElement.upgradeDatabase(root.getObject());
                        objectDB.objectRootModified(root, new SchemaPath(new OpaqueSchemaElement(), root));
                        deploy2k.run();
                    }
                }, new Runnable() {
            @Override
            public void run() {

            }
        }
        }));
    }

    public static void pleaseShutdown() {
        Application.shutdownAllAppMainWindows();
    }

    public static void shutdownCore() {
        rootPath = null;
        dataPath = "";
        dataExt = "";
        odbBackend = "<you forgot to select a backend>";
        sysBackend = "null";
        objectDB = null;
        autoTiles = new ATDB[0];
        schemas = null;
        osSHESEDB = null;
        system = null;
    }

    public static void shutdown() {
        shutdownCore();
        pendingRunnables.clear();
        window = null;
        stuffRendererIndependent = null;
        if (mapContext != null)
            mapContext.freeOsbResources();
        mapContext = null;
        imgContext = null;
        theClipboard = null;
        imageFXCache = null;
        activeHosts = null;
        magicalBindingCache = null;
        TXDB.flushNameDB();
        GaBIEn.hintFlushAllTheCaches();
    }

    // Used for event selection boxes.
    public static boolean currentlyOpenInEditor(IRIO r) {
        for (ISchemaHost ish : activeHosts) {
            SchemaPath sp = ish.getCurrentObject();
            while (sp != null) {
                if (sp.targetElement == r)
                    return true;
                sp = sp.parent;
            }
        }
        return false;
    }

    public static void schemaHostImplRegister(SchemaHostImpl shi) {
        activeHosts.add(shi);
    }

    // Is this messy? Yes. Is it required? After someone lost some work to R48? YES IT DEFINITELY IS.
    // Later: I've reduced the amount of backups performed because it appears spikes were occurring all the time.
    public static void performSystemDump(boolean emergency) {
        RubyIO n = new RubyIO();
        n.setHash();
        for (IObjectBackend.ILoadedObject rio : objectDB.modifiedObjects) {
            String s = objectDB.getIdByObject(rio);
            if (s != null)
                n.addHashVal(new RubyIO().setString(s, true)).setDeepClone(rio.getObject());
        }
        if (!emergency) {
            RubyIO n2 = new RubyIO();
            n2.setString(TXDB.get("R48 Non-Emergency Backup File. This file can be used in place of r48.error.YOUR_SAVED_DATA.r48 in case of power failure or corrupting error. Assuming you actually save often it won't get too big - otherwise you need the reliability."), true);
            RubyIO n3 = AdHocSaveLoad.load("r48.revert.YOUR_SAVED_DATA");
            if (n3 != null) {
                // Unlink for disk space & memory usage reasons.
                // Already this is going to eat RAM.
                n3.rmIVar("@last");
                n2.addIVar("@last", n3);
            }
            n2.addIVar("@current", n);
            n = n2;
        }
        if (emergency)
            System.err.println("emergency dump is now actually occurring. Good luck.");
        AdHocSaveLoad.save(emergency ? "r48.error.YOUR_SAVED_DATA" : "r48.revert.YOUR_SAVED_DATA", n);
        if (emergency)
            System.err.println("emergency dump is complete.");
    }

    public static void reloadSystemDump() {
        RubyIO sysDump = AdHocSaveLoad.load("r48.error.YOUR_SAVED_DATA");
        if (sysDump == null) {
            AppMain.launchDialog(TXDB.get("The system dump was unloadable. It should be: r48.error.YOUR_SAVED_DATA.r48"));
            return;
        }
        RubyIO possibleActualDump = sysDump.getInstVarBySymbol("@current");
        if (possibleActualDump != null)
            sysDump = possibleActualDump;
        for (Map.Entry<IRIO, IRIO> rio : sysDump.hashVal.entrySet()) {
            String name = rio.getKey().decString();
            IObjectBackend.ILoadedObject root = objectDB.getObject(name);
            if (root != null) {
                root.getObject().setDeepClone(rio.getValue());
                objectDB.objectRootModified(root, new SchemaPath(new OpaqueSchemaElement(), root));
            }
        }
        if (possibleActualDump != null) {
            AppMain.launchDialog(TXDB.get("Power failure dump loaded."));
        } else {
            AppMain.launchDialog(TXDB.get("Error dump loaded."));
        }
    }

    // Attempts to ascertain all known objects
    public static LinkedList<String> getAllObjects() {
        // anything loaded gets added (this allows some bypass of the mechanism)
        HashSet<String> mainSet = new HashSet<String>(objectDB.objectMap.keySet());
        mainSet.addAll(schemas.listFileDefs());
        if (system instanceof IDynobjMapSystem) {
            IDynobjMapSystem idms = (IDynobjMapSystem) system;
            mainSet.addAll(idms.getDynamicObjects());
        }
        return new LinkedList<String>(mainSet);
    }

    public static void saveAllModified() {
        AppMain.objectDB.ensureAllSaved();
        for (ImageEditorController iec : AppMain.imgContext)
            if (iec.imageModified())
                iec.save();
    }

    public static boolean hasModified() {
        if (AppMain.objectDB.modifiedObjects.size() > 0)
            return true;
        for (ImageEditorController iec : AppMain.imgContext)
            if (iec.imageModified())
                return true;
        return false;
    }
}
