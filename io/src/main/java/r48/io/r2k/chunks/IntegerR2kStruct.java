/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.chunks;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedData;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * yay, an integer!
 * NOTE: The default values have to be exact due to RMW
 * Created on 31/05/17.
 */
public class IntegerR2kStruct extends IRIOFixedData implements IR2kInterpretable {
    private int i;

    public IntegerR2kStruct(DMContext dm2, int i2) {
        super(dm2, 'i');
        i = i2;
    }

    @Override
    public Runnable saveState() {
        final int saved = i;
        return () -> i = saved;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        i = R2kUtil.readLcfVLI(bais);
    }

    @Override
    public boolean canOmitChunk() {
        // return i == di;
        // false always for now
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfVLI(baos, i);
    }

    @Override
    public long getFX() {
        return i;
    }

    @Override
    public IRIO setFX(long iv) {
        trackingWillChange();
        i = (int) iv;
        return this;
    }

    @Override
    public String[] getIVars() {
        return new String[0];
    }

    @Override
    public IRIO addIVar(String sym) {
        return null;
    }

    @Override
    public IRIO getIVar(String sym) {
        return null;
    }
}
