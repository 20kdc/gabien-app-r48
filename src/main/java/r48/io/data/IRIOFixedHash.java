/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.data;

import java.util.HashMap;

/**
 * An IRIO object that's a hash with fixed key & value classes.
 * Uses hashmaps properly thanks to not using IRIOs for storage.
 * Created on November 24, 2018.
 */
public abstract class IRIOFixedHash<K, V extends IRIO> extends IRIOFixed {
    public HashMap<K, V> hashVal = new HashMap<K, V>();

    public IRIOFixedHash() {
        super('{');
    }

    public abstract K convertIRIOtoKey(IRIO i);

    public abstract IRIO convertKeyToIRIO(K i);

    public abstract V newValue();

    @Override
    public IRIO setHash() {
        hashVal.clear();
        return this;
    }

    @Override
    public IRIO[] getHashKeys() {
        IRIO[] ir = new IRIO[hashVal.size()];
        int idx = 0;
        for (K k : hashVal.keySet())
            ir[idx++] = convertKeyToIRIO(k);
        return ir;
    }

    @Override
    public V getHashVal(IRIO key) {
        K k = convertIRIOtoKey(key);
        return hashVal.get(k);
    }

    @Override
    public V addHashVal(IRIO key) {
        K k = convertIRIOtoKey(key);
        V v = newValue();
        hashVal.put(k, v);
        return v;
    }

    @Override
    public void removeHashVal(IRIO key) {
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
