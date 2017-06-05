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
 * Created on 05/06/17.
 */
public class ByteR2kStruct implements IR2kStruct {
    public byte value;

    public ByteR2kStruct(int v) {
        value = (byte) v;
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setFX(value & 0xFF);
    }

    @Override
    public void fromRIO(RubyIO src) {
        value = (byte) (src.fixnumVal);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        value = (byte) R2kUtil.readLcfU8(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        baos.write(value);
        return false;
    }
}
