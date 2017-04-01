/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import r48.AppMain;
import r48.RubyIO;
import r48.io.IObjectBackend;
import r48.schema.ISchemaElement;
import r48.schema.util.SchemaPath;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.WeakHashMap;

/**
 * Not quite a database, but not quite not a database either.
 * Created on 12/29/16.
 */
public class ObjectDB {
    private final IObjectBackend backend;

    public ObjectDB(IObjectBackend b) {
        backend = b;
    }

    public HashMap<String, WeakReference<RubyIO>> objectMap = new HashMap<String, WeakReference<RubyIO>>();
    public WeakHashMap<RubyIO, String> reverseObjectMap = new WeakHashMap<RubyIO, String>();
    // The values don't actually matter -
    //  this locks the object into memory for as long as it's modified.
    public LinkedList<RubyIO> modifiedObjects = new LinkedList<RubyIO>();
    public WeakHashMap<RubyIO, LinkedList<Runnable>> objectListenersMap = new WeakHashMap<RubyIO, LinkedList<Runnable>>();

    public String getIdByObject(RubyIO obj) {
        return reverseObjectMap.get(obj);
    }

    // NOTE: Preferably call the one-parameter version,
    //  since that tries to create a sensible default.
    public RubyIO getObject(String id, String backupSchema) {
        if (objectMap.containsKey(id)) {
            RubyIO r = objectMap.get(id).get();
            if (r != null)
                return r;
        }
        RubyIO rio = backend.loadObjectFromFile(id);
        if (rio == null) {
            if (!AppMain.schemas.hasSDBEntry(backupSchema)) {
                System.err.println("Could not find backup schema for object " + id);
                return null;
            }
            ISchemaElement ise = AppMain.schemas.getSDBEntry(backupSchema);
            if (backupSchema != null) {
                if (ise != null) {
                    try {
                        rio = SchemaPath.createDefaultValue(ise, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        objectMap.put(id, new WeakReference<RubyIO>(rio));
        reverseObjectMap.put(rio, id);
        return rio;
    }

    public RubyIO getObject(String id) {
        return getObject(id, "File." + id);
    }

    public void ensureSaved(String id, RubyIO rio) {
        if (objectMap.containsKey(id)) {
            RubyIO rio2 = objectMap.get(id).get();
            if (rio2 != null) {
                if (rio2 != rio) {
                    // Overwriting - clean up.
                    System.out.println("WARNING: Overwriting shouldn't really ever happen.");
                    modifiedObjects.remove(rio2);
                }
            }
        }
        try {
            backend.saveObjectToFile(id, rio);
            objectMap.put(id, new WeakReference<RubyIO>(rio));
            reverseObjectMap.put(rio, id);
            modifiedObjects.remove(rio);
        } catch (IOException ioe) {
            // ERROR!
            AppMain.launchDialog("Error: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    public boolean getObjectModified(String id) {
        WeakReference<RubyIO> riow = objectMap.get(id);
        if (riow == null)
            return false;
        RubyIO potentiallyModified = riow.get();
        if (potentiallyModified != null)
            return modifiedObjects.contains(potentiallyModified);
        return false;
    }

    private LinkedList<Runnable> getOrCreateModificationHandlers(RubyIO p) {
        LinkedList<Runnable> notifyObjectModified = objectListenersMap.get(p);
        if (notifyObjectModified == null) {
            notifyObjectModified = new LinkedList<Runnable>();
            objectListenersMap.put(p, notifyObjectModified);
        }
        return notifyObjectModified;
    }

    public void registerModificationHandler(RubyIO root, Runnable handler) {
        getOrCreateModificationHandlers(root).add(handler);
    }

    public void deregisterModificationHandler(RubyIO root, Runnable handler) {
        getOrCreateModificationHandlers(root).remove(handler);
    }

    public void objectRootModified(RubyIO p) {
        // Is this available in ObjectDB?
        // If not, it's probably a default value that got modified,
        //  and the required root modification message will come later.
        if (!reverseObjectMap.containsKey(p))
            return;
        if (!modifiedObjects.contains(p))
            modifiedObjects.add(p);
        LinkedList<Runnable> notifyObjectModified = objectListenersMap.get(p);
        if (notifyObjectModified != null)
            for (Runnable r : new LinkedList<Runnable>(notifyObjectModified))
                r.run();
    }

    public int countModificationListeners(RubyIO p) {
        LinkedList<Runnable> notifyObjectModified = objectListenersMap.get(p);
        if (notifyObjectModified != null)
            return notifyObjectModified.size();
        return 0;
    }

    public void ensureAllSaved() {
        for (RubyIO rio : new LinkedList<RubyIO>(modifiedObjects)) {
            String id = getIdByObject(rio);
            if (id != null)
                ensureSaved(id, rio);
        }
    }
}
