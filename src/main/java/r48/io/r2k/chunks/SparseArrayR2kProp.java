/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.obj.Event;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Let's just hope this all works out.
 * Created on 31/05/17.
 */
public class SparseArrayR2kProp<T extends IR2kInterpretable> implements IR2kProp {
    public final ISupplier<T> constructor;
    public final HashMap<Integer, T> map = new HashMap<Integer, T>();
    public SparseArrayR2kProp(ISupplier<T> call) {
        constructor = call;
    }

    @Override
    public byte[] getData() throws IOException {
        throw new RuntimeException("incomplete");
    }

    @Override
    public void importData(byte[] data) throws IOException {
        map.clear();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        int entries = R2kUtil.readLcfVLI(bais);
        for (int i = 0; i < entries; i++) {
            int k = R2kUtil.readLcfVLI(bais);
            T target = constructor.get();
            target.importData(bais);
            map.put(k, target);
        }
        bais.close();
    }

    public RubyIO toRIOHash() {
        RubyIO ev = new RubyIO().setHash();
        for (Map.Entry<Integer, T> e : map.entrySet())
            ev.hashVal.put(new RubyIO().setFX(e.getKey()), e.getValue().asRIO());
        return ev;
    }

    public RubyIO toRIOArray() {
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
}
