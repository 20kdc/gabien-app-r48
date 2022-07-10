/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import java.io.IOException;
import java.io.InputStream;

import r48.io.data.IRIO;

/**
 * Created on 02/06/17.
 */
public class BooleanR2kStruct extends IntegerR2kStruct {
    public BooleanR2kStruct(boolean i2) {
        super(i2 ? 1 : 0);
        type = i2 ? 'T' : 'F';
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        super.importData(bais);
        type = (i != 0) ? 'T' : 'F';
    }

    @Override
    public IRIO setFX(long iv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getFX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setBool(boolean b) {
        i = b ? 1 : 0;
        type = b ? 'T' : 'F';
        return this;
    }
}
