/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Let's just hope this all works out.
 * Created on 31/05/17.
 */
public class SparseArrayR2kInterpretable<T extends IR2kStruct> implements IR2kInterpretable {
    public final ISupplier<T> constructor;
    public final HashMap<Integer, T> map = new HashMap<Integer, T>();

    public SparseArrayR2kInterpretable(ISupplier<T> call) {
        constructor = call;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        map.clear();
        int entries = R2kUtil.readLcfVLI(bais);
        for (int i = 0; i < entries; i++) {
            int k = R2kUtil.readLcfVLI(bais);
            T target = constructor.get();
            target.importData(bais);
            map.put(k, target);
        }
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        LinkedList<Integer> sort = new LinkedList<Integer>(map.keySet());
        Collections.sort(sort);
        R2kUtil.writeLcfVLI(baos, sort.size());
        for (Integer i : sort) {
            R2kUtil.writeLcfVLI(baos, i);
            map.get(i).exportData(baos);
        }
        return false;
    }
}
