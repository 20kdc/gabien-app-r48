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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import gabien.uslx.io.ByteArrayMemoryish;
import gabien.uslx.io.MemoryishR;

public class DoubleR2kStruct extends IRIOFixedData implements IR2kInterpretable {
    private double v;

    public DoubleR2kStruct(DMContext dm2) {
        super(dm2, 'f');
    }

    public DoubleR2kStruct(DMContext dm2, double v) {
        this(dm2);
        this.v = v;
    }

    public DoubleR2kStruct(DMContext dm2, int v) {
        this(dm2, (double) v);
    }

    @Override
    public Runnable saveState() {
        final double saved = v;
        return () -> v = saved;
    }

    @Override
    public IRIO setFloat(byte[] s) {
        trackingWillChange();
        v = Double.parseDouble(IntUtils.decodeRbFloat(s));
        return this;
    }

    @Override
    public MemoryishR getBuffer() {
        return new ByteArrayMemoryish(getBufferCopy());
    }

    @Override
    public byte[] getBufferCopy() {
        try {
            return Double.toString(v).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decString() {
        return Double.toString(v);
    }

    @Override
    public Charset getBufferEnc() {
        return StandardCharsets.UTF_8;
    }

    @Override
    public void putBuffer(byte[] data) {
        trackingWillChange();
        v = Double.parseDouble(IntUtils.decodeRbFloat(data));
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        byte[] data = new byte[8];
        if (bais.read(data) != 8)
            throw new IOException("Didn't get whole double");
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        swap(data, 7, 0);
        swap(data, 6, 1);
        swap(data, 5, 2);
        swap(data, 4, 3);
        v = dis.readDouble();
    }

    private void swap(byte[] data, int i, int i1) {
        byte p = data[i];
        data[i] = data[i1];
        data[i1] = p;
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        ByteArrayOutputStream preswap = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(preswap);
        dos.writeDouble(v);
        byte[] data = preswap.toByteArray();
        swap(data, 7, 0);
        swap(data, 6, 1);
        swap(data, 5, 2);
        swap(data, 4, 3);
        baos.write(data);
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
