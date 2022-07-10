/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.dm2chk;

import gabien.uslx.append.*;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedHash;
import r48.io.data.IRIOFixnum;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Not much can be done about the generics situation here.
 * Created on December 04, 2018.
 */
public class DM2SparseArrayH<V extends IRIO> extends IRIOFixedHash<Integer, V> implements IR2kInterpretable {
    public final ISupplier<V> constructor;

    public DM2SparseArrayH(ISupplier<V> cons) {
        constructor = cons;
    }

    @Override
    public Integer convertIRIOtoKey(IRIO i) {
        return (int) i.getFX();
    }

    @Override
    public IRIO convertKeyToIRIO(Integer i) {
        return new IRIOFixnum(i);
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
