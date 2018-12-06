/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedHash;
import r48.io.data.IRIOFixnum;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Copied from SparseArrayHR2kStruct on December 6th 2018.
 * This is a compatibility layer so Database work can be more gradual.
 */
public class CompatSparseArrayHR2kStruct<T extends IR2kStruct> extends IRIOFixedHash<Integer, RubyIO> implements IR2kStruct {
    public final ISupplier<T> constructor;

    public CompatSparseArrayHR2kStruct(ISupplier<T> call) {
        constructor = call;
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setDeepClone(this);
    }

    @Override
    public void fromRIO(IRIO src) {
        setDeepClone(src);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        HashMap<Integer, T> actMap = new HashMap<Integer, T>();
        SparseArrayR2kInterpretable.importData(actMap, constructor, bais);
        for (Map.Entry<Integer, T> me : actMap.entrySet())
            hashVal.put(me.getKey(), me.getValue().asRIO());
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        HashMap<Integer, T> actMap = new HashMap<Integer, T>();
        for (Map.Entry<Integer, RubyIO> me : hashVal.entrySet()) {
            T vt = constructor.get();
            vt.fromRIO(me.getValue());
            actMap.put(me.getKey(), vt);
        }
        SparseArrayR2kInterpretable.exportData(actMap, baos);
        return false;
    }

    @Override
    public Integer convertIRIOtoKey(IRIO i) {
        return (int) i.getFX();
    }

    @Override
    public IRIO convertKeyToIRIO(Integer i) {
        return new IRIOFixnum(i);
    }

    @Override
    public RubyIO newValue() {
        return new RubyIO().setNull();
    }
}
