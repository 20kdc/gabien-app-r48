/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.r2k.R2kUtil;

import java.io.IOException;

/**
 * the difficulty is getting this stuff into memory...
 * Created on 31/05/17.
 */
public class StringR2kProp implements IR2kProp {
    public byte[] data = new byte[0];

    public StringR2kProp() {
    }

    @Override
    public byte[] getData() throws IOException {
        throw new RuntimeException("incomplete");
    }

    @Override
    public void importData(byte[] ndata) throws IOException {
        data = ndata;
    }
}
