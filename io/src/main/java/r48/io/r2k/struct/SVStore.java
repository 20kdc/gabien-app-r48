/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.struct;

import r48.io.IntUtils;
import r48.io.data.obj.DM2Context;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 05/06/17.
 */
public class SVStore extends StringR2kStruct {
    public SVStore(DM2Context dm2c) {
        super(dm2c);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        while (true) {
            int idx = R2kUtil.readLcfVLI(bais);
            if (idx == 0)
                break;
            int len = R2kUtil.readLcfVLI(bais);
            byte[] data = IntUtils.readBytes(bais, len);
            if (idx == 1) {
                this.data = data;
            } else {
                System.err.println("UNKNOWN SVStore CHUNK: " + idx);
            }
        }
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfVLI(baos, 1);
        R2kUtil.writeLcfVLI(baos, data.length);
        baos.write(data);
        baos.write(0);
    }
}
