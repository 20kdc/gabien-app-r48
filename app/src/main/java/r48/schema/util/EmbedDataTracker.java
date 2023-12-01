/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import r48.io.data.IRIO;

import java.util.LinkedList;
import java.util.Stack;
import java.util.WeakHashMap;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Tracks scroll values and such.
 * Created on October 09, 2018.
 */
public class EmbedDataTracker {
    public WeakHashMap<SchemaPath, LinkedList<EmbedDataSlot<?>>> mapTree = new WeakHashMap<>();

    public EmbedDataTracker() {
    }

    public EmbedDataTracker(Stack<SchemaPath> paths, EmbedDataTracker other) {
        for (SchemaPath sp : paths) {
            LinkedList<EmbedDataSlot<?>> sph = other.mapTree.get(sp);
            if (sph != null)
                mapTree.put(sp, new LinkedList<EmbedDataSlot<?>>(sph));
        }
    }

    @SuppressWarnings("unchecked")
    public @NonNull <T> EmbedDataSlot<T> createSlot(SchemaPath current, IRIO target, EmbedDataKey<T> prop, T defVal) {
        LinkedList<EmbedDataSlot<?>> localEDKs = mapTree.get(current);
        if (localEDKs == null) {
            localEDKs = new LinkedList<EmbedDataSlot<?>>();
            mapTree.put(current, localEDKs);
        }
        for (EmbedDataSlot<?> edk : localEDKs)
            if (edk.target.equals(target))
                if (edk.prop.equals(prop))
                    return (EmbedDataSlot<T>) edk;
        EmbedDataSlot<T> newEDK = new EmbedDataSlot<T>(target, prop);
        newEDK.value = defVal;
        localEDKs.add(newEDK);
        return newEDK;
    }
}