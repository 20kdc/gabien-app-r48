/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.app;

import gabien.ui.*;
import r48.AdHocSaveLoad;
import r48.App;
import r48.io.IObjectBackend;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.schema.OpaqueSchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Used to contain static variables, now just initialization routines.
 * This is theoretically roughly one of the oldest classes in the project, but has been phased out.
 * Created on 12/27/16. Being phased out as of 26th February 2023, reduced to static methods as of the 28th.
 */
public class AppMain {
    public static void initializeUI(App app, final WindowCreatingUIElementConsumer uiTicker, boolean mobile) {
        app.np = new AppNewProject(app);
        app.ui = new AppUI(app, mobile);
        app.ui.initialize(uiTicker);
    }

    // Is this messy? Yes. Is it required? After someone lost some work to R48? YES IT DEFINITELY IS.
    // Later: I've reduced the amount of backups performed because it appears spikes were occurring all the time.
    public static void performSystemDump(App app, boolean emergency, String addendumData) {
        IRIO n = new IRIOGeneric(app.ilg.adhocIOContext);
        n.setObject("R48::Backup");
        n.addIVar("@emergency").setBool(emergency);
        if (!emergency) {
            IRIOGeneric n3 = AdHocSaveLoad.load("r48.revert.YOUR_SAVED_DATA");
            if (n3 != null) {
                // Unlink for disk space & memory usage reasons.
                // Already this is going to eat RAM.
                n3.rmIVar("@last");
                n.addIVar("@last").setDeepClone(n3);
            }
        }
        n.addIVar("@description").setString(addendumData);
        performSystemDumpBodyInto(app, n);
        if (emergency)
            System.err.println("emergency dump is now actually occurring. Good luck.");
        AdHocSaveLoad.save(emergency ? "r48.error.YOUR_SAVED_DATA" : "r48.revert.YOUR_SAVED_DATA", n);
        if (emergency)
            System.err.println("emergency dump is complete.");
    }
    private static void performSystemDumpBodyInto(App app, IRIO n) {
        IRIO h = n.addIVar("@objects");
        h.setHash();
        for (IObjectBackend.ILoadedObject rio : app.odb.modifiedObjects) {
            String s = app.odb.getIdByObject(rio);
            if (s != null)
                h.addHashVal(DMKey.ofStr(s)).setDeepClone(rio.getObject());
        }
    }

    public static void reloadSystemDump(App app) {
        IRIOGeneric sysDump = AdHocSaveLoad.load("r48.error.YOUR_SAVED_DATA");
        if (sysDump == null) {
            app.ui.launchDialog(app.t.g.dlgNoSysDump);
            return;
        }
        IRIO objs = sysDump.getIVar("@objects");
        for (DMKey rk : objs.getHashKeys()) {
            String name = rk.decString();
            IObjectBackend.ILoadedObject root = app.odb.getObject(name);
            if (root != null) {
                root.getObject().setDeepClone(sysDump.getHashVal(rk));
                app.odb.objectRootModified(root, new SchemaPath(new OpaqueSchemaElement(app), root));
            }
        }
        if (sysDump.getIVar("@emergency").getType() == 'T') {
            app.ui.launchDialog(app.t.g.dlgReloadED);
        } else {
            app.ui.launchDialog(app.t.g.dlgReloadPFD);
        }
    }
}
