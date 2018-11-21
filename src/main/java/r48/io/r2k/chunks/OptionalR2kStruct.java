/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.data.IRIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wraps optional things.
 * Created on 01/06/17.
 */
public class OptionalR2kStruct<T extends IR2kStruct> implements IR2kStruct {
    public T instance;
    public ISupplier<T> constructor;

    public OptionalR2kStruct(ISupplier<T> s) {
        constructor = s;
    }

    @Override
    public RubyIO asRIO() {
        if (instance == null)
            return null;
        return instance.asRIO();
    }

    @Override
    public void fromRIO(IRIO src) {
        if (src == null) {
            instance = null;
        } else {
            instance = constructor.get();
            instance.fromRIO(src);
        }
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        instance = constructor.get();
        instance.importData(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        if (instance == null)
            return true;
        instance.exportData(baos);
        return false;
    }
}
