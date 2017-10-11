/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.io.r2k.R2kUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * makes no sense for this to be a Struct
 * Created on 31/05/17.
 */
public class ArraySizeR2kInterpretable<T extends IR2kInterpretable> implements IR2kInterpretable {
    public ISupplier<ArrayR2kInterpretable<T>> target;
    public boolean unitSize;

    public int bytes = 0;
    // Buffer used to ensure consistency
    public byte[] resultBytes = null;

    public ArraySizeR2kInterpretable() {
    }

    public ArraySizeR2kInterpretable(int b) {
        bytes = b;
    }

    public ArraySizeR2kInterpretable(boolean us) {
        unitSize = us;
    }

    public ArraySizeR2kInterpretable(int b, boolean us) {
        bytes = b;
        unitSize = us;
    }

    // This does... basically nothing

    @Override
    public void importData(InputStream bais) throws IOException {
        switch (bytes) {
            case 0:
                R2kUtil.readLcfVLI(bais);
                break;
            case 1:
                R2kUtil.readLcfU8(bais);
                break;
            default:
                throw new RuntimeException("unknown B " + bytes);
        }
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        ArrayR2kInterpretable<T> targ = target.get();
        if (targ == null)
            return true;
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        resultBytes = null;
        targ.exportData(b2);
        resultBytes = b2.toByteArray();
        int v = b2.size();
        if (unitSize)
            v = targ.array.size();
        switch (bytes) {
            case 0:
                R2kUtil.writeLcfVLI(baos, v);
                break;
            case 1:
                if (v > 255)
                    throw new IOException("Too big array.");
                baos.write(v);
                break;
            default:
                throw new RuntimeException("unknown B " + bytes);
        }
        return false;
    }
}
