/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import r48.app.AppCore;
import r48.app.TimeMachineChangeSource;
import r48.io.IObjectBackend;
import r48.io.data.DMContext;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.uslx.append.Block;

/**
 * Not quite a database, but not quite not a database either.
 * Created on 12/29/16.
 */
public class ObjectDB extends AppCore.Csv {
    /**
     * This exists to ensure that IDM3Data keeps the ILoadedObject in memory.
     * This ensures we can't lose an object as long as we have undo/redo data for it.
     */
    public static final DMContext.Key<IObjectBackend.ILoadedObject> DMCONTEXT_LOADED_OBJECT = new DMContext.Key<>();

    // Useful for object shenanigans.
    public final IObjectBackend backend;
    /**
     * The MapSystem save hook overwrites this for now
     */
    public @NonNull Consumer<String> saveHook = (id) -> {};

    public ObjectDB(AppCore app, IObjectBackend b) {
        super(app);
        backend = b;
    }

    public HashMap<String, WeakReference<IObjectBackend.ILoadedObject>> objectMap = new HashMap<String, WeakReference<IObjectBackend.ILoadedObject>>();
    public WeakHashMap<IObjectBackend.ILoadedObject, String> reverseObjectMap = new WeakHashMap<IObjectBackend.ILoadedObject, String>();
    // The values don't actually matter -
    //  this locks the object into memory for as long as it's modified.
    public HashSet<IObjectBackend.ILoadedObject> modifiedObjects = new HashSet<IObjectBackend.ILoadedObject>();
    public HashSet<IObjectBackend.ILoadedObject> newlyCreatedObjects = new HashSet<IObjectBackend.ILoadedObject>();
    public WeakHashMap<IObjectBackend.ILoadedObject, LinkedList<WeakReference<Consumer<SchemaPath>>>> objectListenersMap = new WeakHashMap<IObjectBackend.ILoadedObject, LinkedList<WeakReference<Consumer<SchemaPath>>>>();
    public HashMap<String, LinkedList<WeakReference<Consumer<SchemaPath>>>> objectRootListenersMap = new HashMap<String, LinkedList<WeakReference<Consumer<SchemaPath>>>>();
    private HashSet<Runnable> pendingModifications = new HashSet<Runnable>();

    private boolean objectRootModifiedRecursion = false;

    public @Nullable String getIdByObject(IObjectBackend.ILoadedObject obj) {
        return reverseObjectMap.get(obj);
    }
    public @NonNull String getIdByObjectOrThrow(IObjectBackend.ILoadedObject obj) {
        String id = reverseObjectMap.get(obj);
        if (id == null)
            throw new RuntimeException("Unable to get ID of " + obj.toString());
        return id;
    }

    private DMContext createDM3Context() {
        AtomicReference<DMContext> dmcx = new AtomicReference<DMContext>();
        DMContext dmc = new DMContext(new TimeMachineChangeSource(app.timeMachine) {
            @Override
            public void onTimeTravel() {
                markObjectAsAmbiguouslyModified(dmcx.get().get(DMCONTEXT_LOADED_OBJECT));
            }
        }, app.encoding);
        dmcx.set(dmc);
        return dmc;
    }

    // NOTE: Preferably call the one-parameter version,
    //  since that tries to create a sensible default.
    public IObjectBackend.ILoadedObject getObject(String id, String backupSchema) {
        WeakReference<IObjectBackend.ILoadedObject> omwr = objectMap.get(id);
        if (omwr != null) {
            IObjectBackend.ILoadedObject r = omwr.get();
            if (r != null)
                return r;
        }
        app.loadProgress.accept(T.u.odb_loadObj.r(id));
        DMContext context = createDM3Context();
        IObjectBackend.ILoadedObject rio;
        try (Block license = context.changes.openUnpackLicense()) {
            rio = backend.loadObject(id, context);
        }
        if (rio == null) {
            if (backupSchema != null) {
                if (!app.sdb.hasSDBEntry(backupSchema)) {
                    System.err.println("Could not find backup schema for object " + id);
                    return null;
                }
                SchemaElement ise = app.sdb.getSDBEntry(backupSchema);
                if (ise != null) {
                    // Note that the setup of the object counts as part of the object's unpack license.
                    // This is INTENTIONAL. It stops SEVERE crashes when undoing the new map operation.
                    try (Block license = context.changes.openUnpackLicense()) {
                        rio = backend.newObject(id, context);
                        if (rio == null)
                            return null;
                        context.set(DMCONTEXT_LOADED_OBJECT, rio);
                        SchemaPath.setDefaultValue(rio.getObject(), ise, null);
                    }
                    modifiedObjects.add(rio);
                    newlyCreatedObjects.add(rio);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            context.set(DMCONTEXT_LOADED_OBJECT, rio);
        }
        objectMap.put(id, new WeakReference<IObjectBackend.ILoadedObject>(rio));
        reverseObjectMap.put(rio, id);
        return rio;
    }

    public IObjectBackend.ILoadedObject getObject(String id) {
        return getObject(id, "File." + id);
    }

    public void ensureSaved(String id, IObjectBackend.ILoadedObject rio) {
        if (objectMap.containsKey(id)) {
            IObjectBackend.ILoadedObject rio2 = objectMap.get(id).get();
            if (rio2 != null) {
                if (rio2 != rio) {
                    // Overwriting - clean up.
                    System.out.println("WARNING: Overwriting shouldn't really ever happen.");
                    modifiedObjects.remove(rio2);
                    newlyCreatedObjects.remove(rio2);
                }
            }
        }
        try {
            rio.save();
            objectMap.put(id, new WeakReference<IObjectBackend.ILoadedObject>(rio));
            reverseObjectMap.put(rio, id);
            modifiedObjects.remove(rio);
            newlyCreatedObjects.remove(rio);
        } catch (Exception ioe) {
            // ERROR!
            app.reportNonCriticalErrorToUser(T.u.odb_saveErr.r(id), ioe);
            ioe.printStackTrace();
            return;
        }
        saveHook.accept(id);
    }

    public boolean getObjectModified(String id) {
        WeakReference<IObjectBackend.ILoadedObject> riow = objectMap.get(id);
        if (riow == null)
            return false;
        IObjectBackend.ILoadedObject potentiallyModified = riow.get();
        if (potentiallyModified != null)
            return modifiedObjects.contains(potentiallyModified);
        return false;
    }

    public boolean getObjectNewlyCreated(String id) {
        WeakReference<IObjectBackend.ILoadedObject> riow = objectMap.get(id);
        if (riow == null)
            return false;
        IObjectBackend.ILoadedObject potentiallyModified = riow.get();
        if (potentiallyModified != null)
            return newlyCreatedObjects.contains(potentiallyModified);
        return false;
    }

    private LinkedList<WeakReference<Consumer<SchemaPath>>> getOrCreateModificationHandlers(IObjectBackend.ILoadedObject p) {
        LinkedList<WeakReference<Consumer<SchemaPath>>> notifyObjectModified = objectListenersMap.get(p);
        if (notifyObjectModified == null) {
            notifyObjectModified = new LinkedList<WeakReference<Consumer<SchemaPath>>>();
            objectListenersMap.put(p, notifyObjectModified);
        }
        return notifyObjectModified;
    }

    private LinkedList<WeakReference<Consumer<SchemaPath>>> getOrCreateRootModificationHandlers(String p) {
        LinkedList<WeakReference<Consumer<SchemaPath>>> notifyObjectModified = objectRootListenersMap.get(p);
        if (notifyObjectModified == null) {
            notifyObjectModified = new LinkedList<WeakReference<Consumer<SchemaPath>>>();
            objectRootListenersMap.put(p, notifyObjectModified);
        }
        return notifyObjectModified;
    }

    // Note that these are run at the end of frame,
    //  because there appears to be a performance issue with these being spammed over and over again. Oops.
    // Also note, these are all weakly referenced.

    public void registerModificationHandler(IObjectBackend.ILoadedObject root, Consumer<SchemaPath> handler) {
        getOrCreateModificationHandlers(root).add(new WeakReference<Consumer<SchemaPath>>(handler));
    }

    public void deregisterModificationHandler(IObjectBackend.ILoadedObject root, Consumer<SchemaPath> handler) {
        removeFromGOCMH(getOrCreateModificationHandlers(root), handler);
    }

    public void registerModificationHandler(String root, Consumer<SchemaPath> handler) {
        getOrCreateRootModificationHandlers(root).add(new WeakReference<Consumer<SchemaPath>>(handler));
    }

    public void deregisterModificationHandler(String root, Consumer<SchemaPath> handler) {
        removeFromGOCMH(getOrCreateRootModificationHandlers(root), handler);
    }

    public void runPendingModifications() {
        LinkedList<Runnable> runs = new LinkedList<>(pendingModifications);
        pendingModifications.clear();
        for (Runnable r : runs)
            r.run();
    }

    public void objectRootModified(final IObjectBackend.ILoadedObject p, final SchemaPath path) {
        if (objectRootModifiedRecursion) {
            pendingModifications.add(new Runnable() {
                @Override
                public void run() {
                    // in case of mysterious error
                    objectRootModifiedRecursion = false;
                    objectRootModified(p, path);
                }
            });
            return;
        }
        objectRootModifiedRecursion = true;
        // Is this available in ObjectDB? If not, then it shouldn't be locked into permanent memory.
        // However, if there are modification listeners on this particular object, they get used
        if (reverseObjectMap.containsKey(p))
            modifiedObjects.add(p);
        LinkedList<WeakReference<Consumer<SchemaPath>>> notifyObjectModified = objectListenersMap.get(p);
        handleNotificationList(notifyObjectModified, path);
        String root = getIdByObject(path.root);
        if (root != null) {
            notifyObjectModified = objectRootListenersMap.get(root);
            handleNotificationList(notifyObjectModified, path);
        }
        objectRootModifiedRecursion = false;
    }

    private void handleNotificationList(LinkedList<WeakReference<Consumer<SchemaPath>>> notifyObjectModified, SchemaPath sp) {
        if (notifyObjectModified == null)
            return;
        Iterator<WeakReference<Consumer<SchemaPath>>> it = notifyObjectModified.iterator();
        while (it.hasNext()) {
            WeakReference<Consumer<SchemaPath>> spi = it.next();
            Consumer<SchemaPath> ics = spi.get();
            if (ics == null)
                it.remove();
        }
        if (sp != null) {
            for (WeakReference<Consumer<SchemaPath>> spi : new LinkedList<WeakReference<Consumer<SchemaPath>>>(notifyObjectModified)) {
                Consumer<SchemaPath> ics = spi.get();
                if (ics != null && sp != null)
                    ics.accept(sp);
            }
        }
    }

    public int countModificationListeners(IObjectBackend.ILoadedObject p) {
        int n = 0;
        LinkedList<WeakReference<Consumer<SchemaPath>>> notifyObjectModified = objectListenersMap.get(p);
        handleNotificationList(notifyObjectModified, null);
        if (notifyObjectModified != null)
            n += notifyObjectModified.size();
        String r = getIdByObject(p);
        if (r != null) {
            notifyObjectModified = objectRootListenersMap.get(r);
            handleNotificationList(notifyObjectModified, null);
            if (notifyObjectModified != null)
                n += notifyObjectModified.size();
        }
        return n;
    }

    private void removeFromGOCMH(LinkedList<WeakReference<Consumer<SchemaPath>>> orCreateModificationHandlers, Consumer<SchemaPath> handler) {
        WeakReference<Consumer<SchemaPath>> wr = null;
        for (WeakReference<Consumer<SchemaPath>> w : orCreateModificationHandlers) {
            if (w.get() == handler) {
                wr = w;
                break;
            }
        }
        if (wr != null)
            orCreateModificationHandlers.remove(wr);
    }

    public void ensureAllSaved() {
        for (IObjectBackend.ILoadedObject rio : new LinkedList<IObjectBackend.ILoadedObject>(modifiedObjects)) {
            String id = getIdByObject(rio);
            if (id != null)
                ensureSaved(id, rio);
        }
    }

    public void revertEverything() {
        app.timeMachine.clearUndoRedo();
        // Any object that can be reverted, revert it.
        // Mark them all down for removal from modifiedObjects later.
        LinkedList<IObjectBackend.ILoadedObject> pokedObjects = new LinkedList<>();
        for (IObjectBackend.ILoadedObject lo : modifiedObjects) {
            String id = getIdByObject(lo);
            if (id != null) {
                DMContext context = createDM3Context();
                IObjectBackend.ILoadedObject newVal;
                try (Block license = context.changes.openUnpackLicense()) {
                    newVal = backend.loadObject(id, context);
                }
                if (newVal != null) {
                    // Try doing things just by overwriting the internals, otherwise deep-clone
                    if (!lo.overwriteWith(newVal))
                        lo.getObject().setDeepClone(newVal.getObject());
                    pokedObjects.add(lo);
                }
            }
        }
        // Perform modification listeners.
        for (IObjectBackend.ILoadedObject lo : pokedObjects)
            markObjectAsAmbiguouslyModified(lo);
        // Remove from modifiedObjects - they don't count as modified.
        modifiedObjects.removeAll(pokedObjects);
        pokedObjects = null;
        System.gc();
    }

    /**
     * This is for use specifically by TimeMachine and revertEverything.
     */
    private void markObjectAsAmbiguouslyModified(IObjectBackend.ILoadedObject lo) {
        // Use an opaque schema element because we really don't have a good one here.
        // We don't use changeOccurred because that would activate schema processing, which is also undesired here.
        objectRootModified(lo, new SchemaPath(app.sdb.getSDBEntry("OPAQUE"), lo));
    }
}
