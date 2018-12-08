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
public class ShortR2kStruct extends IRIOFixed implements IR2kInterpretable {
    public short value;

    public ShortR2kStruct(int v) {
        super('i');
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
    public boolean exportData(OutputStream baos) throws IOException {
        IntUtils.writeU16(baos, value);
        return false;
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
