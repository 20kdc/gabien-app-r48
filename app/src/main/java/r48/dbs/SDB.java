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
import r48.DictionaryUpdaterRunnable;
import r48.io.IObjectBackend;
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

    private HashMap<String, SchemaElement> schemaDatabase = new HashMap<String, SchemaElement>();
    protected HashMap<String, SchemaElement> schemaTrueDatabase = new HashMap<String, SchemaElement>();
    private LinkedList<DictionaryUpdaterRunnable> dictionaryUpdaterRunnables = new LinkedList<DictionaryUpdaterRunnable>();
    private LinkedList<Runnable> mergeRunnables = new LinkedList<Runnable>();
    private LinkedList<String> remainingExpected = new LinkedList<String>();

    protected HashMap<String, CMDB> cmdbs = new HashMap<String, CMDB>();
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
        schemaDatabase.put("string", new StringSchemaElement(app, "", '\"'));
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

        schemaTrueDatabase.putAll(schemaDatabase);
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
        // If a placeholder exists, keep using that
        if (!schemaDatabase.containsKey(text))
            schemaDatabase.put(text, ise);
        schemaTrueDatabase.put(text, ise);
    }

    public SchemaElement getSDBEntry(final String text) {
        if (schemaDatabase.containsKey(text))
            return schemaDatabase.get(text);
        // Notably, the proxy is put in the database so the expectation is only added once.
        remainingExpected.add(text);
        SchemaElement ise = new NameProxySchemaElement(app, text, true);
        schemaDatabase.put(text, ise);
        return ise;
    }

    // Use if and only if you deliberately need the changing nature of a proxy (this disables the cache)
    public void ensureSDBProxy(String text) {
        if (schemaDatabase.containsKey(text)) {
            // Implicitly asserts that this is a proxy.
            ((NameProxySchemaElement) schemaDatabase.get(text)).useCache = false;
        } else {
            NameProxySchemaElement npse = new NameProxySchemaElement(app, text, false);
            schemaDatabase.put(text, npse);
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
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.sanitize();
        for (Runnable merge : mergeRunnables)
            merge.run();
    }

    public void updateDictionaries(IObjectBackend.ILoadedObject map) {
        boolean needsMerge = false;
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            needsMerge |= dur.actIfRequired(map);
        if (needsMerge)
            for (Runnable merge : mergeRunnables)
                merge.run();
    }

    public void kickAllDictionariesForMapChange() {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.run();
    }

    public void addDUR(DictionaryUpdaterRunnable dur) {
        ensureSDBProxy(dur.dict);
        dictionaryUpdaterRunnables.add(dur);
    }

    public void addMergeRunnable(String id, ISupplier<SchemaElement> s) {
        ensureSDBProxy(id);
        mergeRunnables.add(() -> {
            setSDBEntry(id, s.get());
        });
    }

    private class NameProxySchemaElement extends SchemaElement implements IProxySchemaElement {
        private final String tx;
        private boolean useCache;
        private SchemaElement cache = null;

        public NameProxySchemaElement(App app, String text, boolean useCach) {
            super(app);
            tx = text;
            useCache = useCach;
        }

        @Override
        public SchemaElement getEntry() {
            if (cache != null)
                return cache;
            SchemaElement r = schemaTrueDatabase.get(tx);
            if (r == null)
                throw new RuntimeException("Schema used " + tx + ", but it didn't exist when invoked.");
            if (useCache)
                cache = r;
            return r;
        }

        @Override
        public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
            return getEntry().buildHoldingEditor(target, launcher, path);
        }

        @Override
        public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
            getEntry().modifyVal(target, path, setDefault);
        }
    }
}
