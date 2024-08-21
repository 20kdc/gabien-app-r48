/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.dbs;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;

import r48.io.data.IRIO;
import r48.schema.util.SchemaPath;

/**
 * Interface for an object root.
 * Importantly, this works for anonymous objects.
 * Should implement toString.
 * Created 21st August, 2024.
 */
public abstract class ObjectRootHandle {
    protected final @NonNull LinkedList<WeakReference<Consumer<SchemaPath>>> objectListenersMap = new LinkedList<>();

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
        Isolated.implRemoveFromGOCMH(objectListenersMap, handler);
    }

    /**
     * Sends notifications that the root was modified.
     */
    public abstract void objectRootModified(SchemaPath path);

    /**
     * Gets the IRIO.
     */
    public abstract IRIO getObject();

    /**
     * Attempts to ensure this root has been saved. (Might not be possible.)
     */
    public abstract void ensureSaved();

    public static final class Isolated extends ObjectRootHandle {
        private final IRIO object;
        public Isolated(IRIO o) {
            this.object = o;
        }

        @Override
        public void objectRootModified(SchemaPath path) {
        }

        @Override
        public IRIO getObject() {
            return object;
        }

        @Override
        public void ensureSaved() {
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
    }
}
