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
import r48.io.data.IRIONullable;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Same as DM2SparseArrayH.
 * A problem arises because the contents must be nullable. IRIONullable was created to resolve this.
 * Created on December 05, 2018.
 */
public class DM2SparseArrayA<V extends IRIO> extends IRIOFixedArray<IRIONullable<V>> implements IR2kInterpretable {
    public final ISupplier<V> constructor;

    public DM2SparseArrayA(ISupplier<V> cons) {
        constructor = cons;
    }

    @Override
    public IRIONullable<V> newValue() {
        return new IRIONullable<V>(constructor.get(), true);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        HashMap<Integer, V> hashVal = new HashMap<Integer, V>();
        R2kUtil.importSparse(hashVal, constructor, bais);
        int maxEnt = -1;
        for (Map.Entry<Integer, V> ent : hashVal.entrySet())
            if (maxEnt < ent.getKey())
                maxEnt = ent.getKey();
        arrVal = new IRIO[maxEnt + 1];
        for (int i = 0; i < arrVal.length; i++) {
            V entVal = hashVal.get(i);
            if (entVal == null) {
                arrVal[i] = newValue();
            } else {
                arrVal[i] = new IRIONullable<V>(entVal, false);
            }
        }
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        // Bypass the type checker... :(
        HashMap<Integer, V> hashVal = new HashMap<Integer, V>();
        for (int i = 0; i < arrVal.length; i++) {
            IRIONullable<V> v = (IRIONullable<V>) arrVal[i];
            if (!v.nulled)
                hashVal.put(i, v.target);
        }
        R2kUtil.exportSparse(hashVal, baos);
        return false;
    }
}
