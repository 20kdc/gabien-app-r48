/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.chunks;

import r48.io.IntUtils;
import r48.io.data.IRIOFixedUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

/**
 * Uhoh. These are a lot of classes.
 * Created on 31/05/17.
 */
public class BlobR2kStruct extends IRIOFixedUser implements IR2kInterpretable {
    public BlobR2kStruct(byte[] mkDef) {
        super("Blob", mkDef);
    }
    public BlobR2kStruct(Supplier<byte[]> mkDef) {
        super("Blob", mkDef.get());
    }

    public BlobR2kStruct(String c, byte[] mkDef) {
        super(c, mkDef);
    }
    public BlobR2kStruct(String c, Supplier<byte[]> mkDef) {
        super(c, mkDef.get());
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        userVal = IntUtils.readBytes(bais, bais.available());
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        baos.write(userVal);
    }
}
