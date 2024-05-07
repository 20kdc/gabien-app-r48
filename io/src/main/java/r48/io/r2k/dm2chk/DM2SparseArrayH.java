/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.dm2chk;

import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedHash;
import r48.io.data.RORIO;
import r48.io.data.obj.DM2Context;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

/**
 * Not much can be done about the generics situation here.
 * Created on December 04, 2018.
 */
public class DM2SparseArrayH<V extends IRIO> extends IRIOFixedHash<Integer, V> implements IR2kInterpretable {
    public final Supplier<V> constructor;

    public DM2SparseArrayH(DM2Context dm2, Supplier<V> cons) {
        super(dm2.dm3);
        constructor = cons;
    }

    @Override
    public Integer convertIRIOtoKey(RORIO i) {
        return (int) i.getFX();
    }

    @Override
    public DMKey convertKeyToIRIO(Integer i) {
        return DMKey.of(i);
    }

    @Override
    public V newValue() {
        return constructor.get();
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        R2kUtil.importSparse(hashVal, constructor, bais);
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        // Bypass the type checker... :(
        R2kUtil.exportSparse(hashVal, baos);
    }
}
