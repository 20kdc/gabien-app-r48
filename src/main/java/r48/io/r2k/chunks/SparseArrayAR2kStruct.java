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
public class SparseArrayAR2kStruct<T extends IR2kStruct> extends SparseArrayR2kInterpretable<T> implements IR2kStruct {

    public SparseArrayAR2kStruct(ISupplier<T> call) {
        super(call);
    }

    @Override
    public RubyIO asRIO() {
        int max = -1;
        for (Map.Entry<Integer, T> e : map.entrySet())
            if (e.getKey() > max)
                max = e.getKey();
        RubyIO[] resArray = new RubyIO[max + 1];
        for (int i = 0; i < resArray.length; i++)
            resArray[i] = new RubyIO().setNull();
        for (Map.Entry<Integer, T> e : map.entrySet())
            resArray[e.getKey()] = e.getValue().asRIO();
        RubyIO r = new RubyIO();
        r.type = '[';
        r.arrVal = resArray;
        return r;
    }

    @Override
    public void fromRIO(RubyIO src) {
        map.clear();
        int i = 0;
        for (RubyIO rio : src.arrVal) {
            // Nulls are used for padding here, don't include them
            if (rio.type != '0') {
                T n = constructor.get();
                n.fromRIO(rio);
                map.put(i, n);
            }
            i++;
        }
    }
}
