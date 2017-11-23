/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.RubyIO;

import java.io.*;

public class DoubleR2kStruct implements IR2kStruct {
    public double v = 0;

    @Override
    public RubyIO asRIO() {
        RubyIO d = new RubyIO();
        d.type = 'f';
        d.setString(Double.toString(v));
        return d;
    }

    @Override
    public void fromRIO(RubyIO src) {
        v = Double.valueOf(src.decString());
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
}
