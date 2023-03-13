/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNull;

import gabien.GaBIEn;
import gabien.datum.DatumSrcLoc;
import gabien.ui.UIElement;
import gabien.ui.UIElement.UIPanel;
import gabien.ui.UIElement.UIProxy;
import gabien.uslx.append.IConsumer;
import r48.app.AppCore;
import r48.app.AppNewProject;
import r48.app.AppUI;
import r48.app.EngineDef;
import r48.app.IAppAsSeenByLauncher;
import r48.app.InterlaunchGlobals;
import r48.io.data.RORIO;
import r48.map.StuffRenderer;
import r48.minivm.MVMEnvR48;
import r48.minivm.fn.MVMR48AppLibraries;
import r48.tr.DynTrSlot;
import r48.tr.IDynTrProxy;
import r48.tr.pages.TrRoot;

/**
 * An attempt to move as much as possible out of static variables.
 * Created 26th February, 2023
 */
public final class App extends AppCore implements IAppAsSeenByLauncher, IDynTrProxy {
    public HashMap<Integer, String> osSHESEDB;
    // scheduled tasks for when UI is around, not in UI because it may not init (ever, even!)
    public HashSet<Runnable> uiPendingRunnables = new HashSet<Runnable>();
    // these init during UI init!
    public AppUI ui;
    public AppNewProject np;

    // The global context-independent stuffRenderer. *Only use outside of maps.*
    public StuffRenderer stuffRendererIndependent;

    // State for in-system copy/paste
    public RORIO theClipboard = null;
    public final Runnable applyConfigChange = () -> {
        c.applyUIGlobals();
    };

    // VM context
    public final MVMEnvR48 vmCtx;

    /**
     * Initialize App.
     * Warning: Occurs off main thread.
     */
    public App(InterlaunchGlobals ilg, EngineDef gp, String rp, String sip, IConsumer<String> loadProgress) {
        super(ilg, gp, rp, sip, loadProgress);
        vmCtx = new MVMEnvR48((str) -> {
            loadProgress.accept(t.g.loadingProgress.r(str));
        }, ilg.logTrIssues);
        MVMR48AppLibraries.add(vmCtx, this);
        vmCtx.include("vm/global", false);
    }

    @Override
    public DynTrSlot dynTrBase(DatumSrcLoc srcLoc, String id, Object text) {
        return vmCtx.dynTrBase(srcLoc, id, text);
    }

    public void performTranslatorDump(String fn) {
        vmCtx.dynTrDump(fn);
    }

    /**
     * Finishes initialization on main thread just before ticking begins.
     */
    public void finishInitOnMainThread() {
        ui.finishInitialization();
    }

    public void tick(double dT) {
        ui.tick(dT);
    }

    public void shutdown() {
        if (ui != null) {
            if (ui.mapContext != null)
                ui.mapContext.freeOsbResources();
            ui.mapContext = null;
        }
        GaBIEn.hintFlushAllTheCaches();
    }

    public static class Svc {
        public final @NonNull App app;
        /**
         * This is a special exception to the usual style rules.
         */
        public final @NonNull TrRoot T;
        public Svc(@NonNull App app) {
            this.app = app;
            T = app.t;
        }
    }

    public static class Prx extends UIProxy {
        public final @NonNull App app;
        public final @NonNull TrRoot T;
        public Prx(@NonNull App app) {
            this.app = app;
            T = app.t;
        }
    }

    public static abstract class Pan extends UIPanel {
        public final @NonNull App app;
        public final @NonNull TrRoot T;
        public Pan(@NonNull App app) {
            this.app = app;
            T = app.t;
        }
    }

    public static abstract class Elm extends UIElement {
        public final @NonNull App app;
        public final @NonNull TrRoot T;
        public Elm(@NonNull App app) {
            this.app = app;
            T = app.t;
        }

        public Elm(@NonNull App app, int i, int j) {
            super(i, j);
            this.app = app;
            T = app.t;
        }
    }
}
