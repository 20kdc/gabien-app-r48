/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.ui.UIElement;
import gabien.uslx.append.ISupplier;
import r48.App;
import r48.io.IObjectBackend;
import r48.io.IObjectBackend.ILoadedObject;
import r48.io.data.IRIO;
import r48.schema.*;
import r48.schema.arrays.*;
import r48.schema.displays.EPGDisplaySchemaElement;
import r48.schema.displays.HuePickerSchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.LowerBoundIntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.*;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The ultimate database, more or less, since this houses the data definitions needed to do things like edit Events.
 * Kinda required for reading maps.
 * Created on 12/30/16.
 */
public class SDB extends App.Svc {
    // The very unsafe option which will turn on all sorts of automatic script helper functions.
    // Some of which currently WILL destroy scripts. 315, map29
    // Ok, so I've checked in various ways and done a full restore from virgin copy.
    // I've used UITest to avoid triggering any potentially destructive Schema code.
    // The answer is the same:
    // Entries 9 and 10 of entity 315, in the Map029 file, contain a duplicate Leave Block.
    // I have no idea how this was managed.
    // On another note, app.engine.allowIndentControl now contains the option this comment block refers to.

    private HashMap<String, SchemaElement> schemaDatabase = new HashMap<>();
    private LinkedList<DynamicSchemaUpdater> dictionaryUpdaterRunnables = new LinkedList<>();
    private LinkedList<Runnable> mergeRunnables = new LinkedList<>();
    private LinkedList<String> remainingExpected = new LinkedList<>();

    protected HashMap<String, CMDB> cmdbs = new HashMap<>();
    public final SDBHelpers helpers;

    public final StandardArrayInterface standardArrayUi = new StandardArrayInterface();

    public final OpaqueSchemaElement opaque;

    public SDB(App app) {
        super(app);
        helpers = new SDBHelpers(app);
        opaque = new OpaqueSchemaElement(app);
        schemaDatabase.put("nil", opaque);
        schemaDatabase.put("int", new IntegerSchemaElement(app, 0));
        schemaDatabase.put("roint", new ROIntegerSchemaElement(app, 0));
        schemaDatabase.put("int+0", new LowerBoundIntegerSchemaElement(app, 0, 0));
        schemaDatabase.put("int+1", new LowerBoundIntegerSchemaElement(app, 1, 1));
        schemaDatabase.put("index", new AMAISchemaElement(app));
        schemaDatabase.put("float", new FloatSchemaElement(app, "0", false));
        schemaDatabase.put("jnum", new FloatSchemaElement(app, "0", true));
        schemaDatabase.put("string", new StringSchemaElement(app, () -> "", '\"'));
        schemaDatabase.put("boolean", new BooleanSchemaElement(app, false));
        schemaDatabase.put("booleanDefTrue", new BooleanSchemaElement(app, true));
        schemaDatabase.put("int_boolean", new IntBooleanSchemaElement(app, false));
        schemaDatabase.put("int_booleanDefTrue", new IntBooleanSchemaElement(app, true));
        schemaDatabase.put("OPAQUE", opaque);
        schemaDatabase.put("hue", new HuePickerSchemaElement(app));

        schemaDatabase.put("percent", new LowerBoundIntegerSchemaElement(app, 0, 100));

        schemaDatabase.put("zlibBlobEditor", new ZLibBlobSchemaElement(app));
        schemaDatabase.put("stringBlobEditor", new StringBlobSchemaElement(app));

        schemaDatabase.put("internal_EPGD", new EPGDisplaySchemaElement(app));
        schemaDatabase.put("internal_scriptIE", new ScriptControlSchemaElement(app));

        schemaDatabase.put("internal_LF_INDEX", new OSStrHashMapSchemaElement(app));

        if (app.engine.defineIndent) {
            if (app.engine.allowIndentControl) {
                schemaDatabase.put("indent", new ROIntegerSchemaElement(app, 0));
            } else {
                schemaDatabase.put("indent", new IntegerSchemaElement(app, 0));
            }
        }
    }

    public void newCMDB(String a0) {
        if (cmdbs.containsKey(a0))
            throw new RuntimeException("Attempted to overwrite CMDB: " + a0);
        cmdbs.put(a0, new CMDB(this, a0));
    }

    public CMDB getCMDB(String arg) {
        CMDB cm = cmdbs.get(arg);
        if (cm == null)
            throw new RuntimeException("Expected CMDB to exist (and it didn't): " + arg);
        return cm;
    }

    public void loadCMDB(String arg, String fn) {
        getCMDB(arg).load(fn);
    }

    public void confirmAllExpectationsMet() {
        if (remainingExpected.size() > 0)
            throw new RuntimeException("Remaining expectation " + remainingExpected.getFirst());
        for (CMDB cmdb : cmdbs.values())
            cmdb.check();
    }

    public boolean hasSDBEntry(String text) {
        return schemaDatabase.containsKey(text);
    }

    public void setSDBEntry(final String text, SchemaElement ise) {
        remainingExpected.remove(text);
        SchemaElement se = schemaDatabase.get(text);
        if (se == null) {
            schemaDatabase.put(text, ise);
        } else if (se instanceof NameProxySchemaElement) {
            ((NameProxySchemaElement) se).cache = ise;
        } else {
            throw new RuntimeException("Cannot override SE " + text);
        }
    }

    public SchemaElement getSDBEntry(final String text) {
        SchemaElement tmp = schemaDatabase.get(text);
        if (tmp != null)
            return tmp;
        // Notably, the proxy is put in the database so the expectation is only added once.
        remainingExpected.add(text);
        SchemaElement ise = new NameProxySchemaElement(app, text);
        schemaDatabase.put(text, ise);
        return ise;
    }

    // Use if and only if you deliberately need the changing nature of a proxy (this disables the cache)
    public DynamicSchemaElement ensureSDBProxy(String text) {
        if (schemaDatabase.containsKey(text)) {
            SchemaElement se = schemaDatabase.get(text);
            if (!(se instanceof DynamicSchemaElement))
                throw new RuntimeException("DynamicSchemaElement expected: " + text);
            return (DynamicSchemaElement) se;
        } else {
            DynamicSchemaElement npse = new DynamicSchemaElement(app, text);
            schemaDatabase.put(text, npse);
            return npse;
        }
    }

    public LinkedList<ObjectInfo> listFileDefs() {
        LinkedList<ObjectInfo> fd = new LinkedList<ObjectInfo>();
        for (String s : schemaDatabase.keySet())
            if (s.startsWith("File."))
                fd.add(new ObjectInfo(app, s.substring(5), s));
        return fd;
    }

    public @Nullable SchemaElement findSchemaFor(@NonNull IObjectBackend.ILoadedObject ilo) {
        return findSchemaFor(app.odb.getIdByObject(ilo), ilo.getObject());
    }

    public @Nullable SchemaElement findSchemaFor(@Nullable String objId, @NonNull IRIO object) {
        if (objId != null)
            if (app.sdb.hasSDBEntry("File." + objId))
                return app.sdb.getSDBEntry("File." + objId);
        if (object.getType() == 'o')
            return app.sdb.getSDBEntry(object.getSymbol());
        return null;
    }

    public void startupSanitizeDictionaries() {
        for (DynamicSchemaUpdater dur : dictionaryUpdaterRunnables)
            dur.sanitize();
        for (Runnable merge : mergeRunnables)
            merge.run();
    }

    public void updateDictionaries(IObjectBackend.ILoadedObject map) {
        boolean needsMerge = false;
        for (DynamicSchemaUpdater dur : dictionaryUpdaterRunnables)
            needsMerge |= dur.actIfRequired(map);
        if (needsMerge)
            for (Runnable merge : mergeRunnables)
                merge.run();
    }

    public void kickAllDictionariesForMapChange() {
        // this just marks them for update
        for (DynamicSchemaUpdater dur : dictionaryUpdaterRunnables)
            dur.run();
    }

    public void addDUR(DynamicSchemaUpdater dur) {
        dictionaryUpdaterRunnables.add(dur);
    }

    public void addMergeRunnable(String id, ISupplier<SchemaElement> s) {
        final DynamicSchemaElement dse = ensureSDBProxy(id);
        mergeRunnables.add(() -> {
            dse.setEntry(s.get());
        });
    }

    public Collection<SchemaElement> getAllEntryValues() {
        LinkedList<SchemaElement> res = new LinkedList<>();
        for (SchemaElement se : schemaDatabase.values()) {
            if (se instanceof BaseProxySchemaElement) {
                res.add(((BaseProxySchemaElement) se).getEntry());
            } else {
                res.add(se);
            }
        }
        return res;
    }

    public interface DynamicSchemaUpdater extends Runnable {
        /**
         * This initializes the DUR the first time to a "blank slate".
         */
        void sanitize();
        /**
         * If required, updates the DUR.
         * Returns true if this is done.
         */
        boolean actIfRequired(ILoadedObject map);
    }

    private static abstract class BaseProxySchemaElement extends SchemaElement implements IProxySchemaElement {
        protected SchemaElement cache = null;

        public BaseProxySchemaElement(App app) {
            super(app);
        }

        @Override
        public SchemaElement getEntry() {
            if (cache == null)
                throw new RuntimeException("Schema used " + this + ", but it didn't exist when invoked.");
            return cache;
        }

        @Override
        public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
            return getEntry().buildHoldingEditor(target, launcher, path);
        }

        @Override
        public @Nullable String windowTitleSuffix(SchemaPath path) {
            return getEntry().windowTitleSuffix(path);
        }

        @Override
        public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
            getEntry().modifyVal(target, path, setDefault);
        }

        @Override
        public void visitChildren(IRIO target, SchemaPath path, Visitor v) {
            getEntry().visit(target, path, v);
        }
    }

    private class NameProxySchemaElement extends BaseProxySchemaElement implements IProxySchemaElement {
        private final String tx;

        public NameProxySchemaElement(App app, String text) {
            super(app);
            tx = text;
        }

        @Override
        public String toString() {
            return "(name proxy " + tx + ")";
        }
    }

    public class DynamicSchemaElement extends BaseProxySchemaElement implements IProxySchemaElement {
        private final String tx;

        private DynamicSchemaElement(App app, String text) {
            super(app);
            tx = text;
        }

        @Override
        public String toString() {
            return "(dynamic proxy " + tx + ")";
        }

        public void setEntry(SchemaElement se) {
            cache = se;
        }
    }
}
