/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.io.IntUtils;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 05/06/17.
 */
public class ByteR2kStruct extends IRIOFixed implements IR2kInterpretable {
    public byte value;
    public boolean signed = false;

    public ByteR2kStruct(int v) {
        super('i');
        value = (byte) v;
    }

    public ByteR2kStruct() {
        this(0);
    }

    @Override
    public IRIO setFX(long fx) {
        value = (byte) fx;
        return this;
    }

    @Override
    public long getFX() {
        if (!signed)
            return value & 0xFF;
        return value;
    }

    public ByteR2kStruct signed() {
        signed = true;
        return this;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        value = (byte) IntUtils.readU8(bais);
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        baos.write(value);
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
