/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;
import r48.schema.FloatSchemaElement;

import java.io.*;

public class DoubleR2kStruct extends IRIOFixed implements IR2kStruct {
    public double v;

    public DoubleR2kStruct() {
        super('f');
    }

    public DoubleR2kStruct(double v) {
        this();
        this.v = v;
    }

    @Override
    public IRIO setFloat(byte[] s) {
        v = Double.parseDouble(FloatSchemaElement.decodeVal(s));
        return this;
    }

    @Override
    public byte[] getBuffer() {
        try {
            return Double.toString(v).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putBuffer(byte[] data) {
        v = Double.parseDouble(FloatSchemaElement.decodeVal(data));
    }

    @Override
    public String decString() {
        return super.decString();
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setDeepClone(this);
    }

    @Override
    public void fromRIO(IRIO src) {
        setDeepClone(src);
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
    public boolean exportData(OutputStream baos) throws IOException {
        ByteArrayOutputStream preswap = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(preswap);
        dos.writeDouble(v);
        byte[] data = preswap.toByteArray();
        swap(data, 7, 0);
        swap(data, 6, 1);
        swap(data, 5, 2);
        swap(data, 4, 3);
        baos.write(data);
        return false;
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
