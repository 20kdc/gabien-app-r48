/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.IObjectBackend;
import r48.io.IntUtils;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.*;
import java.util.*;

/**
 * Created on 30/05/17.
 */
public class R2kUtil {

    public final static String userspaceBinder = "Binder::";

    private static ByteArrayInputStream baos(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    private static void baosCleanup(ByteArrayInputStream a) throws IOException {
        if (a.available() > 0)
            throw new IOException("Not all of the data was consumed in the operation.");
    }

    // --

    // --

    // --

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
            int b = IntUtils.readU8(src);
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

    // For now, assume SHIFT-JIS on all.
    // -- VERIFIED! This is definitely the encoding, check Ib event names
    // Note that this is just for the xIO classes to decode magics.
    // You should NEVER, EVER, *EVER* be decoding these otherwise,
    //  instead passing the binary data directly to RIO
    public static String decodeLcfString(byte[] data) {
        try {
            return new String(data, IObjectBackend.Factory.encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // See above for when to use this
    public static byte[] encodeLcfString(String text) {
        try {
            return text.getBytes(IObjectBackend.Factory.encoding);
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
        map.addIVar("@__LCF__unknown", hash);
    }

    public static void rioToUnk(IRIO mt, HashMap<Integer, byte[]> unknownChunks) {
        mt = mt.getIVar("@__LCF__unknown");
        if (mt != null)
            for (IRIO k : mt.getHashKeys())
                unknownChunks.put((int) k.getFX(), mt.getHashVal(k).getBuffer());
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

    public static Index[] mergeIndices(Index[] a, Index[] b) {
        LinkedList<Index> lli = new LinkedList<Index>();
        for (int i = 0; i < a.length; i++)
            lli.add(a[i]);
        for (int i = 0; i < b.length; i++)
            lli.add(b[i]);
        Collections.sort(lli, new Comparator<Index>() {
            @Override
            public int compare(Index index, Index t1) {
                if (index.index < t1.index)
                    return -1;
                if (index.index > t1.index)
                    return 1;
                return 0;
            }
        });
        return lli.toArray(new Index[0]);
    }

    public static void importSparse(HashMap<Integer, ?> map, ISupplier constructor, InputStream bais) throws IOException {
        map.clear();
        int entries = readLcfVLI(bais);
        for (int i = 0; i < entries; i++) {
            int k = readLcfVLI(bais);
            IR2kInterpretable target = (IR2kInterpretable) constructor.get();
            try {
                target.importData(bais);
            } catch (IOException e) {
                throw new IOException("In element " + i, e);
            } catch (RuntimeException e) {
                throw new RuntimeException("In element " + i, e);
            }
            // Incredibly unsafe but callers need this to reduce complexity.
            // One of these warnings vs. many warnings all over the place,
            //  all over nothing.
            ((HashMap) map).put(k, target);
        }
    }

    public static void exportSparse(HashMap<Integer, ?> map, OutputStream baos) throws IOException {
        LinkedList<Integer> sort = new LinkedList<Integer>(map.keySet());
        Collections.sort(sort);
        writeLcfVLI(baos, sort.size());
        for (Integer i : sort) {
            writeLcfVLI(baos, i);
            ((IR2kInterpretable) map.get(i)).exportData(baos);
        }
    }
}
