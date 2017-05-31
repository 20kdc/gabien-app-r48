/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.IFunction;
import r48.io.r2k.R2kUtil;

import java.io.IOException;

/**
 * Created on 31/05/17.
 */
public class ArraySizeR2kProp<T extends IR2kInterpretable> implements IR2kProp {
    public ArrayR2kProp<T> target;

    @Override
    public byte[] getData() throws IOException {
        throw new IOException("incomplete");
    }

    @Override
    public void importData(byte[] data) throws IOException {
        // No need to do anything
    }
}
