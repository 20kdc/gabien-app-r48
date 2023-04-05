/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.chunks;

import r48.io.IntUtils;
import r48.io.data.DM2Context;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * the difficulty is getting this stuff into memory...
 * (later) and out again.
 * Created on 31/05/17.
 */
public class StringR2kStruct extends IRIOFixed implements IR2kInterpretable {
    public byte[] data = new byte[0];
    public final Charset encoding;

    public StringR2kStruct(DM2Context ctx) {
        super('"');
        encoding = ctx.encoding;
    }

    public StringR2kStruct(DM2Context ctx, byte[] dat) {
        super('"');
        encoding = ctx.encoding;
        data = dat;
    }

    @Override
    public IRIO setString(String s) {
        data = s.getBytes(encoding);
        return this;
    }

    @Override
    public IRIO setString(byte[] s, Charset jenc) {
        if (jenc.equals(encoding)) {
            data = s;
            return this;
        }
        return super.setString(s, jenc);
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

    @Override
    public byte[] getBuffer() {
        return data;
    }

    @Override
    public void putBuffer(byte[] dat) {
        data = dat;
    }

    @Override
    public Charset getBufferEnc() {
        return encoding;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        data = IntUtils.readBytes(bais, bais.available());
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        baos.write(data);
    }
}
