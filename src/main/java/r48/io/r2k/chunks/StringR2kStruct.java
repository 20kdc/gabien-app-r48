/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * the difficulty is getting this stuff into memory...
 * (later) and out again.
 * Created on 31/05/17.
 */
public class StringR2kStruct implements IR2kStruct {
    public byte[] data = new byte[0];

    public StringR2kStruct() {
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setString(data);
    }

    @Override
    public void fromRIO(RubyIO src) {
        data = src.strVal;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        data = R2kUtil.readLcfBytes(bais, bais.available());
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        baos.write(data);
        return false;
    }
}
