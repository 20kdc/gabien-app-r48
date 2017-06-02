/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;

/**
 * Created on 01/06/17.
 */
public class ArrayR2kStruct<T extends IR2kStruct> extends ArrayR2kInterpretable<T> implements IR2kStruct {
    public ArrayR2kStruct(ArraySizeR2kInterpretable<T> other, ISupplier<T> c, boolean trust) {
        super(other, c, trust);
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO();
        rio.type = '[';
        rio.arrVal = new RubyIO[array.size()];
        for (int i = 0; i < array.size(); i++)
            rio.arrVal[i] = array.get(i).asRIO();
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        array.clear();
        for (RubyIO rio : src.arrVal) {
            T x = cons.get();
            x.fromRIO(rio);
            array.add(x);
        }
    }
}
