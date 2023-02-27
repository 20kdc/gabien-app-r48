/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48;

import java.util.LinkedList;

import gabien.GaBIEn;
import gabien.ui.Rect;
import gabien.ui.Size;
import gabien.ui.UIAutoclosingPopupMenu;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import gabien.ui.WindowCreatingUIElementConsumer;
import gabien.uslx.append.IConsumer;
import gabien.uslx.append.ISupplier;
import gabienapp.UIFancyInit;
import r48.dbs.TXDB;
import r48.imagefx.ImageFXCache;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.maptools.UIMTBase;
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
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;
import r48.ui.utilitybelt.ImageEditorController;
import r48.wm.WindowManager;

/**
 * An attempt to move as much as possible out of static variables.
 * Created 26th February, 2023
 */
public class AppUI extends App.Svc {
    public WindowManager wm;
    // All active schema hosts
    private LinkedList<ISchemaHost> activeHosts = new LinkedList<ISchemaHost>();
    // All active image editors
    public LinkedList<ImageEditorController> imgContext;

    // Image cache
    public ImageFXCache imageFXCache;

    // This is the main map context. Expect this to randomly be null and try to avoid accessing it.
    public IMapContext mapContext;

    public AppUI(App app) {
        super(app);
    }

    public ISupplier<IConsumer<Double>> initialize(WindowCreatingUIElementConsumer uiTicker) {
        GaBIEn.setBrowserDirectory(AppMain.rootPath);

        // Initialize imageFX before doing anything graphical
        imageFXCache = new ImageFXCache();

        imgContext = new LinkedList<ImageEditorController>();

        // Set up a default stuffRenderer for things to use.
        AppMain.stuffRendererIndependent = app.system.rendererFromTso(null);

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
                AppMain.performSystemDump(false, "revert file");
                // Shutdown schema hosts
                for (ISchemaHost ish : activeHosts)
                    ish.shutdown();
                // We're prepared for revert, do the thing
                app.odb.revertEverything();
                // Map editor will have fixed itself because it watches the roots and does full reinits when anything even remotely changes
                // But do this as well
                AppMain.schemas.kickAllDictionariesForMapChange();
            }
        }));
        UISplitterLayout usl = new UISplitterLayout(sym, sym2, false, 0.5);
        wm = new WindowManager(uiTicker, null, usl);

        initializeTabs();

        UIFancyInit.submitToConsoletron(TXDB.get("Finishing up initialization..."));

        // start possible recommended directory nagger
        final LinkedList<String> createDirs = new LinkedList<String>();
        for (String s : AppMain.schemas.recommendedDirs)
            if (!GaBIEn.dirExists(PathUtils.autoDetectWindows(AppMain.rootPath + s)))
                createDirs.add(s);

        // Only trigger create directories prompt if the database is *clearly* missing objects.
        // Do not do so otherwise (see: OneShot)
        if (app.odb.modifiedObjects.size() > 0) {
            if (createDirs.size() > 0) {
                wm.createWindow(new UIAutoclosingPopupMenu(new String[] {
                        TXDB.get("This appears to be newly created. Click to create directories.")
                }, new Runnable[] {
                        new Runnable() {
                            @Override
                            public void run() {
                                for (String st : createDirs)
                                    GaBIEn.makeDirectories(PathUtils.autoDetectWindows(AppMain.rootPath + st));
                                AppMain.launchDialog(TXDB.get("Done!"));
                            }
                        }
                }, FontSizes.menuTextHeight, FontSizes.menuScrollersize, true));
            }
        }

        return new ISupplier<IConsumer<Double>>() {
            @Override
            public IConsumer<Double> get() {
                wm.finishInitialization();
                return new IConsumer<Double>() {
                    @Override
                    public void accept(Double deltaTime) {
                        sym.symbol = hasModified() ? Art.Symbol.Save : Art.Symbol.SaveDisabled;
                        if (mapContext != null) {
                            String mapId = mapContext.getCurrentMapObject();
                            IObjectBackend.ILoadedObject map = null;
                            if (mapId != null)
                                map = app.odb.getObject(mapId);
                            AppMain.schemas.updateDictionaries(map);
                        } else {
                            AppMain.schemas.updateDictionaries(null);
                        }

                        app.odb.runPendingModifications();

                        LinkedList<Runnable> runs = new LinkedList<Runnable>(app.uiPendingRunnables);
                        app.uiPendingRunnables.clear();
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

    private void initializeTabs() {
        LinkedList<IToolset> toolsets = new LinkedList<IToolset>();

        toolsets.add(new BasicToolset(app));

        if (app.system.enableMapSubsystem) {
            UIFancyInit.submitToConsoletron(TXDB.get("Looking for maps and saves (this'll take a while)..."));
            MapToolset mapController = new MapToolset(app);
            // Really just restricts access to prevent a hax pileup
            mapContext = mapController.getContext();
            toolsets.add(mapController);
        } else {
            mapContext = null;
        }

        Runnable runVisFrame = new Runnable() {
            @Override
            public void run() {
                double keys = app.odb.objectMap.keySet().size();
                if (keys < 1) {
                    wm.setOrange(0.0d);
                } else {
                    wm.setOrange(app.odb.modifiedObjects.size() / keys);
                }
                app.uiPendingRunnables.add(this);
            }
        };
        app.uiPendingRunnables.add(runVisFrame);

        UIElement firstTab = null;
        // Initialize toolsets.
        for (IToolset its : toolsets) {
            UIFancyInit.submitToConsoletron(TXDB.get("Initializing tab...") + "\n" + its.toString());
            for (UIElement uie : its.generateTabs()) {
                if (firstTab == null)
                    firstTab = uie;
                wm.createWindow(uie, true, true);
            }
        }
        wm.selectFirstTab();
    }

    public void startImgedit() {
        // Registers & unregisters self
        app.ui.wm.createWindowSH(new ImageEditorController(app).rootView);
    }

    public Runnable createLaunchConfirmation(final String s, final Runnable runnable) {
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
                app.ui.wm.createWindowSH(mtb);
            }
        };
    }

    // Used for event selection boxes.
    public boolean currentlyOpenInEditor(IRIO r) {
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

    public void startHelp(String base, String link) {
        // exception to the rule
        UILabel uil = new UILabel("", FontSizes.helpPathHeight);
        final UIHelpSystem uis = new UIHelpSystem();
        final HelpSystemController hsc = new HelpSystemController(uil, base, uis);
        uis.onLinkClick = hsc;
        final UIScrollLayout uus = new UIScrollLayout(true, FontSizes.generalScrollersize);
        uus.panelsAdd(uis);
        Size rootSize = wm.getRootSize();
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
        wm.createWindow(topbar);
        hsc.accept(link);
    }

    public void schemaHostImplRegister(SchemaHostImpl shi) {
        activeHosts.add(shi);
    }

    public void saveAllModified() {
        app.odb.ensureAllSaved();
        for (ImageEditorController iec : imgContext)
            if (iec.imageModified())
                iec.save();
    }

    public boolean hasModified() {
        if (app.odb.modifiedObjects.size() > 0)
            return true;
        for (ImageEditorController iec : imgContext)
            if (iec.imageModified())
                return true;
        return false;
    }
}
