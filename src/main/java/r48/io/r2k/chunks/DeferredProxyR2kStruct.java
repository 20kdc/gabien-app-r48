/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.RubyIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Increases the chance of import/export success by trying to perform the conversion earlier.
 * Single-use, as a result.
 * The flows are: [importData], asRIO
 * and [fromRIO], exportData.
 * Created on February 10th 2018.
 */
public class DeferredProxyR2kStruct implements IR2kStruct {
    private IR2kStruct inner;
    private RubyIO data;

    public DeferredProxyR2kStruct(IR2kStruct in) {
        inner = in;
    }

    @Override
    public RubyIO asRIO() {
        if (inner != null) {
            if (data == null) {
                data = inner.asRIO();
                inner = null;
            } else {
                throw new RuntimeException("Data nn, inner nn?");
            }
        } else if (data == null)
            throw new RuntimeException("Data null, inner null?");
        return data;
    }

    @Override
    public void fromRIO(RubyIO src) {
        if (data != null)
            throw new RuntimeException("Data already exported.");
        if (inner == null)
            throw new RuntimeException("Data already exported.");
        data = src;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        if (data != null)
            throw new RuntimeException("Data already imported.");
        if (inner == null)
            throw new RuntimeException("Data already imported.");
        inner.importData(bais);
        data = inner.asRIO();
        inner = null;
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        if (data != null) {
            inner.fromRIO(data);
            data = null;
        }
        if (inner == null)
            throw new RuntimeException("inner missing.");
        boolean res = inner.exportData(baos);
        inner = null;
        return res;
    }
}
