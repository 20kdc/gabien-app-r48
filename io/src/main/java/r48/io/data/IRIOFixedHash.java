/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An IRIO object that's a hash with fixed key & value classes.
 * Uses hashmaps properly thanks to not using IRIOs for storage.
 * Created on November 24, 2018.
 */
public abstract class IRIOFixedHash<K, V extends IRIO> extends IRIOFixedData {
    public HashMap<K, V> hashVal = new HashMap<K, V>();

    public IRIOFixedHash(@NonNull DMContext context) {
        super(context, '{');
    }

    @SuppressWarnings("unchecked")
    @Override
    public Runnable saveState() {
        HashMap<K, V> nkv = (HashMap<K, V>) hashVal.clone();
        return () -> {
            hashVal.clear();
            hashVal.putAll(nkv);
        };
    }

    public abstract K convertIRIOtoKey(RORIO i);

    public abstract DMKey convertKeyToIRIO(K i);

    public abstract V newValue();

    @Override
    public IRIO setHash() {
        trackingWillChange();
        hashVal.clear();
        return this;
    }

    @Override
    public DMKey[] getHashKeys() {
        DMKey[] ir = new DMKey[hashVal.size()];
        int idx = 0;
        for (K k : hashVal.keySet())
            ir[idx++] = convertKeyToIRIO(k);
        return ir;
    }

    @Override
    public V getHashVal(DMKey key) {
        K k = convertIRIOtoKey(key);
        return hashVal.get(k);
    }

    @Override
    public V addHashVal(DMKey key) {
        trackingWillChange();
        K k = convertIRIOtoKey(key);
        V v = newValue();
        hashVal.put(k, v);
        return v;
    }

    @Override
    public void removeHashVal(DMKey key) {
        trackingWillChange();
        K k = convertIRIOtoKey(key);
        hashVal.remove(k);
    }

    @Override
    public String[] getIVars() {
        return new String[0];
    }

    @Override
    public IRIO addIVar(String sym) {
        return null;
    }

    @Override
    public IRIO getIVar(String sym) {
        return null;
    }
}
