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
import java.nio.charset.Charset;

import gabien.uslx.io.ByteArrayMemoryish;
import gabien.uslx.io.MemoryishR;

/**
 * the difficulty is getting this stuff into memory...
 * (later) and out again.
 * Created on 31/05/17.
 */
public class StringR2kStruct extends IRIOFixedData implements IR2kInterpretable {
    private byte[] data;
    private ByteArrayMemoryish dataBAM;
    private String dataDecoded;
    private final Charset encoding;

    public StringR2kStruct(DMContext ctx) {
        super(ctx, '"');
        encoding = ctx.encoding;
        data = new byte[0];
        dataBAM = new ByteArrayMemoryish(data);
        dataDecoded = "";
    }

    public StringR2kStruct(DMContext ctx, byte[] dat) {
        super(ctx, '"');
        encoding = ctx.encoding;
        data = dat;
        dataBAM = new ByteArrayMemoryish(data);
        dataDecoded = new String(data, encoding);
    }

    @Override
    public Runnable saveState() {
        final byte[] saved = data;
        final ByteArrayMemoryish savedBAM = dataBAM;
        final String savedDecoded = dataDecoded;
        return () -> {
            data = saved;
            dataBAM = savedBAM;
            dataDecoded = savedDecoded;
        };
    }

    @Override
    public IRIO setString(String s) {
        putBuffer(s.getBytes(encoding));
        return this;
    }

    @Override
    public IRIO setString(byte[] s, Charset jenc) {
        if (jenc.equals(encoding)) {
            putBuffer(s);
            return this;
        }
        return super.setString(s, jenc);
    }

    @Override
    public void putBuffer(byte[] dat) {
        trackingWillChange();
        data = dat;
        dataBAM = new ByteArrayMemoryish(data);
        dataDecoded = new String(dat, encoding);
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
    public MemoryishR getBuffer() {
        return dataBAM;
    }

    @Override
    public String decString() {
        return dataDecoded;
    }

    @Override
    public byte[] getBufferCopy() {
        return data.clone();
    }

    @Override
    public Charset getBufferEnc() {
        return encoding;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        putBuffer(IntUtils.readBytes(bais, bais.available()));
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
