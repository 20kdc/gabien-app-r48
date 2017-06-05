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
public class ShortR2kStruct implements IR2kStruct {
    public short value;

    public ShortR2kStruct(int v) {
        value = (short) v;
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setFX(value);
    }

    @Override
    public void fromRIO(RubyIO src) {
        value = (short) (src.fixnumVal);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        value = (short) R2kUtil.readLcfU16(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfU16(baos, value);
        return false;
    }
}
