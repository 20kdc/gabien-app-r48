/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Uhoh. These are a lot of classes.
 * Created on 31/05/17.
 */
public class BlobR2kStruct implements IR2kStruct {
    public byte[] dat;

    public BlobR2kStruct(ISupplier<byte[]> mkDef) {
        dat = mkDef.get();
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setUser("Blob", dat);
    }

    @Override
    public void fromRIO(RubyIO src) {
        dat = src.userVal;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        dat = R2kUtil.readLcfBytes(bais, bais.available());
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        baos.write(dat);
        return false;
    }
}
