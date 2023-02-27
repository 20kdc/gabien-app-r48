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

import gabien.ui.UIElement.UIProxy;

/**
 * An attempt to move as much as possible out of static variables.
 * Created 26th February, 2023
 */
public final class App extends AppCore {
    public HashMap<Integer, String> osSHESEDB;
    // scheduled tasks for when UI is around, not in UI because it may not init (ever, even!)
    public HashSet<Runnable> uiPendingRunnables = new HashSet<Runnable>();
    // these init during UI init!
    public AppUI ui;
    public AppNewProject np;

    public void shutdown() {
        if (ui != null) {
            if (ui.mapContext != null)
                ui.mapContext.freeOsbResources();
            ui.mapContext = null;
        }
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
}
