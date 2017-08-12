/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
    public int bytes = 0;
    // Buffer used to ensure consistency
    public byte[] resultBytes = null;

    public ArraySizeR2kInterpretable() {

    }

    public ArraySizeR2kInterpretable(int b) {
        bytes = b;
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
        switch (bytes) {
            case 0:
                R2kUtil.writeLcfVLI(baos, b2.size());
                break;
            case 1:
                if (b2.size() > 255)
                    throw new IOException("Too big array.");
                baos.write(b2.size());
                break;
            default:
                throw new RuntimeException("unknown B " + bytes);
        }
        return false;
    }
}
