/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumWriter;
import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.WindowCreatingUIElementConsumer;
import gabien.ui.dialogs.UIAutoclosingPopupMenu;
import gabien.ui.dialogs.UICredits;
import gabien.ui.elements.UIEmpty;
import gabien.ui.elements.UIIconButton;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIListLayout;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import gabien.uslx.vfs.FSBackend;
import r48.App;
import r48.IMapContext;
import r48.dbs.ObjectDB.ODBHandle;
import r48.dbs.ObjectRootHandle;
import r48.imagefx.ImageFXCache;
import r48.io.data.DMKey;
import r48.io.data.DMPath;
import r48.io.data.IRIO;
import r48.schema.EnumSchemaElement.Prefix;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaDynamicContext;
import r48.schema.util.UISchemaHostWindow;
import r48.schema.util.SchemaPath;
import r48.toolsets.BasicToolset;
import r48.toolsets.IToolset;
import r48.toolsets.MapToolset;
import r48.ui.Art;
import r48.ui.UIAppendButton;
import r48.ui.UIDynAppPrx;
import r48.ui.dialog.UITextPrompt;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;
import r48.ui.utilitybelt.ImageEditorController;
import r48.wm.IQuickStatusGetter;
import r48.wm.WindowManager;

/**
 * An attempt to move as much as possible out of static variables.
 * Created 26th February, 2023
 */
public class AppUI extends App.Svc {
    public WindowManager wm;
    // All active schema hosts
    private LinkedList<UISchemaHostWindow> activeHosts = new LinkedList<UISchemaHostWindow>();
    // All active image editors
    public LinkedList<ImageEditorController> imgContext;

    // Indicates if R48 is running on a mobile or mobile-like platform.
    // In this event, adjusts UI to be more touch-friendly.
    public final boolean isMobile;

    // Image cache
    public ImageFXCache imageFXCache;

    // This is the main map context. Expect this to randomly be null and try to avoid accessing it.
    public IMapContext mapContext;

    private UIIconButton saveButtonSym;
    private UIIconButton undoButtonSym;
    private UIIconButton redoButtonSym;

    public final Coco coco;

    /**
     * Symbol instances!
     */
    public final Art.Symbol.Instance[] symbolInstances;

    public AppUI(App app, boolean mobile) {
        super(app);
        Art.Symbol[] syms = Art.Symbol.values();
        symbolInstances = new Art.Symbol.Instance[syms.length];
        for (int i = 0; i < syms.length; i++)
            symbolInstances[i] = syms[i].instanceDirect(app);
        isMobile = mobile;
        coco = new Coco(app);
    }

    public void initialize(WindowCreatingUIElementConsumer uiTicker) {
        app.loadProgress.accept(T.u.init);

        GaBIEn.setBrowserDirectory(app.gameRoot.getAbsolutePath());

        // Initialize imageFX before doing anything graphical
        imageFXCache = new ImageFXCache();

        imgContext = new LinkedList<ImageEditorController>();

        // Set up a default stuffRenderer for things to use.
        app.stuffRendererIndependent = app.system.rendererFromTso(null);

        // initialize UI
        saveButtonSym = new UIIconButton(Art.Symbol.Save.i(app), app.f.tabTH, this::saveAllModified);
        final UIIconButton btnRevert = new UIIconButton(Art.Symbol.Back.i(app), app.f.tabTH, createLaunchConfirmation(T.u.revertWarn, () -> {
            AppMain.performSystemDump(app, false, "revert file");
            // Shutdown schema hosts
            for (UISchemaHostWindow ish : activeHosts)
                ish.shutdown();
            // We're prepared for revert, do the thing
            app.odb.revertEverything();
            // Map editor will have fixed itself because it watches the roots and does full reinits when anything even remotely changes
            // But do this as well
            app.sdb.kickAllDictionariesForMapChange();
        }));
        undoButtonSym = new UIIconButton(Art.Symbol.Undo.i(app), app.f.tabTH, () -> {
            if (app.timeMachine.canUndo())
                app.timeMachine.undo();
        });
        redoButtonSym = new UIIconButton(Art.Symbol.Redo.i(app), app.f.tabTH, () -> {
            if (app.timeMachine.canRedo())
                app.timeMachine.redo();
        });
        UIListLayout iconBar = new UIListLayout(false, saveButtonSym, new UIEmpty(app.f.scaleGuess(4), 0), btnRevert, new UIEmpty(app.f.scaleGuess(8), 0), undoButtonSym, new UIEmpty(app.f.scaleGuess(4), 0), redoButtonSym);
        wm = new WindowManager(app.ilg, coco, uiTicker, null, iconBar, new IQuickStatusGetter() {
            
            @Override
            public String[] getQuickStatus() {
                if (coco.helpDisplayMode == 1) {
                    return new String[] {
                            wm.getRootSize().toString(),
                            "Target: 464x127"
                    };
                } else if (coco.helpDisplayMode == 2) {
                    return new String[] {
                            "^ Tab Bar                                    Save Un/Redo",
                            "                                               Revert",
                            "",
                            "",
                            "",
                            "",
                            "                      Main Panel Area",
                    };
                }
                Runtime r = Runtime.getRuntime();
                String qs = ((r.totalMemory() - r.freeMemory()) / (1024 * 1024)) + "/" + (r.totalMemory() / (1024 * 1024)) + "M " + (r.maxMemory() / (1024 * 1024)) + "MX " + app.odb.objectMap.size() + "O " + "<" + app.timeMachine.undoSnapshots() + " " + app.timeMachine.redoSnapshots() + ">";
                int keyCount = app.odb.objectMap.keySet().size();
                String[] data = new String[keyCount + 1];
                data[0] = qs;
                int idx = 1;
                for (Map.Entry<String, WeakReference<ODBHandle>> s : app.odb.objectMap.entrySet()) {
                    ObjectRootHandle ilo = s.getValue().get();
                    if (ilo != null) {
                        boolean modified = app.odb.modifiedObjects.contains(ilo);
                        data[idx++] = s.getKey() + (modified ? "* " : " ") + app.odb.countModificationListeners(ilo) + "ML";
                    }
                }
                while (idx < data.length)
                    data[idx++] = "";
                return data;
            }
            
            @Override
            public float getOrange() {
                int modifiedObjectCount = app.odb.modifiedObjects.size();
                if (modifiedObjectCount == 1)
                    return 0.33f;
                if (modifiedObjectCount == 2)
                    return 0.66f;
                if (modifiedObjectCount >= 3)
                    return 1.0f;
                return 0.0f;
            }
        });

        initializeTabs();

        app.loadProgress.accept(T.u.init2);

        // start possible recommended directory nagger
        final LinkedList<FSBackend> createDirs = new LinkedList<FSBackend>();
        for (String s : app.engine.mkdirs) {
            FSBackend tgt = app.gameRoot.intoPath(s);
            if (!tgt.isDirectory())
                createDirs.add(tgt);
        }

        // Only trigger create directories prompt if the database is *clearly* missing objects.
        // Do not do so otherwise (see: OneShot)
        if (app.odb.modifiedObjects.size() > 0) {
            if (createDirs.size() > 0) {
                wm.createWindow(new UIAutoclosingPopupMenu(new String[] {
                        T.u.newDirs
                }, new Runnable[] {
                        () -> {
                            for (FSBackend st : createDirs)
                                st.mkdirs();
                            launchDoneDialog();
                        }
                }, app.f.menuTH, app.f.menuS, true));
            }
        }
    }

    public void finishInitialization() {
        wm.finishInitialization();
    }

    public void tick(double dT) {
        saveButtonSym.symbol = (hasModified() ? Art.Symbol.Save : Art.Symbol.SaveDisabled).i(app);
        undoButtonSym.symbol = (app.timeMachine.canUndo() ? Art.Symbol.Undo : Art.Symbol.UndoDisabled).i(app);
        redoButtonSym.symbol = (app.timeMachine.canRedo() ? Art.Symbol.Redo : Art.Symbol.RedoDisabled).i(app);
        if (mapContext != null) {
            String mapId = mapContext.getCurrentMapObject();
            ObjectRootHandle map = null;
            if (mapId != null)
                map = app.odb.getObject(mapId);
            app.sdb.updateDictionaries(map);
        } else {
            app.sdb.updateDictionaries(null);
        }

        app.odb.runPendingModifications();

        LinkedList<Runnable> runs = new LinkedList<>(app.uiPendingRunnables);
        app.uiPendingRunnables.clear();
        for (Runnable r : runs)
            r.run();

        LinkedList<UISchemaHostWindow> newActive = new LinkedList<>();
        for (UISchemaHostWindow ac : activeHosts)
            if (ac.windowOpen)
                newActive.add(ac);
        activeHosts = newActive;

        app.timeMachine.doCycle();
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

    public void confirmDeletion(boolean mobileOnly, IRIO irio, SchemaElement se, final @Nullable UIElement menuBasis, final Runnable runnable) {
        confirmDeletion(mobileOnly, app.format(irio, se, Prefix.NoPrefix), menuBasis, runnable);
    }

    public void confirmDeletion(boolean mobileOnly, String stuff, final @Nullable UIElement menuBasis, final Runnable runnable) {
        if (mobileOnly && !app.deletionButtonsNeedConfirmation) {
            runnable.run();
            return;
        }
        String text = T.u.confirmDeletion.r(stuff);
        confirm(text, menuBasis, runnable);
    }

    public void confirm(final String s, final @Nullable UIElement menuBasis, final Runnable runnable) {
        UITextButton accept = new UITextButton(T.u.confirm_accept, app.f.dialogWindowTH, null).centred();
        UITextButton cancel = new UITextButton(T.u.confirm_cancel, app.f.dialogWindowTH, null).centred();
        UIElement uie = new UISplitterLayout(new UILabel(s, app.f.dialogWindowTH),
                new UISplitterLayout(accept, cancel, false, 0.5d), true, 1d);
        final UIDynAppPrx mtb = UIDynAppPrx.wrap(app, uie);
        mtb.titleOverride = T.t.confirm;
        accept.onClick = () -> {
            runnable.run();
            mtb.selfClose = true;
        };
        cancel.onClick = () -> {
            mtb.selfClose = true;
        };
        app.ui.wm.adjustWindowSH(mtb);
        if (menuBasis != null) {
            app.ui.wm.createMenu(menuBasis, mtb);
        } else {
            app.ui.wm.createWindow(mtb);
        }
    }

    public Runnable createLaunchConfirmation(final String s, final Runnable runnable) {
        return () -> {
            UITextButton accept = new UITextButton(T.u.confirm_accept, app.f.dialogWindowTH, null).centred();
            UITextButton cancel = new UITextButton(T.u.confirm_cancel, app.f.dialogWindowTH, null).centred();
            UIElement uie = new UISplitterLayout(new UILabel(s, app.f.dialogWindowTH),
                    new UISplitterLayout(accept, cancel, false, 0.5d), true, 1d);
            final UIDynAppPrx mtb = UIDynAppPrx.wrap(app, uie);
            mtb.titleOverride = T.t.confirm;
            accept.onClick = () -> {
                runnable.run();
                mtb.selfClose = true;
            };
            cancel.onClick = () -> {
                mtb.selfClose = true;
            };
            app.ui.wm.createWindowSH(mtb);
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
        UILabel uil = new UILabel("", app.f.helpPathH);
        final UIHelpSystem uis = new UIHelpSystem(app.ilg);
        final HelpSystemController hsc = new HelpSystemController(uil, base, uis);
        uis.onLinkClick = (linkId) -> {
            if (linkId.equals("CREDITS")) {
                wm.createWindow(new UICredits(app.f.generalS, app.f.dialogWindowTH) {
                    @Override
                    public String toString() {
                        return T.g.bCredits;
                    }
                });
                return;
            }
            hsc.accept(linkId);
        };
        final UIScrollLayout uus = new UIScrollLayout(true, app.f.generalS, uis);
        Size rootSize = wm.getRootSize();
        final UISplitterLayout topbar = new UISplitterLayout(new UIAppendButton(T.u.helpIndex, uil, () -> {
            hsc.loadPage(0);
        }, app.f.helpPathH), uus, true, 0) {
            @Override
            public String toString() {
                return T.u.helpTitle;
            }
        };
        hsc.onLoad = () -> {
            uus.scrollbar.scrollPoint = 0;
        };
        topbar.setForcedBounds(null, new Rect(0, 0, (rootSize.width / 3) * 2, rootSize.height / 2));
        wm.createWindow(topbar, "HelpWindow");
        hsc.accept(link);
    }

    public void schemaHostImplRegister(UISchemaHostWindow shi) {
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
        UILabel ul = new UILabel(s, app.f.textDialogDescTH);
        UIScrollLayout svl = new UIScrollLayout(true, app.f.generalS, ul) {
            @Override
            public String toString() {
                return title;
            }
        };
        wm.createWindowSH(svl);
    }

    public void launchDoneDialog() {
        launchDialog(T.u.done);
    }

    public void launchPrompt(String text, Consumer<String> consumer) {
        wm.createWindow(new UITextPrompt(app, text, consumer));
    }

    // Notably, you can't use this for non-roots because you'll end up bypassing ObjectDB.
    public ISchemaHost launchSchema(@NonNull ObjectRootHandle rio, @Nullable SchemaDynamicContext context) {
        return launchSchema(rio.rootSchema, rio, context);
    }

    // Notably, you can't use this for non-roots because you'll end up bypassing ObjectDB.
    public ISchemaHost launchSchema(SchemaElement s, @NonNull ObjectRootHandle rio, @Nullable SchemaDynamicContext context) {
        // Responsible for keeping listeners in place so nothing breaks.
        UISchemaHostWindow watcher = new UISchemaHostWindow(app, context);
        watcher.pushObject(new SchemaPath(s, rio));
        return watcher;
    }

    /**
     * Launches a disconnected schema pointing at a target object.
     * Try to only use this when absolutely necessary.
     */
    public ISchemaHost launchDisconnectedSchema(@NonNull ObjectRootHandle root, DMKey arrayIndex, IRIO element, SchemaElement elementSchema, String indexText, SchemaDynamicContext context) {
        // produce a valid (and false) parent chain, that handles all required guarantees.
        ISchemaHost shi = launchSchema(root, context);
        SchemaPath sp = new SchemaPath(root);
        sp = sp.arrayHashIndex(arrayIndex, indexText);
        shi.pushObject(sp.newWindow(elementSchema, element));
        return shi;
    }

    /**
     * Launches a schema by starting with a root/path pair.
     * Or tries, anyway.
     */
    public @Nullable ISchemaHost launchSchemaTrace(@NonNull ObjectRootHandle root, @Nullable SchemaDynamicContext context, @NonNull DMPath goal) {
        SchemaPath pathRoot = new SchemaPath(root);
        SchemaPath res = pathRoot.tracePathRoute(goal);
        if (res == null) {
            launchDialog(T.u.schemaTraceFailure);
            return null;
        }
        UISchemaHostWindow watcher = new UISchemaHostWindow(app, context);
        watcher.pushPathTree(res);
        return watcher;
    }

    public void copyUITree() {
        StringWriter sw = new StringWriter();
        DatumWriter dw = new DatumWriter(sw);
        app.ui.wm.debugDumpUITree(dw);
        GaBIEn.clipboard.copyText(sw.toString());
    }
}
