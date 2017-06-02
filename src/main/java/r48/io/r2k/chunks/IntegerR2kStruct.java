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
 * yay, an integer!
 * NOTE: The default values have to be exact due to RMW
 * Created on 31/05/17.
 */
public class IntegerR2kStruct implements IR2kStruct {
    public final int di;
    public int i;

    public IntegerR2kStruct(int i2) {
        i = di = i2;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        i = R2kUtil.readLcfVLI(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        if (i == di)
            return true;
        R2kUtil.writeLcfVLI(baos, i);
        return false;
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setFX(i);
    }

    @Override
    public void fromRIO(RubyIO src) {
        i = (int) src.fixnumVal;
    }
}
