/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.ui.UIElement;
import r48.App;
import r48.app.AppCore;
import r48.io.data.IRIO;
import r48.schema.*;
import r48.schema.arrays.*;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSrcLoc;

/**
 * The ultimate database, more or less, since this houses the data definitions needed to do things like edit Events.
 * Kinda required for reading maps.
 * Created on 12/30/16.
 */
public class SDB extends AppCore.Csv {
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
    private HashMap<String, DatumSrcLoc> remainingExpected = new HashMap<>();

    public final StandardArrayInterface standardArrayUi = new StandardArrayInterface();

    public SDB(AppCore app) {
        super(app);
    }

    public void confirmAllExpectationsMet() {
        if (remainingExpected.size() > 0)
            throw new RuntimeException("Remaining expectation " + remainingExpected.entrySet().iterator().next());
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
        return getSDBEntry(text, DatumSrcLoc.NONE);
    }

    public SchemaElement getSDBEntry(final String text, final DatumSrcLoc location) {
        SchemaElement tmp = schemaDatabase.get(text);
        if (tmp != null)
            return tmp;
        // Notably, the proxy is put in the database so the expectation is only added once.
        remainingExpected.put(text, location);
        SchemaElement ise = new NameProxySchemaElement((App) app, text);
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
            DynamicSchemaElement npse = new DynamicSchemaElement((App) app, text);
            schemaDatabase.put(text, npse);
            return npse;
        }
    }

    /**
     * Added for liblcf#245, not really something app should use otherwise
     */
    public HashSet<String> getAllSDBEntryIDs() {
        return new HashSet<>(schemaDatabase.keySet());
    }

    /**
     * Lists defined files.
     * The list actually comes out of EngineDef to stop MVM defining objects which it can then use to stage attacks on users.
     * Same rationale as EngineDef having the IO backend config, basically.
     */
    public LinkedList<ObjectInfo> listFileDefs() {
        LinkedList<ObjectInfo> fd = new LinkedList<ObjectInfo>();
        for (String s : app.engine.definesObjects)
            fd.add(new ObjectInfo(app, s));
        return fd;
    }

    public @Nullable SchemaElement findSchemaFor(@NonNull ObjectRootHandle ilo) {
        return findSchemaFor(app.odb.getIdByObject(ilo), ilo.getObject());
    }

    public @Nullable SchemaElement findSchemaFor(@Nullable String objId, @NonNull IRIO object) {
        if (objId != null)
            if (hasSDBEntry("File." + objId))
                return getSDBEntry("File." + objId);
        if (object.getType() == 'o')
            return getSDBEntry(object.getSymbol());
        return null;
    }

    public void startupSanitizeDictionaries() {
        for (DynamicSchemaUpdater dur : dictionaryUpdaterRunnables)
            dur.sanitize();
        for (Runnable merge : mergeRunnables)
            merge.run();
        for (String s : schemaDatabase.keySet()) {
            if (s.startsWith("File.")) {
                boolean exists = false;
                String comp = s.substring(5);
                for (String s2 : app.engine.definesObjects) {
                    if (s2.equals(comp)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists)
                    throw new RuntimeException("Schema defined " + s + " but was not authorized by enginedef.");
            }
        }
    }

    public void updateDictionaries(ObjectRootHandle map) {
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

    public void addMergeRunnable(String id, Supplier<SchemaElement> s) {
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
        boolean actIfRequired(ObjectRootHandle map);
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
        public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
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
        public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
            getEntry().visit(target, path, v, detailedPaths);
        }
    }

    private class NameProxySchemaElement extends BaseProxySchemaElement {
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

    public class DynamicSchemaElement extends BaseProxySchemaElement {
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
