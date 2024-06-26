/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k;

import r48.io.IntUtils;
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

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
    public static String decodeLcfString(DMContext dm2c, byte[] data) {
        return new String(data, dm2c.encoding);
    }

    // See above for when to use this
    public static byte[] encodeLcfString(DMContext dm2c, String text) {
        return text.getBytes(dm2c.encoding);
    }

    // --

    public static void unkToRio(IRIO map, HashMap<Integer, byte[]> unknownChunks) {
        if (unknownChunks.isEmpty())
            return;
        IRIO hash = map.addIVar("@__LCF__unknown");
        hash.setHash();
        for (Map.Entry<Integer, byte[]> e : unknownChunks.entrySet()) {
            hash.addHashVal(DMKey.of(e.getKey())).setUser("Blob", e.getValue());
        }
    }

    public static void rioToUnk(IRIO mt, HashMap<Integer, byte[]> unknownChunks) {
        mt = mt.getIVar("@__LCF__unknown");
        if (mt != null)
            for (DMKey k : mt.getHashKeys())
                unknownChunks.put((int) k.getFX(), mt.getHashVal(k).getBufferCopy());
    }

    public static Supplier<byte[]> supplyBlank(final int i, final byte i1) {
        return new Supplier<byte[]>() {
            @Override
            public byte[] get() {
                byte[] data = new byte[i];
                for (int j = 0; j < i; j++)
                    data[j] = i1;
                return data;
            }
        };
    }

    public static <T> void importSparse(HashMap<Integer, T> map, Supplier<T> constructor, InputStream bais) throws IOException {
        map.clear();
        int entries = readLcfVLI(bais);
        for (int i = 0; i < entries; i++) {
            int k = readLcfVLI(bais);
            T target = constructor.get();
            try {
                ((IR2kInterpretable) target).importData(bais);
            } catch (IOException e) {
                throw new IOException("In element " + i, e);
            } catch (RuntimeException e) {
                throw new RuntimeException("In element " + i, e);
            }
            // Incredibly unsafe but callers need this to reduce complexity.
            // One of these warnings vs. many warnings all over the place,
            //  all over nothing.
            map.put(k, target);
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
