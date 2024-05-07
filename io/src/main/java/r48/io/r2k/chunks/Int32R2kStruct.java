/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.chunks;

import r48.io.IntUtils;
import r48.io.data.IRIOFixnum;
import r48.io.data.obj.DM2Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 05/06/17.
 */
public class Int32R2kStruct extends IRIOFixnum implements IR2kInterpretable {
    public Int32R2kStruct(DM2Context dm2, int v) {
        super(dm2.dm3, v);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        val = IntUtils.readS32(bais);
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        IntUtils.writeS32(baos, (int) val);
    }
}
