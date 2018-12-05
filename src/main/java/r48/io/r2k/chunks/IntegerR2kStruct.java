/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * yay, an integer!
 * NOTE: The default values have to be exact due to RMW
 * Created on 31/05/17.
 */
public class IntegerR2kStruct extends IRIOFixed implements IR2kStruct {
    public final int di;
    public int i;

    public IntegerR2kStruct(int i2) {
        super('i');
        i = di = i2;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        i = R2kUtil.readLcfVLI(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        //if (i == di)
        //return true;
        R2kUtil.writeLcfVLI(baos, i);
        return false;
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setFX(i);
    }

    @Override
    public void fromRIO(IRIO src) {
        i = (int) src.getFX();
    }

    @Override
    public long getFX() {
        return i;
    }

    @Override
    public IRIO setFX(long iv) {
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
