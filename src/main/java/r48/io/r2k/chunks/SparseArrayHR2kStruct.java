/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;

import java.util.Map;

/**
 * Created on 01/06/17.
 */
public class SparseArrayHR2kStruct<T extends IR2kStruct> extends SparseArrayR2kInterpretable<T> implements IR2kStruct {

    public SparseArrayHR2kStruct(ISupplier<T> call) {
        super(call);
    }

    @Override
    public RubyIO asRIO() {
        RubyIO ev = new RubyIO().setHash();
        for (Map.Entry<Integer, T> e : map.entrySet())
            ev.hashVal.put(new RubyIO().setFX(e.getKey()), e.getValue().asRIO());
        return ev;
    }

    @Override
    public void fromRIO(RubyIO src) {
        map.clear();
        for (Map.Entry<RubyIO, RubyIO> e : src.hashVal.entrySet()) {
            T n = constructor.get();
            n.fromRIO(e.getValue());
            map.put((int) e.getKey().fixnumVal, n);
        }
    }
}
