/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

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
    public ArrayR2kInterpretable<T> target;

    // This does... basically nothing

    @Override
    public void importData(InputStream bais) throws IOException {
        R2kUtil.readLcfVLI(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        // currently relies on the output being consistent :(
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        target.exportData(b2);
        R2kUtil.writeLcfVLI(baos, b2.size());
        return false;
    }
}
