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
import gabien.ui.UIElement;
import gabien.ui.UIElement.UIPanel;
import gabien.ui.UIElement.UIProxy;
import gabien.uslx.append.IConsumer;
import r48.app.AppCore;
import r48.app.AppNewProject;
import r48.app.AppUI;
import r48.app.IAppAsSeenByLauncher;
import r48.app.InterlaunchGlobals;
import r48.map.StuffRenderer;
import r48.minivm.MVMCContext;

/**
 * An attempt to move as much as possible out of static variables.
 * Created 26th February, 2023
 */
public final class App extends AppCore implements IAppAsSeenByLauncher {
    public HashMap<Integer, String> osSHESEDB;
    // scheduled tasks for when UI is around, not in UI because it may not init (ever, even!)
    public HashSet<Runnable> uiPendingRunnables = new HashSet<Runnable>();
    // these init during UI init!
    public AppUI ui;
    public AppNewProject np;

    // The global context-independent stuffRenderer. *Only use outside of maps.*
    public StuffRenderer stuffRendererIndependent;

    // State for in-system copy/paste
    public RubyIO theClipboard = null;
    public final Runnable applyConfigChange = () -> {
        c.applyUIGlobals();
    };

    // VM context
    public final MVMCContext vmCtx = new MVMCContext();

    /**
     * Initialize App.
     * Warning: Occurs off main thread.
     */
    public App(InterlaunchGlobals ilg, String rp, String sip, IConsumer<String> loadProgress) {
        super(ilg, rp, sip, loadProgress);
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
        public Svc(@NonNull App app) {
            this.app = app;
        }
    }

    public static class Prx extends UIProxy {
        public final @NonNull App app;
        public Prx(@NonNull App app) {
            this.app = app;
        }
    }

    public static abstract class Pan extends UIPanel {
        public final @NonNull App app;
        public Pan(@NonNull App app) {
            this.app = app;
        }
    }

    public static abstract class Elm extends UIElement {
        public final @NonNull App app;

        public Elm(@NonNull App app) {
            this.app = app;
        }

        public Elm(@NonNull App app, int i, int j) {
            this.app = app;
        }
    }
}
