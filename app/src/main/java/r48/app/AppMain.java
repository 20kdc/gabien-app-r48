/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.app;

import gabien.GaBIEn;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.AdHocSaveLoad;
import r48.App;
import r48.RubyIO;
import r48.cfg.Config;
import r48.dbs.ObjectDB;
import r48.dbs.SDB;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.map.systems.MapSystem;
import r48.schema.OpaqueSchemaElement;
import r48.schema.util.SchemaPath;

import java.util.*;

/**
 * Used to contain static variables, now just initialization routines.
 * This is theoretically roughly one of the oldest classes in the project, but has been phased out.
 * Created on 12/27/16. Being phased out as of 26th February 2023.
 */
public class AppMain {
    public static App initializeCore(Config c, final String rp, final String sip, final String gamepak, final IConsumer<String> progress) {
        final App app = new App(c, rp, sip, progress);

        // initialize core resources

        app.sdb = new SDB(app);

        app.sdb.readFile(gamepak + "Schema.txt"); // This does a lot of IO, for one line.

        // initialize everything else that needs initializing, starting with ObjectDB
        IObjectBackend backend = IObjectBackend.Factory.create(app.odbBackend, app.rootPath, app.dataPath, app.dataExt);
        app.odb = new ObjectDB(app, backend, (s) -> {
            if (app.system != null)
                app.system.saveHook(s);
        });

        app.system = MapSystem.create(app, app.sysBackend);

        // Final internal consistency checks and reading in dictionaries from target
        //  before starting the UI, which can cause external consistency checks
        //  (...and potentially cause havoc in the process)

        app.sdb.startupSanitizeDictionaries(); // in case an object using dictionaries has to be created to use dictionaries
        progress.accept(TXDB.get("Initializing dictionaries & creating objects..."));
        app.sdb.updateDictionaries(null);
        app.sdb.confirmAllExpectationsMet();
        return app;
    }

    public static ISupplier<IConsumer<Double>> initializeUI(App app, final WindowCreatingUIElementConsumer uiTicker, boolean mobile) {
        app.np = new AppNewProject(app);
        app.ui = new AppUI(app, mobile);
        return app.ui.initialize(uiTicker);
    }

    public static void shutdown(App app) {
        if (app != null)
            app.shutdown();
        GaBIEn.hintFlushAllTheCaches();
    }

    // Is this messy? Yes. Is it required? After someone lost some work to R48? YES IT DEFINITELY IS.
    // Later: I've reduced the amount of backups performed because it appears spikes were occurring all the time.
    public static void performSystemDump(App app, boolean emergency, String addendumData) {
        RubyIO n = new RubyIO();
        n.setHash();
        n.addIVar("@description").setString(addendumData, true);
        for (IObjectBackend.ILoadedObject rio : app.odb.modifiedObjects) {
            String s = app.odb.getIdByObject(rio);
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

    public static void reloadSystemDump(App app) {
        RubyIO sysDump = AdHocSaveLoad.load("r48.error.YOUR_SAVED_DATA");
        if (sysDump == null) {
            app.ui.launchDialog(TXDB.get("The system dump was unloadable. It should be: r48.error.YOUR_SAVED_DATA.r48"));
            return;
        }
        RubyIO possibleActualDump = sysDump.getInstVarBySymbol("@current");
        if (possibleActualDump != null)
            sysDump = possibleActualDump;
        for (Map.Entry<IRIO, IRIO> rio : sysDump.hashVal.entrySet()) {
            String name = rio.getKey().decString();
            IObjectBackend.ILoadedObject root = app.odb.getObject(name);
            if (root != null) {
                root.getObject().setDeepClone(rio.getValue());
                app.odb.objectRootModified(root, new SchemaPath(new OpaqueSchemaElement(app), root));
            }
        }
        if (possibleActualDump != null) {
            app.ui.launchDialog(TXDB.get("Power failure dump loaded."));
        } else {
            app.ui.launchDialog(TXDB.get("Error dump loaded."));
        }
    }
}
