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

// RECOMMENDED FOR DEBUGGING (of R48 or other software) ONLY!
// Usage outside of debugging is probably not a good idea.
public class SkipperR2kStruct implements IR2kStruct {
    public IR2kStruct inter;
    public int bcount;
    public SkipperR2kStruct(IR2kStruct internal, int bytes) {
        inter = internal;
        bcount = bytes;
    }

    @Override
    public RubyIO asRIO() {
        return inter.asRIO();
    }

    @Override
    public void fromRIO(RubyIO src) {
        inter.fromRIO(src);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        bais.skip(bcount);
        inter.importData(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        throw new IOException("This contains a debug 'Skipper' solely meant for analysis of broken LCF data.");
    }
}
