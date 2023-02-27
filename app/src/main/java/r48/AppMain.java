/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import gabien.GaBIEn;
import gabien.ui.*;
import gabien.uslx.append.*;
import gabienapp.UIFancyInit;
import r48.dbs.ObjectDB;
import r48.dbs.SDB;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.map.UIMapView;
import r48.map.systems.MapSystem;
import r48.schema.OpaqueSchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaHostImpl;
import r48.schema.util.SchemaPath;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * The static variable holding class.
 * Created on 12/27/16. Being phased out as of 26th February 2023.
 */
public class AppMain {
    // The last AppMain static variable, in the process of being phased out.
    public static App instance;
    public static SDB schemas = null;

    public static void initializeCore(final String rp, final String sip, final String gamepak) {
        instance = new App();

        instance.rootPath = rp;
        instance.secondaryImagePath = sip;

        // initialize core resources

        schemas = new SDB(instance);
        instance.magicalBindingCache = new WeakHashMap<IRIO, HashMap<IMagicalBinder, WeakReference<RubyIO>>>();

        schemas.readFile(gamepak + "Schema.txt"); // This does a lot of IO, for one line.

        // initialize everything else that needs initializing, starting with ObjectDB
        IObjectBackend backend = IObjectBackend.Factory.create(instance.odbBackend, instance.rootPath, instance.dataPath, instance.dataExt);
        instance.odb = new ObjectDB(schemas.opaque, backend, (s) -> {
            if (instance.system != null)
                instance.system.saveHook(s);
        });

        instance.system = MapSystem.create(instance, instance.sysBackend);

        // Final internal consistency checks and reading in dictionaries from target
        //  before starting the UI, which can cause external consistency checks
        //  (...and potentially cause havoc in the process)

        schemas.startupSanitizeDictionaries(); // in case an object using dictionaries has to be created to use dictionaries
        UIFancyInit.submitToConsoletron(TXDB.get("Initializing dictionaries & creating objects..."));
        schemas.updateDictionaries(null);
        schemas.confirmAllExpectationsMet();
    }

    public static ISupplier<IConsumer<Double>> initializeUI(final WindowCreatingUIElementConsumer uiTicker) {
        instance.np = new AppNewProject(instance);
        instance.ui = new AppUI(instance);
        return instance.ui.initialize(uiTicker);
    }

    // Notably, you can't use this for non-roots because you'll end up bypassing ObjectDB.
    public static ISchemaHost launchSchema(String s, IObjectBackend.ILoadedObject rio, UIMapView context) {
        // Responsible for keeping listeners in place so nothing breaks.
        SchemaHostImpl watcher = new SchemaHostImpl(instance, context);
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
        instance.ui.launchDialog(s);
    }

    public static void shutdown() {
        if (instance != null)
            instance.shutdown();
        instance = null;
        schemas = null;
        TXDB.flushNameDB();
        GaBIEn.hintFlushAllTheCaches();
    }

    // Is this messy? Yes. Is it required? After someone lost some work to R48? YES IT DEFINITELY IS.
    // Later: I've reduced the amount of backups performed because it appears spikes were occurring all the time.
    public static void performSystemDump(boolean emergency, String addendumData) {
        RubyIO n = new RubyIO();
        n.setHash();
        n.addIVar("@description").setString(addendumData, true);
        for (IObjectBackend.ILoadedObject rio : instance.odb.modifiedObjects) {
            String s = instance.odb.getIdByObject(rio);
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
            IObjectBackend.ILoadedObject root = instance.odb.getObject(name);
            if (root != null) {
                root.getObject().setDeepClone(rio.getValue());
                instance.odb.objectRootModified(root, new SchemaPath(new OpaqueSchemaElement(instance), root));
            }
        }
        if (possibleActualDump != null) {
            AppMain.launchDialog(TXDB.get("Power failure dump loaded."));
        } else {
            AppMain.launchDialog(TXDB.get("Error dump loaded."));
        }
    }
}
