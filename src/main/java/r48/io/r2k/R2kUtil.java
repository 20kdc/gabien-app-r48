/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k;

import gabien.ui.ISupplier;
import r48.RubyIO;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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

    public static int readLcfU8(InputStream src) throws IOException {
        int i = src.read();
        if (i < 0)
            throw new IOException("EOF unexpected");
        return i;
    }

    // --

    public static int readLcfS32(InputStream src) throws IOException {
        int b1 = readLcfU8(src);
        int b2 = readLcfU8(src);
        int b3 = readLcfU8(src);
        int b4 = readLcfU8(src);
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    public static void writeLcfS32(OutputStream os, int i) throws IOException {
        ByteBuffer t = ByteBuffer.wrap(new byte[4]);
        t.order(ByteOrder.LITTLE_ENDIAN);
        t.putInt(i);
        os.write(t.array());
    }

    // --

    public static int readLcfU16(InputStream src) throws IOException {
        int b1 = readLcfU8(src);
        int b2 = readLcfU8(src);
        return (b2 << 8) | b1;
    }

    public static void writeLcfU16(OutputStream os, int i) throws IOException {
        ByteBuffer t = ByteBuffer.wrap(new byte[2]);
        t.order(ByteOrder.LITTLE_ENDIAN);
        t.putShort((short) i);
        os.write(t.array());
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

    public static void writeLcfVLI(OutputStream os, int i) throws IOException {
        // Convert to unsigned so this makes sense
        long r = i & 0xFFFFFFFFL;

        LinkedList<Integer> bytes = new LinkedList<Integer>();
        // Get it in reverse (little-endian)...
        while (r > 0x7F) {
            bytes.add((int) (r & 0x7F));
            r >>= 7;
        }
        bytes.add((int) (r & 0x7F));
        Collections.reverse(bytes);
        int bc = bytes.size();
        for (int ib : bytes) {
            if (bc > 1) {
                os.write(ib | 0x80);
                bc--;
            } else {
                os.write(ib);
            }
        }
    }

    // --

    public static byte[] readLcfBytes(InputStream src, int l) throws IOException {
        byte[] data = new byte[l];
        int o = 0;
        while (o < l)
            o += src.read(data, o, l - o);
        return data;
    }

    // For now, assume SHIFT-JIS on all.
    // -- VERIFIED! This is definitely the encoding, check Ib event names
    // Note that this is just for the xIO classes to decode magics.
    // You should NEVER, EVER, *EVER* be decoding these otherwise,
    //  instead passing the binary data directly to RIO
    public static String decodeLcfString(byte[] data) {
        try {
            return new String(data, RubyIO.encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // See above for when to use this
    public static byte[] encodeLcfString(String text) {
        try {
            return text.getBytes(RubyIO.encoding);
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

    public static void rioToUnk(RubyIO mt, HashMap<Integer, byte[]> unknownChunks) {
        mt = mt.getInstVarBySymbol("@__LCF__unknown");
        if (mt != null)
            for (Map.Entry<RubyIO, RubyIO> e : mt.hashVal.entrySet())
                unknownChunks.put((int) e.getKey().fixnumVal, e.getValue().userVal);
    }

    public static ISupplier<byte[]> supplyBlank(final int i, final byte i1) {
        return new ISupplier<byte[]>() {
            @Override
            public byte[] get() {
                byte[] data = new byte[i];
                for (int j = 0; j < i; j++)
                    data[j] = i1;
                return data;
            }
        };
    }
}
