/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
