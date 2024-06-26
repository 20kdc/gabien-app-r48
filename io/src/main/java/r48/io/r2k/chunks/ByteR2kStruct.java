/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.chunks;

import r48.io.IntUtils;
import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Created on 05/06/17.
 */
public class ByteR2kStruct extends IRIOFixedData implements IR2kInterpretable {
    protected byte value;

    public ByteR2kStruct(@NonNull DMContext context, int v) {
        super(context, 'i');
        value = (byte) v;
    }

    public ByteR2kStruct(@NonNull DMContext context) {
        this(context, 0);
    }

    @Override
    public Runnable saveState() {
        final byte saved = value;
        return () -> value = saved;
    }

    @Override
    public IRIO setFX(long fx) {
        trackingWillChange();
        value = (byte) fx;
        return this;
    }

    @Override
    public long getFX() {
        return value & 0xFF;
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

    /**
     * Created to enable the signed option without custom factories...
     */
    public static class Signed extends ByteR2kStruct {
        public Signed(@NonNull DMContext context, int v) {
            super(context, v);
        }

        public Signed(@NonNull DMContext context) {
            super(context);
        }

        @Override
        public long getFX() {
            return value;
        }
    }
}
