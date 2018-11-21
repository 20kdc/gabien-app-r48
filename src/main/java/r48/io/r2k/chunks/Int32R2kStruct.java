/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.data.IRIO;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 05/06/17.
 */
public class Int32R2kStruct implements IR2kStruct {
    public int value;

    public Int32R2kStruct(int v) {
        value = v;
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setFX(value);
    }

    @Override
    public void fromRIO(IRIO src) {
        value = (int) (src.getFX());
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        value = R2kUtil.readLcfS32(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfS32(baos, value);
        return false;
    }
}
