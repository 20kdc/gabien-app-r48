/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import r48.App;
import r48.IMapContext;
import r48.imagefx.ImageFXCache;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.map.UIMapView;
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
import r48.ui.dialog.UITextPrompt;
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

    // Indicates if R48 is running on a mobile or mobile-like platform.
    // In this event, adjusts UI to be more touch-friendly.
    public final boolean isMobile;

    // Image cache
    public ImageFXCache imageFXCache;

    // This is the main map context. Expect this to randomly be null and try to avoid accessing it.
    public IMapContext mapContext;

    private UISymbolButton saveButtonSym;

    public AppUI(App app, boolean mobile) {
        super(app);
        isMobile = mobile;
    }

    public void initialize(WindowCreatingUIElementConsumer uiTicker) {
        app.loadProgress.accept(T.u.init);

        GaBIEn.setBrowserDirectory(app.rootPath);

        // Initialize imageFX before doing anything graphical
        imageFXCache = new ImageFXCache();

        imgContext = new LinkedList<ImageEditorController>();

        // Set up a default stuffRenderer for things to use.
        app.stuffRendererIndependent = app.system.rendererFromTso(null);

        // initialize UI
        saveButtonSym = new UISymbolButton(Art.Symbol.Save, app.f.tabTextHeight, new Runnable() {
            @Override
            public void run() {
                saveAllModified();
            }
        });
        final UISymbolButton sym2 = new UISymbolButton(Art.Symbol.Back, app.f.tabTextHeight, createLaunchConfirmation(T.u.revertWarn, new Runnable() {
            @Override
            public void run() {
                AppMain.performSystemDump(app, false, "revert file");
                // Shutdown schema hosts
                for (ISchemaHost ish : activeHosts)
                    ish.shutdown();
                // We're prepared for revert, do the thing
                app.odb.revertEverything();
                // Map editor will have fixed itself because it watches the roots and does full reinits when anything even remotely changes
                // But do this as well
                app.sdb.kickAllDictionariesForMapChange();
            }
        }));
        UISplitterLayout usl = new UISplitterLayout(saveButtonSym, sym2, false, 0.5);
        wm = new WindowManager(app, uiTicker, null, usl);

        initializeTabs();

        app.loadProgress.accept(T.u.init2);

        // start possible recommended directory nagger
        final LinkedList<String> createDirs = new LinkedList<String>();
        for (String s : app.sdb.recommendedDirs)
            if (!GaBIEn.dirExists(PathUtils.autoDetectWindows(app.rootPath + s)))
                createDirs.add(s);

        // Only trigger create directories prompt if the database is *clearly* missing objects.
        // Do not do so otherwise (see: OneShot)
        if (app.odb.modifiedObjects.size() > 0) {
            if (createDirs.size() > 0) {
                wm.createWindow(new UIAutoclosingPopupMenu(new String[] {
                        T.u.newDirs
                }, new Runnable[] {
                        new Runnable() {
                            @Override
                            public void run() {
                                for (String st : createDirs)
                                    GaBIEn.makeDirectories(PathUtils.autoDetectWindows(app.rootPath + st));
                                launchDoneDialog();
                            }
                        }
                }, app.f.menuTextHeight, app.f.menuScrollersize, true));
            }
        }
    }

    public void finishInitialization() {
        wm.finishInitialization();
    }

    public void tick(double dT) {
        saveButtonSym.symbol = hasModified() ? Art.Symbol.Save : Art.Symbol.SaveDisabled;
        if (mapContext != null) {
            String mapId = mapContext.getCurrentMapObject();
            IObjectBackend.ILoadedObject map = null;
            if (mapId != null)
                map = app.odb.getObject(mapId);
            app.sdb.updateDictionaries(map);
        } else {
            app.sdb.updateDictionaries(null);
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

    private void initializeTabs() {
        LinkedList<IToolset> toolsets = new LinkedList<IToolset>();

        toolsets.add(new BasicToolset(app));

        if (app.system.enableMapSubsystem) {
            app.loadProgress.accept(T.u.initMapScan);
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
            app.loadProgress.accept(T.u.initTab.r(its.toString()));
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
                UITextButton accept = new UITextButton(T.u.confirm_accept, app.f.dialogWindowTextHeight, null).centred();
                UITextButton cancel = new UITextButton(T.u.confirm_cancel, app.f.dialogWindowTextHeight, null).centred();
                UIElement uie = new UISplitterLayout(new UILabel(s, app.f.dialogWindowTextHeight),
                        new UISplitterLayout(accept, cancel, false, 0.5d), true, 1d);
                final UIMTBase mtb = UIMTBase.wrap(null, uie);
                mtb.titleOverride = T.t.confirm;
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
        UILabel uil = new UILabel("", app.f.helpPathHeight);
        final UIHelpSystem uis = new UIHelpSystem(app.ilg);
        final HelpSystemController hsc = new HelpSystemController(uil, base, uis);
        uis.onLinkClick = hsc;
        final UIScrollLayout uus = new UIScrollLayout(true, app.f.generalScrollersize);
        uus.panelsAdd(uis);
        Size rootSize = wm.getRootSize();
        final UINSVertLayout topbar = new UINSVertLayout(new UIAppendButton(T.u.helpIndex, uil, new Runnable() {
            @Override
            public void run() {
                hsc.loadPage(0);
            }
        }, app.f.helpPathHeight), uus) {
            @Override
            public String toString() {
                return T.u.helpTitle;
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

    public void performFullImageFlush() {
        if (mapContext != null)
            mapContext.performCacheFlush();
    }

    public void launchDialog(String s, Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        launchDialog(T.u.contextError.r(s, sw.toString()), T.t.error);
    }

    public void launchDialog(String s) {
        launchDialog(s, T.t.info);
    }

    public void launchDialog(Throwable e) {
        launchDialog(T.t.error, e);
    }

    public void launchDialog(String s, String title) {
        UILabel ul = new UILabel(s, app.f.textDialogDescTextHeight);
        UIScrollLayout svl = new UIScrollLayout(true, app.f.generalScrollersize) {
            @Override
            public String toString() {
                return title;
            }
        };
        svl.panelsAdd(ul);
        wm.createWindowSH(svl);
    }

    public void launchDoneDialog() {
        launchDialog(T.u.done);
    }

    public void launchPrompt(String text, IConsumer<String> consumer) {
        wm.createWindow(new UITextPrompt(app, text, consumer));
    }

    // Notably, you can't use this for non-roots because you'll end up bypassing ObjectDB.
    public ISchemaHost launchSchema(String s, IObjectBackend.ILoadedObject rio, UIMapView context) {
        // Responsible for keeping listeners in place so nothing breaks.
        SchemaHostImpl watcher = new SchemaHostImpl(app, context);
        watcher.pushObject(new SchemaPath(app.sdb.getSDBEntry(s), rio));
        return watcher;
    }

    public ISchemaHost launchNonRootSchema(IObjectBackend.ILoadedObject root, String rootSchema, IRIO arrayIndex, IRIO element, String elementSchema, String indexText, UIMapView context) {
        // produce a valid (and false) parent chain, that handles all required guarantees.
        ISchemaHost shi = launchSchema(rootSchema, root, context);
        SchemaPath sp = new SchemaPath(app.sdb.getSDBEntry(rootSchema), root);
        sp = sp.arrayHashIndex(arrayIndex, indexText);
        shi.pushObject(sp.newWindow(app.sdb.getSDBEntry(elementSchema), element));
        return shi;
    }
}
