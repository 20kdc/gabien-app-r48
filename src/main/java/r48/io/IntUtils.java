/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import r48.io.data.IRIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Various functions moved here because IMI automatic cleanup may/will target the r2k package.
 * Handles little-endian values.
 * Created on December 02, 2018.
 */
public class IntUtils {
    public static int readU8(InputStream src) throws IOException {
        int i = src.read();
        if (i < 0)
            throw new IOException("EOF unexpected");
        return i;
    }

    public static int readS32(InputStream src) throws IOException {
        int b1 = readU8(src);
        int b2 = readU8(src);
        int b3 = readU8(src);
        int b4 = readU8(src);
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    public static void writeS32(OutputStream os, int i) throws IOException {
        ByteBuffer t = ByteBuffer.wrap(new byte[4]);
        t.order(ByteOrder.LITTLE_ENDIAN);
        t.putInt(i);
        os.write(t.array());
    }

    public static int readU16(InputStream src) throws IOException {
        int b1 = readU8(src);
        int b2 = readU8(src);
        return (b2 << 8) | b1;
    }

    public static void writeU16(OutputStream os, int i) throws IOException {
        ByteBuffer t = ByteBuffer.wrap(new byte[2]);
        t.order(ByteOrder.LITTLE_ENDIAN);
        t.putShort((short) i);
        os.write(t.array());
    }

    public static byte[] readBytes(InputStream src, int l) throws IOException {
        byte[] data = new byte[l];
        int o = 0;
        while (o < l) {
            int rs = src.read(data, o, l - o);
            if (rs <= 0)
                throw new IOException("EOF");
            o += rs;
        }
        return data;
    }

    public static String decodeRbFloat(byte[] strVal) {
        // Stop at the first null byte.
        int firstNull = strVal.length;
        for (int i = 0; i < strVal.length; i++) {
            if (strVal[i] == 0) {
                firstNull = i;
                break;
            }
        }
        byte[] text = new byte[firstNull];
        System.arraycopy(strVal, 0, text, 0, text.length);
        return new String(text, Charset.forName("UTF-8"));
    }

    public static boolean encodeRbFloat(IRIO target, String text, boolean jsonCoerce) {
        if (jsonCoerce) {
            try {
                long l = Long.parseLong(text);
                target.setFX(l);
                return true;
            } catch (NumberFormatException e) {
            }
        }
        try {
            Double.parseDouble(text);
            target.setFloat(text.getBytes("UTF-8"));
            return true;
        } catch (NumberFormatException e) {
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static void resizeArrayTo(IRIO target, int sizeFixed) {
        while (target.getALen() > sizeFixed)
            target.rmAElem(sizeFixed);
        int alen;
        while ((alen = target.getALen()) < sizeFixed)
            target.addAElem(alen);
    }
}
