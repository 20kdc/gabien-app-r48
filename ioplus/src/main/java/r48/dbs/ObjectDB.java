/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import r48.dbs.ObjectRootHandle.Utils;
import r48.io.IObjectBackend;
import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.undoredo.TimeMachine;
import r48.io.undoredo.TimeMachineChangeSource;
import r48.schema.SchemaElementIOP;
import r48.schema.util.SchemaPath;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.uslx.append.Block;

/**
 * Not quite a database, but not quite not a database either.
 * Created on 12/29/16.
 */
public final class ObjectDB {
    public final ObjectDB.Host host;

    /**
     * This exists to ensure that IDM3Data keeps the IObjectRootHandle in memory.
     * This ensures we can't lose an object as long as we have undo/redo data for it.
     */
    public static final DMContext.Key<ObjectRootHandle> DMCONTEXT_LOADED_OBJECT = new DMContext.Key<>();

    // Useful for object shenanigans.
    public final IObjectBackend backend;
    /**
     * The MapSystem save hook overwrites this for now
     */
    public @NonNull Consumer<String> saveHook = (id) -> {};

    public ObjectDB(Host host, IObjectBackend b) {
        this.host = host;
        backend = b;
    }

    public HashMap<String, WeakReference<ODBHandle>> objectMap = new HashMap<>();
    // The values don't actually matter -
    //  this locks the object into memory for as long as it's modified.
    public HashSet<ODBHandle> modifiedObjects = new HashSet<>();
    public HashSet<ODBHandle> newlyCreatedObjects = new HashSet<>();
    public HashMap<String, LinkedList<WeakReference<Consumer<SchemaPath>>>> objectRootListenersMap = new HashMap<>();
    private HashSet<Runnable> pendingModifications = new HashSet<>();

    public @Nullable String getIdByObject(ObjectRootHandle obj) {
        if (obj instanceof ODBHandle)
            return ((ODBHandle) obj).id;
        return null;
    }

    private DMContext createDM3Context() {
        AtomicReference<DMContext> dmcx = new AtomicReference<DMContext>();
        DMContext dmc = new DMContext(new TimeMachineChangeSource(host.odbHostGetTimeMachine()) {
            @Override
            public void onTimeTravel() {
                markObjectAsAmbiguouslyModified(dmcx.get().get(DMCONTEXT_LOADED_OBJECT));
            }
        }, host.odbHostGetEncoding());
        dmcx.set(dmc);
        return dmc;
    }

    /**
     * Tries to get an object without creating it.
     */
    private @Nullable ObjectRootHandle tryGetObjectInternal(String id) {
        WeakReference<ODBHandle> riow = objectMap.get(id);
        if (riow == null)
            return null;
        return riow.get();
    }

    public ObjectRootHandle getObject(String id) {
        return getObject(id, true);
    }

    public ObjectRootHandle getObject(String id, boolean create) {
        ObjectRootHandle omwr = tryGetObjectInternal(id);
        if (omwr != null)
            return omwr;
        host.odbHostObjectLoadMsg(id);
        DMContext context = createDM3Context();
        IObjectBackend.ILoadedObject rio;
        try (Block license = context.changes.openUnpackLicense()) {
            rio = backend.loadObject(id, context);
        }
        ODBHandle rootHandle;
        SchemaElementIOP ise = host.odbHostMapObjectIDToSchema(id);
        if (rio == null) {
            if (create && ise != null) {
                // Note that the setup of the object counts as part of the object's unpack license.
                // This is INTENTIONAL. It stops SEVERE crashes when undoing the new map operation.
                try (Block license = context.changes.openUnpackLicense()) {
                    rio = backend.newObject(id, context);
                    if (rio == null)
                        return null;
                    rootHandle = new ODBHandle(ise, id, rio);
                    context.set(DMCONTEXT_LOADED_OBJECT, rootHandle);
                    SchemaPath.setDefaultValue(rio.getObject(), ise, null);
                }
                modifiedObjects.add(rootHandle);
                newlyCreatedObjects.add(rootHandle);
            } else {
                return null;
            }
        } else {
            rootHandle = new ODBHandle(ise, id, rio);
            context.set(DMCONTEXT_LOADED_OBJECT, rootHandle);
        }
        objectMap.put(id, new WeakReference<ODBHandle>(rootHandle));
        return rootHandle;
    }

    public boolean getObjectNewlyCreated(String id) {
        ObjectRootHandle potentiallyModified = tryGetObjectInternal(id);
        if (potentiallyModified != null)
            return newlyCreatedObjects.contains(potentiallyModified);
        return false;
    }

    private LinkedList<WeakReference<Consumer<SchemaPath>>> getOrCreateRootModificationHandlers(String p) {
        LinkedList<WeakReference<Consumer<SchemaPath>>> notifyObjectModified = objectRootListenersMap.get(p);
        if (notifyObjectModified == null) {
            notifyObjectModified = new LinkedList<WeakReference<Consumer<SchemaPath>>>();
            objectRootListenersMap.put(p, notifyObjectModified);
        }
        return notifyObjectModified;
    }

    public void registerModificationHandler(String root, Consumer<SchemaPath> handler) {
        getOrCreateRootModificationHandlers(root).add(new WeakReference<Consumer<SchemaPath>>(handler));
    }

    public void deregisterModificationHandler(String root, Consumer<SchemaPath> handler) {
        Utils.implRemoveFromGOCMH(getOrCreateRootModificationHandlers(root), handler);
    }

    public void runPendingModifications() {
        LinkedList<Runnable> runs = new LinkedList<>(pendingModifications);
        pendingModifications.clear();
        for (Runnable r : runs)
            r.run();
    }

    public int countModificationListeners(ObjectRootHandle p) {
        if (!(p instanceof ODBHandle))
            return 0;
        ODBHandle rootHandle = (ODBHandle) p;
        int n = 0;
        LinkedList<WeakReference<Consumer<SchemaPath>>> notifyObjectModified = rootHandle.objectListenersMap;
        Utils.handleNotificationList(notifyObjectModified, null);
        if (notifyObjectModified != null)
            n += notifyObjectModified.size();
        String r = rootHandle.id;
        notifyObjectModified = objectRootListenersMap.get(r);
        Utils.handleNotificationList(notifyObjectModified, null);
        if (notifyObjectModified != null)
            n += notifyObjectModified.size();
        return n;
    }

    public void ensureAllSaved() {
        for (ObjectRootHandle rio : new LinkedList<>(modifiedObjects))
            rio.ensureSaved();
    }

    public void revertEverything() {
        host.odbHostGetTimeMachine().clearUndoRedo();
        // Any object that can be reverted, revert it.
        // Mark them all down for removal from modifiedObjects later.
        LinkedList<ODBHandle> pokedObjects = new LinkedList<>();
        for (ODBHandle lo : modifiedObjects) {
            DMContext context = createDM3Context();
            IObjectBackend.ILoadedObject newVal;
            try (Block license = context.changes.openUnpackLicense()) {
                newVal = backend.loadObject(lo.id, context);
            }
            if (newVal != null) {
                // Try doing things just by overwriting the internals, otherwise deep-clone
                if (!lo.ilo.overwriteWith(newVal))
                    lo.getObject().setDeepClone(newVal.getObject());
                pokedObjects.add(lo);
            }
        }
        // Perform modification listeners.
        for (ObjectRootHandle lo : pokedObjects)
            markObjectAsAmbiguouslyModified(lo);
        // Remove from modifiedObjects - they don't count as modified.
        modifiedObjects.removeAll(pokedObjects);
        pokedObjects = null;
        System.gc();
    }

    /**
     * This is for use specifically by TimeMachine and revertEverything.
     */
    private void markObjectAsAmbiguouslyModified(ObjectRootHandle lo) {
        // Use an opaque schema element because we really don't have a good one here.
        // We don't use changeOccurred because that would activate schema processing, which is also undesired here.
        lo.objectRootModified(new SchemaPath(host.odbHostGetOpaqueSE(), lo));
    }

    /**
     * This class should not expose any further API that non-ODB consumers might (ab)use.
     */
    public class ODBHandle extends ObjectRootHandle {
        private final @NonNull IObjectBackend.ILoadedObject ilo;
        private final @NonNull String id;

        public ODBHandle(@Nullable SchemaElementIOP rootSchema, @NonNull String id, @NonNull IObjectBackend.ILoadedObject ilo) {
            super(rootSchema);
            this.ilo = ilo;
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public IRIO getObject() {
            return ilo.getObject();
        }

        @Override
        public void objectRootModifiedPass(SchemaPath path) {
            super.objectRootModifiedPass(path);
            if (tryGetObjectInternal(id) == this) {
                modifiedObjects.add(this);
                Utils.handleNotificationList(getOrCreateRootModificationHandlers(id), path);
            }
        }

        @Override
        public void ensureSaved() {
            try {
                if (tryGetObjectInternal(id) != this)
                    throw new RuntimeException("We somehow lost object " + id);
                ilo.save();
                modifiedObjects.remove(this);
                newlyCreatedObjects.remove(this);
            } catch (Exception ioe) {
                // ERROR!
                host.odbHostReportSaveError(id, ioe);
                ioe.printStackTrace();
                return;
            }
            saveHook.accept(id);
        }
    }

    public static interface Host {
        /**
         * Gets the time machine. This must be static.
         */
        TimeMachine odbHostGetTimeMachine();

        /**
         * Gets OPAQUE.
         */
        SchemaElementIOP odbHostGetOpaqueSE();

        /**
         * Proxy for MapSystem.
         */
        SchemaElementIOP odbHostMapObjectIDToSchema(String id);

        /**
         * Gets the ODB encoding. This must be static.
         */
        Charset odbHostGetEncoding();

        /**
         * Reports object load (i.e. in a loading screen)
         */
        void odbHostObjectLoadMsg(String objectId);

        /**
         * Reports an error saving the given object with the given exception.
         */
        void odbHostReportSaveError(String id, Exception ioe);
    }
}
