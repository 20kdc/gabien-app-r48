/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.dbs;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.io.data.IRIO;
import r48.schema.SchemaElementIOP;
import r48.schema.util.SchemaPath;

/**
 * Interface for an object root.
 * Essentially, this is the primary interface between Schema and ObjectDB.
 * Importantly, it also works for anonymous objects not attached to ObjectDB.
 * Should implement toString.
 * Created 21st August, 2024.
 */
public abstract class ObjectRootHandle {
    /**
     * Finally solving the question of "what's the canonical root schema for this object handle?"
     */
    public final @Nullable SchemaElementIOP rootSchema;

    public ObjectRootHandle(@Nullable SchemaElementIOP rootSchema) {
        this.rootSchema = rootSchema;
    }

    protected final @NonNull LinkedList<WeakReference<Consumer<SchemaPath>>> objectListenersMap = new LinkedList<>();
    private boolean objectRootModifiedRecursion = false;
    private LinkedList<SchemaPath> objectRootModifiedQueue = new LinkedList<>();

    // Note that these are run at the end of frame,
    //  because there appears to be a performance issue with these being spammed over and over again. Oops.
    // Also note, these are all weakly referenced.

    /**
     * Registers a modification handler on this root.
     */
    public final void registerModificationHandler(Consumer<SchemaPath> handler) {
        objectListenersMap.add(new WeakReference<Consumer<SchemaPath>>(handler));
    }

    /**
     * Deregisters a modification handler on this root.
     */
    public final void deregisterModificationHandler(Consumer<SchemaPath> handler) {
        Utils.implRemoveFromGOCMH(objectListenersMap, handler);
    }

    /**
     * Sends notifications that the root was modified.
     */
    public final void objectRootModified(SchemaPath path) {
        if (objectRootModifiedRecursion) {
            objectRootModifiedQueue.add(path);
            return;
        }
        try {
            objectRootModifiedRecursion = true;
            objectRootModifiedPass(path);
            while (!objectRootModifiedQueue.isEmpty()) {
                objectRootModifiedPass(objectRootModifiedQueue.pop());
            }
        } finally {
            objectRootModifiedRecursion = false;
        }
    }

    /*
     * Object root modified pass.
     * Override to add more callbacks. 
     */
    public void objectRootModifiedPass(SchemaPath path) {
        Utils.handleNotificationList(objectListenersMap, path);
    }

    /**
     * Gets the IRIO.
     */
    public abstract IRIO getObject();

    /**
     * Attempts to ensure this root has been saved. (Might not be possible.)
     */
    public abstract void ensureSaved();

    /**
     * Isolated object.
     */
    public static class Isolated extends ObjectRootHandle {
        private final IRIO object;
        private final String name;
        public Isolated(@Nullable SchemaElementIOP rootSchema, IRIO o, String name) {
            super(rootSchema);
            this.object = o;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public IRIO getObject() {
            return object;
        }

        @Override
        public void ensureSaved() {
        }
    }

    public static final class Utils {
        private Utils() {
        }

        public static void implRemoveFromGOCMH(LinkedList<WeakReference<Consumer<SchemaPath>>> orCreateModificationHandlers, Consumer<SchemaPath> handler) {
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

        public static void handleNotificationList(LinkedList<WeakReference<Consumer<SchemaPath>>> notifyObjectModified, SchemaPath sp) {
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
                for (WeakReference<Consumer<SchemaPath>> spi : new LinkedList<>(notifyObjectModified)) {
                    Consumer<SchemaPath> ics = spi.get();
                    if (ics != null && sp != null)
                        ics.accept(sp);
                }
            }
        }
    }
}
