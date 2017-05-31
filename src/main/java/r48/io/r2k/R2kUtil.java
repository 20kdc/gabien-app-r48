/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k;

import r48.RubyIO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 30/05/17.
 */
public class R2kUtil {

    private static ByteArrayInputStream baos(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    private static void baosCleanup(ByteArrayInputStream a) throws IOException {
        if (a.available() > 0)
            throw new IOException("Not all of the data was consumed in the operation.");
    }

    // --

    public static int readLcfU8(byte[] data) throws IOException {
        ByteArrayInputStream a = baos(data);
        int r = readLcfU8(a);
        baosCleanup(a);
        return r;
    }

    public static int readLcfU8(InputStream src) throws IOException {
        int i = src.read();
        if (i < 0)
            throw new IOException("EOF unexpected");
        return i;
    }

    // --

    public static int readLcfS32(byte[] data) throws IOException {
        ByteArrayInputStream a = baos(data);
        int r = readLcfS32(a);
        baosCleanup(a);
        return r;
    }

    public static int readLcfS32(InputStream src) throws IOException {
        int b1 = readLcfU8(src);
        int b2 = readLcfU8(src);
        int b3 = readLcfU8(src);
        int b4 = readLcfU8(src);
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    // --

    public static int readLcfU16(byte[] data) throws IOException {
        ByteArrayInputStream a = baos(data);
        int r = readLcfU16(a);
        baosCleanup(a);
        return r;
    }

    public static int readLcfU16(InputStream src) throws IOException {
        int b1 = readLcfU8(src);
        int b2 = readLcfU8(src);
        return (b2 << 8) | b1;
    }

    // --

    public static short readLcfS16(byte[] data) throws IOException {
        ByteArrayInputStream a = baos(data);
        short r = readLcfS16(a);
        baosCleanup(a);
        return r;
    }

    public static short readLcfS16(InputStream src) throws IOException {
        int b1 = readLcfU8(src);
        int b2 = readLcfU8(src);
        return (short) ((b2 << 8) | b1);
    }

    // --

    public static int readLcfVLI(byte[] data) throws IOException {
        ByteArrayInputStream a = baos(data);
        int r = readLcfVLI(a);
        baosCleanup(a);
        return r;
    }

    public static int readLcfVLI(InputStream src) throws IOException {
        int v = 0;
        while (true) {
            int b = readLcfU8(src);
            v = v << 7;
            v |= b & 0x7F;
            if ((b & 0x80) == 0)
                break;
        }
        return v;
    }

    // --

    public static byte[] readLcfBytes(InputStream src, int l) throws IOException {
        byte[] data = new byte[l];
        int o = src.read(data);
        while (o < data.length)
            o += src.read(data, o, data.length - o);
        return data;
    }

    // For now, assume SHIFT-JIS on all.
    // -- VERIFIED! This is definitely the encoding, check ib event names
    public static String decodeLcfString(byte[] data) {
        try {
            return new String(data, "SJIS");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // --

    public static void unkToRio(RubyIO map, HashMap<Integer, byte[]> unknownChunks) {
        if (unknownChunks.isEmpty())
            return;
        RubyIO hash = new RubyIO();
        hash.setHash();
        for (Map.Entry<Integer, byte[]> e : unknownChunks.entrySet())
            hash.hashVal.put(new RubyIO().setFX(e.getKey()), new RubyIO().setUser("Blob", e.getValue()));
        map.iVars.put("@__LCF__unknown", hash);
    }
}
