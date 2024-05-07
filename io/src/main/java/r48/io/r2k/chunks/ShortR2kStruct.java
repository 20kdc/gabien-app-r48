/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.chunks;

import r48.io.IntUtils;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;
import r48.io.data.obj.DM2Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 05/06/17.
 */
public class ShortR2kStruct extends IRIOFixed implements IR2kInterpretable {
    public short value;

    public ShortR2kStruct(DM2Context dm2, int v) {
        super(dm2.dm3, 'i');
        value = (short) v;
    }

    @Override
    public long getFX() {
        return value;
    }

    @Override
    public IRIO setFX(long fx) {
        value = (short) fx;
        return this;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        value = (short) IntUtils.readU16(bais);
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        IntUtils.writeU16(baos, value);
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
