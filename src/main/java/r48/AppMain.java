/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.GaBIEn;
import gabien.IDesktopPeripherals;
import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.*;
import gabienapp.Application;
import r48.dbs.*;
import r48.imagefx.ImageFXCache;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.map.systems.*;
import r48.maptools.UIMTBase;
import r48.schema.OpaqueSchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaHostImpl;
import r48.schema.util.SchemaPath;
import r48.toolsets.BasicToolset;
import r48.toolsets.IToolset;
import r48.toolsets.MapToolset;
import r48.toolsets.RMToolsToolset;
import r48.ui.*;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;

import java.io.*;
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
    // Where new windows go
    private static IConsumer<UIElement> trueWindowMaker, trueWindowMakerI;

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

    // Backend Services

    // The global context-independent stuffRenderer. *Only use outside of maps.*
    public static StuffRenderer stuffRendererIndependent;
    public static MapSystem system;

    // ONLY this class should refer to this (I think?)
    private static IMapContext mapContext;
    private static UIWindowView rootView;
    private static IConsumer<UIElement> insertTab, insertImmortalTab;

    /*
     * For lack of a better place, this is a description of how window management works in R48.
     * There are 3 places windows can be.
     * They can be on the UIWindowView (rootViewWM/rootViewWMI), on the UITabPane (insertTab/insertImmortalTab),
     *  or 'outside' (See 'new BasicToolset' below).
     * If a window is removed it's one of:
     *  a self-destruct, or the user closed it.
     * For the first two places, these are handled by a callback in the host and in the close icon.
     * For the third place, the element gets wrapped.
     */
    // NOTE: This is never cleaned up and does not carry baggage
    private static IConsumer<UIElement> userWindowMaker = new IConsumer<UIElement>() {
        @Override
        public void accept(UIElement uiElement) {
            trueWindowMaker.accept(uiElement);
        }
    };

    // NOTE: These two are never cleaned up and do not carry baggage
    private static IConsumer<UIElement> rootViewWM = new IConsumer<UIElement>() {
        @Override
        public void accept(final UIElement uiElement) {
            rootView.accept(new UIWindowView.WVWindow(uiElement, new UIWindowView.IWVWindowIcon[] {
                    new UIWindowView.IWVWindowIcon() {
                        @Override
                        public void draw(IGrDriver igd, int x, int y, int size) {
                            Art.drawSymbol(igd, Art.Symbol.XRed, x, y, size, false, false);
                        }

                        @Override
                        public void click() {
                            rootView.removeByUIE(uiElement);
                            // This will be seen as a tab transfer without explicit force
                            if (uiElement instanceof IWindowElement)
                                ((IWindowElement) uiElement).windowClosing();
                        }
                    },
                    new UIWindowView.IWVWindowIcon() {
                        @Override
                        public void draw(IGrDriver igd, int x, int y, int size) {
                            Art.tabWindowIcon(igd, x, y, size);
                        }

                        @Override
                        public void click() {
                            rootView.removeByUIE(uiElement);
                            insertTab.accept(uiElement);
                        }
                    }
            }));
        }
    };
    private static IConsumer<UIElement> rootViewWMI = new IConsumer<UIElement>() {
        @Override
        public void accept(final UIElement uiElement) {
            rootView.accept(new UIWindowView.WVWindow(uiElement, new UIWindowView.IWVWindowIcon[] {
                    new UIWindowView.IWVWindowIcon() {
                        @Override
                        public void draw(IGrDriver igd, int x, int y, int size) {
                            Art.tabWindowIcon(igd, x, y, size);
                        }

                        @Override
                        public void click() {
                            rootView.removeByUIE(uiElement);
                            insertImmortalTab.accept(uiElement);
                        }
                    }
            }));
        }
    };

    // State for in-system copy/paste
    public static RubyIO theClipboard = null;

    // All active schema hosts
    private static LinkedList<ISchemaHost> activeHosts;
    // All magical bindings in use
    public static WeakHashMap<RubyIO, HashMap<IMagicalBinder, WeakReference<RubyIO>>> magicalBindingCache;
    // All magical binders in use
    public static HashMap<String, IMagicalBinder> magicalBinderCache;

    // Used to scale certain windows.
    public static int mainWindowWidth;
    public static int mainWindowHeight;

    // Try to ensure these directories exist.
    public static LinkedList<String> recommendedDirs;

    // Manages fullscreeniness.
    public static Runnable toggleFullscreen;

    // Image cache
    public static ImageFXCache imageFXCache = null;

    public static IConsumer<Double> initializeAndRun(final String rp, final String gamepak, final WindowCreatingUIElementConsumer uiTicker) throws IOException {
        rootPath = rp;
        // initialize core resources

        recommendedDirs = new LinkedList<String>();
        schemas = new SDB();
        magicalBindingCache = new WeakHashMap<RubyIO, HashMap<IMagicalBinder, WeakReference<RubyIO>>>();
        magicalBinderCache = new HashMap<String, IMagicalBinder>();

        schemas.readFile(gamepak + "Schema.txt"); // This does a lot of IO, for one line.

        // initialize everything else that needs initializing, starting with ObjectDB

        objectDB = new ObjectDB(IObjectBackend.Factory.create(odbBackend, rootPath, dataPath, dataExt));

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
        } else if (sysBackend.equals("CSO")) {
            system = new CSOSystem();
        } else {
            throw new IOException("Unknown MapSystem backend " + sysBackend);
        }

        // Final internal consistency checks and reading in dictionaries from target
        //  before starting the UI, which can cause external consistency checks
        //  (...and potentially cause havoc in the process)

        schemas.startupSanitizeDictionaries(); // in case an object using dictionaries has to be created to use dictionaries
        schemas.updateDictionaries(null);
        schemas.confirmAllExpectationsMet();

        // Initialize imageFX before doing anything graphical
        imageFXCache = new ImageFXCache();

        activeHosts = new LinkedList<ISchemaHost>();

        // initialize UI
        rootView = new UIWindowView() {
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
                if (peripherals instanceof IDesktopPeripherals)
                    Coco.run((IDesktopPeripherals) peripherals);
                super.update(deltaTime, selected, peripherals);
            }

            @Override
            public void render(IGrDriver igd) {
                Size r = getSize();
                mainWindowWidth = r.width;
                mainWindowHeight = r.height;
                super.render(igd);
            }

            @Override
            public void handleClosedUserWindow(WVWindow wvWindow, boolean selfDestruct) {
                if (selfDestruct)
                    if (wvWindow.contents instanceof IWindowElement)
                        ((IWindowElement) wvWindow.contents).windowClosing();
            }
        };
        rootView.windowTextHeight = FontSizes.windowFrameHeight;
        rootView.sizerVisual = rootView.windowTextHeight / 2;
        rootView.sizerActual = rootView.windowTextHeight;

        trueWindowMaker = rootViewWM;
        trueWindowMakerI = rootViewWMI;

        mainWindowWidth = FontSizes.scaleGuess(800);
        mainWindowHeight = FontSizes.scaleGuess(600);
        rootView.setForcedBounds(null, new Rect(0, 0, mainWindowWidth, mainWindowHeight));

        // Set up a default stuffRenderer for things to use.
        stuffRendererIndependent = system.rendererFromTso(null);

        final UILabel uiStatusLabel = rebuildInnerUI(gamepak, uiTicker);

        // start possible recommended directory nagger
        final LinkedList<String> createDirs = new LinkedList<String>();
        for (String s : recommendedDirs)
            if (!GaBIEn.dirExists(PathUtils.autoDetectWindows(rootPath + s)))
                createDirs.add(s);

        // Only trigger create directories prompt if the database is *clearly* missing objects.
        // Do not do so otherwise (see: OneShot)
        if (objectDB.modifiedObjects.size() > 0) {
            if (createDirs.size() > 0) {
                rootViewWM.accept(new UIAutoclosingPopupMenu(new String[] {
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

        // everything ready, start main window
        toggleFullscreen = new Runnable() {
            boolean weAreFullscreen = true;
            Rect preFullscreenRect = null;
            @Override
            public void run() {
                uiTicker.forceRemove(rootView);
                if (!weAreFullscreen) {
                    preFullscreenRect = rootView.getParentRelativeBounds();
                } else {
                    if (preFullscreenRect != null)
                        rootView.setForcedBounds(null, preFullscreenRect);
                }
                weAreFullscreen = !weAreFullscreen;
                uiTicker.accept(rootView, 1, weAreFullscreen);
            }
        };
        toggleFullscreen.run();

        return new IConsumer<Double>() {
            @Override
            public void accept(Double deltaTime) {
                // Why throw the full format syntax parser on this? Consistency, plus I can extend this format further if need be.

                uiStatusLabel.text = FormatSyntax.formatExtended(TXDB.get("#A modified. Clipboard: #B"), new RubyIO().setFX(objectDB.modifiedObjects.size()), (theClipboard == null) ? new RubyIO().setNull() : theClipboard);
                if (mapContext != null) {
                    String mapId = mapContext.getCurrentMapObject();
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

                LinkedList<ISchemaHost> newActive = new LinkedList<ISchemaHost>();
                for (ISchemaHost ac : activeHosts)
                    if (ac.isActive())
                        newActive.add(ac);
                activeHosts = newActive;
            }
        };
    }

    // This can only be done once now that rootView & the tab pane kind of share state.
    // For a proper UI reset, careful nuking is required.
    private static UITabPane initializeTabs(final String gamepak, final IConsumer<UIElement> uiTicker) {
        LinkedList<IToolset> toolsets = new LinkedList<IToolset>();
        if (system.enableMapSubsystem) {
            MapToolset mapController = new MapToolset(userWindowMaker);
            // Really just restricts access to prevent a hax pileup
            mapContext = mapController.getContext();
            toolsets.add(mapController);
        } else {
            mapContext = null;
        }
        if (system instanceof IRMMapSystem)
            toolsets.add(new RMToolsToolset(gamepak));
        toolsets.add(new BasicToolset(new IConsumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    // Real
                    trueWindowMaker = new IConsumer<UIElement>() {
                        @Override
                        public void accept(final UIElement uiElement) {
                            injectReal(uiElement, false);
                        }
                    };
                    trueWindowMakerI = new IConsumer<UIElement>() {
                        @Override
                        public void accept(final UIElement uiElement) {
                            injectReal(uiElement, true);
                        }
                    };
                } else {
                    // Virtual
                    trueWindowMaker = rootViewWM;
                    trueWindowMakerI = rootViewWMI;
                }
            }

            private void injectReal(final UIElement uiElement, final boolean b) {
                uiTicker.accept(new UIElement.UIProxy(uiElement, false) {
                    @Override
                    public void handleRootDisconnect() {
                        super.handleRootDisconnect();
                        release();
                        if (b) {
                            insertImmortalTab.accept(uiElement);
                        } else {
                            insertTab.accept(uiElement);
                        }
                    }
                });
            }
        }));
        //toolsets.add(new ImageEditToolset());

        final UITabPane utp = new UITabPane(FontSizes.tabTextHeight, true, true) {
            @Override
            public void handleClosedUserTab(UIWindowView.WVWindow wvWindow, boolean selfDestruct) {
                if (selfDestruct)
                    if (wvWindow.contents instanceof IWindowElement)
                        ((IWindowElement) wvWindow.contents).windowClosing();
            }
        };
        Runnable runVisFrame = new Runnable() {
            @Override
            public void run() {
                double keys = objectDB.objectMap.keySet().size();
                if (keys < 1) {
                    utp.visualizationOrange = 0.0d;
                } else {
                    utp.visualizationOrange = objectDB.modifiedObjects.size() / keys;
                }
                pendingRunnables.add(this);
            }
        };
        pendingRunnables.add(runVisFrame);
        insertImmortalTab = new IConsumer<UIElement>() {
            @Override
            public void accept(final UIElement uiElement) {
                utp.addTab(new UIWindowView.WVWindow(uiElement, new UIWindowView.IWVWindowIcon[] {
                        new UIWindowView.IWVWindowIcon() {
                            @Override
                            public void draw(IGrDriver igd, int x, int y, int size) {
                                Art.windowWindowIcon(igd, x, y, size);
                            }

                            @Override
                            public void click() {
                                utp.removeTab(uiElement);
                                Size r = rootView.getSize();
                                uiElement.setForcedBounds(null, new Rect(0, 0, r.width / 2, r.height / 2));
                                trueWindowMakerI.accept(uiElement);
                            }
                        }
                }));
                utp.selectTab(uiElement);
            }
        };

        UIElement firstTab = null;
        // Initialize toolsets.
        for (IToolset its : toolsets)
            for (UIElement uie : its.generateTabs(userWindowMaker)) {
                if (firstTab == null)
                    firstTab = uie;
                insertImmortalTab.accept(uie);
            }

        insertTab = new IConsumer<UIElement>() {
            @Override
            public void accept(final UIElement uiElement) {
                utp.addTab(new UIWindowView.WVWindow(uiElement, new UIWindowView.IWVWindowIcon[] {
                        new UIWindowView.IWVWindowIcon() {
                            @Override
                            public void draw(IGrDriver igd, int x, int y, int size) {
                                Art.drawSymbol(igd, Art.Symbol.XRed, x, y, size, false, false);
                            }

                            @Override
                            public void click() {
                                utp.removeTab(uiElement); // also does root disconnect
                                // This will be seen as a tab transfer without explicit force
                                if (uiElement instanceof IWindowElement)
                                    ((IWindowElement) uiElement).windowClosing();
                            }
                        },
                        new UIWindowView.IWVWindowIcon() {
                            @Override
                            public void draw(IGrDriver igd, int x, int y, int size) {
                                Art.windowWindowIcon(igd, x, y, size);
                            }

                            @Override
                            public void click() {
                                utp.removeTab(uiElement);
                                uiElement.setForcedBounds(null, new Rect(0, 0, mainWindowWidth / 2, mainWindowHeight / 2));
                                trueWindowMaker.accept(uiElement);
                            }
                        }
                }));
                utp.selectTab(uiElement);
            }
        };

        utp.selectTab(firstTab);
        return utp;
    }

    private static UILabel rebuildInnerUI(final String gamepak, final IConsumer<UIElement> uiTicker) {
        UILabel uiStatusLabel = new UILabel(TXDB.get("Loading..."), FontSizes.statusBarTextHeight);

        UIAppendButton workspace = new UIAppendButton(TXDB.get("Save All Modified Files"), uiStatusLabel, new Runnable() {
            @Override
            public void run() {
                objectDB.ensureAllSaved();
            }
        }, FontSizes.statusBarTextHeight);
        workspace = new UIAppendButton(TXDB.get("Clipboard"), workspace, new Runnable() {
            @Override
            public void run() {
                trueWindowMaker.accept(new UIAutoclosingPopupMenu(new String[] {
                        TXDB.get("Save Clipboard To 'clip.r48'"),
                        TXDB.get("Load Clipboard From 'clip.r48'"),
                        TXDB.get("Inspect Clipboard"),
                        TXDB.get("Execute Lua from 'script.lua' onto clipboard")
                }, new Runnable[] {
                        new Runnable() {
                            @Override
                            public void run() {
                                if (theClipboard == null) {
                                    launchDialog(TXDB.get("There is nothing in the clipboard."));
                                } else {
                                    AdHocSaveLoad.save("clip", theClipboard);
                                    launchDialog(TXDB.get("The clipboard was saved."));
                                }
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                RubyIO newClip = AdHocSaveLoad.load("clip");
                                if (newClip == null) {
                                    launchDialog(TXDB.get("The clipboard file is invalid or does not exist."));
                                } else {
                                    theClipboard = newClip;
                                    launchDialog(TXDB.get("The clipboard file was loaded."));
                                }
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                if (theClipboard == null) {
                                    launchDialog(TXDB.get("There is nothing in the clipboard."));
                                } else {
                                    trueWindowMaker.accept(new UITest(theClipboard));
                                }
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                if (theClipboard == null) {
                                    launchDialog(TXDB.get("There is nothing in the clipboard."));
                                } else {
                                    if (!LuaInterface.luaAvailable()) {
                                        launchDialog(TXDB.get("Lua isn't installed, so can't use it."));
                                    } else {
                                        try {
                                            BufferedReader br = new BufferedReader(new InputStreamReader(GaBIEn.getInFile("script.lua"), "UTF-8"));
                                            String t = "";
                                            while (br.ready())
                                                t += br.readLine() + "\r\n";
                                            br.close();
                                            RubyIO rio = LuaInterface.runLuaCall(theClipboard, t);
                                            if (rio == null) {
                                                String s = "";
                                                try {
                                                    if (LuaInterface.lastError != null)
                                                        s = "\n" + new String(LuaInterface.lastError, "UTF-8");
                                                } catch (Exception e2) {
                                                    // output clearly unavailable
                                                }
                                                launchDialog(TXDB.get("Lua error, or took > 10 seconds. Output:") + s);
                                            } else {
                                                theClipboard = rio;
                                                launchDialog(TXDB.get("Successful - the clipboard was replaced."));
                                            }
                                        } catch (Exception e) {
                                            launchDialog(TXDB.get("An exception occurred? (R48-core files are stored in R48's current directory, not the root path.)"));
                                        }
                                    }
                                }
                            }
                        }
                }, FontSizes.menuTextHeight, FontSizes.menuScrollersize, true));
            }
        }, FontSizes.statusBarTextHeight);
        workspace = new UIAppendButton(TXDB.get("Help"), workspace, new Runnable() {
            @Override
            public void run() {
                startHelp(0);
            }
        }, FontSizes.statusBarTextHeight);
        rootView.backing = new UINSVertLayout(workspace, initializeTabs(gamepak, uiTicker));
        return uiStatusLabel;
    }

    // Notably, you can't use this for non-roots because you'll end up bypassing ObjectDB.
    public static ISchemaHost launchSchema(String s, RubyIO rio, UIMapView context) {
        // Responsible for keeping listeners in place so nothing breaks.
        SchemaHostImpl watcher = new SchemaHostImpl(userWindowMaker, context);
        watcher.switchObject(new SchemaPath(schemas.getSDBEntry(s), rio));
        return watcher;
    }

    public static ISchemaHost launchNonRootSchema(RubyIO root, String rootSchema, RubyIO arrayIndex, RubyIO element, String elementSchema, String indexText, UIMapView context) {
        // produce a valid (and false) parent chain, that handles all required guarantees.
        ISchemaHost shi = launchSchema(rootSchema, root, context);
        SchemaPath sp = new SchemaPath(AppMain.schemas.getSDBEntry(rootSchema), root);
        sp = sp.arrayHashIndex(arrayIndex, indexText);
        shi.switchObject(sp.newWindow(AppMain.schemas.getSDBEntry(elementSchema), element));
        return shi;
    }

    public static void launchDialog(String s) {
        UIHelpSystem uhs = new UIHelpSystem();
        for (String st : s.split("\n"))
            uhs.page.add(new UIHelpSystem.HelpElement('.', st.split(" ")));
        UIScrollLayout svl = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public String toString() {
                return TXDB.get("Information");
            }
        };
        svl.panelsAdd(uhs);

        svl.runLayout();
        int h = svl.getWantedSize().height;

        int limit = rootView.getSize().height - rootView.getWindowFrameHeight();
        limit *= 3;
        limit /= 4;
        if (h > limit)
            h = limit;
        svl.setForcedBounds(null, new Rect(0, 0, Math.min(mainWindowWidth, svl.getWantedSize().width), h));
        trueWindowMaker.accept(svl);
    }

    public static Runnable createLaunchConfirmation(final String s, final Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                UITextButton accept = new UITextButton(TXDB.get("Accept"), FontSizes.dialogWindowTextHeight, null);
                UITextButton cancel = new UITextButton(TXDB.get("Cancel"), FontSizes.dialogWindowTextHeight, null);
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
                mtb.setForcedBounds(null, new Rect(0, 0, (mainWindowWidth / 3) * 2, mainWindowHeight / 2));
                trueWindowMaker.accept(mtb);
            }
        };
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
        final UIScrollLayout uus = new UIScrollLayout(true, FontSizes.generalScrollersize);
        uus.panelsAdd(uis);
        Size rootSize = rootView.getSize();
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
        hsc.loadPage(integer);
        topbar.setForcedBounds(null, new Rect(0, 0, (rootSize.width / 3) * 2, rootSize.height / 2));
        trueWindowMaker.accept(topbar);
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
        Runnable deploy = new Runnable() {
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
                        "R2K/System.png", "System/System.png",
                        "R2K/templatetileset.png", "ChipSet/templatetileset.png",
                        "R2K/slime.png", "Monster/monster.png",
                };
                fileCopier(mkdirs, fileCopies);
                // Load map 1, save everything
                mapContext.loadMap("Map.1");
                objectDB.ensureAllSaved();
                launchDialog(TXDB.get("2k3 template synthesis complete."));
            }
        };
        trueWindowMaker.accept(new UIAutoclosingPopupMenu(new String[] {
                TXDB.get("You are creating a RPG Maker 2000/2003 LDB."),
                TXDB.get("Click here to automatically build skeleton project."),
                TXDB.get("Otherwise, close this inner window."),
        }, new Runnable[] {
                deploy,
                deploy,
                deploy
        }, FontSizes.menuTextHeight, FontSizes.menuScrollersize, true));
    }

    public static void csoNewMapMagic(String s, boolean st2) {
        if (!st2) {
            fileCopier(new String[] {
            }, new String[] {
                    "CSO/FG.png", AppMain.dataPath + s + ".png",
                    "CSO/BG.png", AppMain.dataPath + s + "BG.png",
                    "CSO/BGM.org", AppMain.dataPath + s + ".org",
                    "CSO/FG.pxa", AppMain.dataPath + s + ".pxa",
            });
        } else {
            mapContext.loadMap(s);
        }
    }

    public static void pleaseShutdown() {
        Application.shutdownAllAppMainWindows();
    }

    public static void shutdown() {
        trueWindowMaker = null;
        trueWindowMakerI = null;
        pendingRunnables.clear();
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
        rootView = null;
        insertImmortalTab = null;
        insertTab = null;
        theClipboard = null;
        imageFXCache = null;
        activeHosts = null;
        magicalBindingCache = null;
        magicalBinderCache = null;
        recommendedDirs = null;
        toggleFullscreen = null;
        TXDB.flushNameDB();
        GaBIEn.hintFlushAllTheCaches();
    }

    // Used for event selection boxes.
    public static boolean currentlyOpenInEditor(RubyIO r) {
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
        for (RubyIO rio : objectDB.modifiedObjects) {
            String s = objectDB.getIdByObject(rio);
            if (s != null)
                n.hashVal.put(new RubyIO().setString(s, true), rio);
        }
        if (!emergency) {
            RubyIO n2 = new RubyIO();
            n2.setString(TXDB.get("R48 Non-Emergency Backup File. This file can be used in place of r48.error.YOUR_SAVED_DATA.r48 in case of power failure or corrupting error. Assuming you actually save often it won't get too big - otherwise you need the reliability."), true);
            RubyIO n3 = AdHocSaveLoad.load("r48.pfail.YOUR_SAVED_DATA");
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
        AdHocSaveLoad.save(emergency ? "r48.error.YOUR_SAVED_DATA" : "r48.pfail.YOUR_SAVED_DATA", n);
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
        for (Map.Entry<RubyIO, RubyIO> rio : sysDump.hashVal.entrySet()) {
            String name = rio.getKey().decString();
            RubyIO root = objectDB.getObject(name);
            if (root == null) {
                root = new RubyIO();
                root.setNull();
                objectDB.newlyCreatedObjects.add(root);
                objectDB.objectMap.put(name, new WeakReference<RubyIO>(root));
            }
            root.setDeepClone(rio.getValue());
            objectDB.objectRootModified(root, new SchemaPath(new OpaqueSchemaElement(), root));
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
        if (system instanceof IRMMapSystem) {
            IRMMapSystem rms = (IRMMapSystem) system;
            for (IRMMapSystem.RMMapData rio : rms.getAllMaps())
                mainSet.add(rio.idName);
        }
        return new LinkedList<String>(mainSet);
    }

    public static UIFileBrowser setFBSize(UIFileBrowser uiFileBrowser) {
        uiFileBrowser.setForcedBounds(null, new Rect(0, 0, mainWindowWidth / 2, mainWindowHeight / 2));
        return uiFileBrowser;
    }
}
