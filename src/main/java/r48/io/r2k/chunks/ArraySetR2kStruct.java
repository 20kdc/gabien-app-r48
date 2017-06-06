/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;

/**
 * Created on 06/06/17.
 */
public class ArraySetR2kStruct<T extends IR2kStruct> extends ArrayR2kInterpretable<T> implements IR2kStruct {
    public ArraySetR2kStruct(ArraySizeR2kInterpretable<T> other, ISupplier<T> c, boolean trust) {
        super(other, c, trust);
    }

    @Override
    public RubyIO asRIO() {
        RubyIO examineElement = cons.get().asRIO();
        RubyIO hash = new RubyIO().setHash();
        int idx = 0;
        for (T t : array) {
            RubyIO rio = t.asRIO();
            // remove useless elements
            if (!RubyIO.rubyEquals(rio, examineElement))
                hash.hashVal.put(new RubyIO().setFX(idx++), rio);
        }
        return hash;
    }

    @Override
    public void fromRIO(RubyIO src) {
        int maxIdx = -1;
        for (RubyIO rio : src.hashVal.keySet())
            if (rio.fixnumVal > maxIdx)
                maxIdx = (int) rio.fixnumVal;
        array.clear();
        for (int i = 0; i < maxIdx; i++) {
            T v = cons.get();
            RubyIO potential = src.getHashVal(new RubyIO().setFX(i));
            if (potential != null)
                v.fromRIO(potential);
            array.add(v);
        }
    }
}
