/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * the difficulty is getting this stuff into memory...
 * (later) and out again.
 * Created on 31/05/17.
 */
public class StringR2kStruct implements IR2kStruct {
    public byte[] data = new byte[0];

    public StringR2kStruct() {
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setString(data);
    }

    @Override
    public void fromRIO(RubyIO src) {
        data = src.strVal;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        data = R2kUtil.readLcfBytes(bais, bais.available());
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        baos.write(data);
        return false;
    }
}
