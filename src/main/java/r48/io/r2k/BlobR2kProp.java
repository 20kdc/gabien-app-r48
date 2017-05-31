/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k;

import java.io.IOException;

/**
 * Uhoh. These are a lot of classes.
 * Created on 31/05/17.
 */
public class BlobR2kProp implements IR2kProp {
    public byte[] dat;

    @Override
    public byte[] getData() throws IOException {
        throw new RuntimeException("incomplete");
    }

    @Override
    public void importData(byte[] data) throws IOException {
        dat = new byte[data.length];
        for (int i = 0; i < data.length; i++)
            dat[i] = data[i];
    }
}
