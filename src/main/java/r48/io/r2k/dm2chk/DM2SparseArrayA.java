/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.dm2chk;

import gabien.ui.ISupplier;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedArray;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.SparseArrayR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Same as DM2SparseArrayH...
 * Created on December 05, 2018.
 */
public class DM2SparseArrayA<V extends IRIO> extends IRIOFixedArray<V> implements IR2kInterpretable {
    public final ISupplier<V> constructor;

    public DM2SparseArrayA(ISupplier<V> cons) {
        constructor = cons;
    }

    @Override
    public V newValue() {
        return constructor.get();
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        HashMap<Integer, V> hashVal = new HashMap<Integer, V>();
        SparseArrayR2kInterpretable.importData(hashVal, constructor, bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        // Bypass the type checker... :(
        HashMap<Integer, V> hashVal = new HashMap<Integer, V>();
        for (int i = 0; i < arrVal.length; i++) {
            V v = (V) arrVal[i];
            if (v != null)
                hashVal.put(i, v);
        }
        SparseArrayR2kInterpretable.exportData(hashVal, baos);
        return false;
    }
}
