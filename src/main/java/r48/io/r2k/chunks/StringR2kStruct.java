/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.IObjectBackend;
import r48.io.IntUtils;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * the difficulty is getting this stuff into memory...
 * (later) and out again.
 * Created on 31/05/17.
 */
public class StringR2kStruct extends IRIOFixed implements IR2kStruct {
    public byte[] data = new byte[0];

    public StringR2kStruct() {
        super('"');
    }

    public StringR2kStruct(byte[] dat) {
        super('"');
        data = dat;
    }

    @Override
    public IRIO setString(String s) {
        try {
            data = s.getBytes(IObjectBackend.Factory.encoding);
            return this;
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException(ue);
        }
    }

    @Override
    public IRIO setString(byte[] s, String jenc) {
        if (jenc.equals(IObjectBackend.Factory.encoding)) {
            data = copyByteArray(s);
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
    public String getBufferEnc() {
        return IObjectBackend.Factory.encoding;
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setString(data, IObjectBackend.Factory.encoding);
    }

    @Override
    public void fromRIO(IRIO src) {
        // This is probably going to last until DataModel2 has taken over a significant part of the code.
        data = src.getBufferInEncoding(IObjectBackend.Factory.encoding);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        data = IntUtils.readBytes(bais, bais.available());
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        baos.write(data);
        return false;
    }
}
