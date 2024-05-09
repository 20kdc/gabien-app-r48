/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import r48.io.data.DMContext;
import r48.io.data.IRIOBoolean;
import r48.io.r2k.R2kUtil;

/**
 * Created on 02/06/17.
 */
public class BooleanR2kStruct extends IRIOBoolean implements IR2kInterpretable {
    public final boolean di;

    public BooleanR2kStruct(DMContext dm2, boolean i2) {
        super(dm2, i2);
        di = i2;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        setBool(R2kUtil.readLcfVLI(bais) != 0);
    }

    @Override
    public boolean canOmitChunk() {
        // return i == di;
        // false always for now
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfVLI(baos, getBool() ? 1 : 0);
    }
}
