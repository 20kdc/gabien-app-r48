/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io;

import gabien.GaBIEn;
import gabienapp.Application;
import r48.RubyIO;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created on 1/27/17.
 */
public class R48ObjectBackend implements IObjectBackend {
    private final String prefix, postfix;
    // should be true unless otherwise needed
    public final boolean assumeDAG;

    public R48ObjectBackend(String s, String dataExt, boolean aDAG) {
        prefix = s;
        postfix = dataExt;
        assumeDAG = aDAG;
    }

    public static long load32(DataInputStream dis) throws IOException {
        byte b = dis.readByte();
        // Special values
        if (b == 0)
            return 0;
        if (b == 1)
            return dis.readUnsignedByte();
        if (b == -1)
            return -(256 - dis.readUnsignedByte());
        if (b == 2)
            return load16LE(dis);
        if (b == -2)
            return -(65536 - load16LE(dis));
        if (b == 3)
            return load24LE(dis);
        if (b == -3)
            return -(0x1000000L - load24LE(dis));
        if (b == 4)
            return load32LE(dis);
        if (b == -4)
            return -(0x100000000L - load32LE(dis));
        long r = b; // Sign-extend
        if (r >= 0) {
            r -= 5;
        } else {
            r += 5;
        }
        return r;
    }

    private static long load32LE(DataInputStream dis) throws IOException {
        long a = load16LE(dis);
        return a | (load16LE(dis) << 16);
    }

    private static long load24LE(DataInputStream dis) throws IOException {
        long a = load16LE(dis);
        return a | (dis.readUnsignedByte() << 16);
    }

    private static long load16LE(DataInputStream dis) throws IOException {
        long a = dis.readUnsignedByte();
        return a | (dis.readUnsignedByte() << 8);
    }

    public static void save32(DataOutputStream dis, long v) throws IOException {
        if (v == 0) {
            dis.write(0);
            return;
        }
        /*
        if (v < 123)
            if (v >= 0) {
                // Positive
                dis.write((int) (v + 5));
                return;
            }*/

        // Maybe handle the one-bytes at some point (but not now)
        boolean neg = false;
        if (v < 0)
            neg = true;

        int b = 4;
        if (neg) {
            if (v >= -0x1000000)
                b = 3;
            if (v >= -0x10000)
                b = 2;
            if (v >= -0x100)
                b = 1;
            dis.write(-b);
        } else {
            if (v < 0x1000000)
                b = 3;
            if (v < 0x10000)
                b = 2;
            if (v < 0x100)
                b = 1;
            dis.write(b);
        }
        save32LE(dis, v, b);
    }

    private static void save32LE(DataOutputStream dis, long v, int bytes) throws IOException {
        if (bytes > 0)
            dis.write((int) (v & 0xFF));
        if (bytes > 1)
            dis.write((int) ((v >> 8) & 0xFF));
        if (bytes > 2)
            dis.write((int) ((v >> 16) & 0xFF));
        if (bytes > 3)
            dis.write((int) ((v >> 24) & 0xFF));
    }

    @Override
    public RubyIO loadObjectFromFile(String filename) {
        try {
            InputStream inp = GaBIEn.getFile(Application.autoDetectWindows(prefix + filename + postfix));
            if (inp == null)
                return null;
            DataInputStream dis = new DataInputStream(inp);
            // Marshal v4.8
            if (dis.readUnsignedByte() != 0x04)
                throw new IOException("mgk[0]!=0x04");
            if (dis.readUnsignedByte() != 0x08)
                throw new IOException("mgk[1]!=0x08");
            LinkedList<RubyIO> objCache = new LinkedList<RubyIO>();
            LinkedList<String> strCache = new LinkedList<String>();
            RubyIO rio = loadValue(dis, objCache, strCache);
            dis.close();
            return rio;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    @Override
    public void saveObjectToFile(String filename, RubyIO object) throws IOException {
        OutputStream oup = GaBIEn.getOutFile(Application.autoDetectWindows(prefix + filename + postfix));
        if (oup == null)
            throw new IOException("Unable to open file!");
        DataOutputStream dis = new DataOutputStream(oup);
        // Marshal v4.8
        dis.write(new byte[] {4, 8});
        LinkedList<RubyIO> objCache = new LinkedList<RubyIO>();
        LinkedList<String> strCache = new LinkedList<String>();
        saveValue(dis, object, objCache, strCache);
        dis.close();
    }

    private static void saveSymbol(DataOutputStream dis, String sym, LinkedList<String> strCache) throws IOException {
        int symInd = strCache.indexOf(sym);
        if (symInd >= 0) {
            dis.write((int) ';');
            save32(dis, symInd);
        } else {
            dis.write((int) ':');
            byte[] d = sym.getBytes("UTF-8");
            save32(dis, d.length);
            dis.write(d);
            strCache.add(sym);
        }
    }

    private static void saveValue(DataOutputStream dis, RubyIO rio, LinkedList<RubyIO> objCache, LinkedList<String> strCache) throws IOException {
        // Deduplicatables
        int b = rio.type;
        int objIndex = objCache.indexOf(rio);
        if (objIndex != -1) {
            // Seen this object before.
            dis.write((int) '@');
            save32(dis, objIndex);
            return;
        }
        if (b == ':') {
            saveSymbol(dis, rio.symVal, strCache);
            return;
        }
        // Everything else.
        // Firstly, pre-process (iVars wrapping)
        boolean ivarData = rio.iVars.size() != 0;
        boolean shouldWriteObjCacheLate = false;
        boolean okay = false;
        if (b == 'o')
            ivarData = false;
        if (ivarData)
            dis.write((int) 'I');
        // -- Actually write stuff
        dis.write(b);
        if (b == 'o') {
            objCache.add(rio);
            saveSymbol(dis, rio.symVal, strCache);
            saveIVarsCore(dis, rio.iVars, objCache, strCache);
            okay = true;
        }
        if (b == '{') {
            objCache.add(rio);
            saveHashCore(dis, rio.hashVal, objCache, strCache);
            okay = true;
        }
        if (b == '}') {
            objCache.add(rio);
            saveHashCore(dis, rio.hashVal, objCache, strCache);
            saveValue(dis, rio.hashDefVal, objCache, strCache);
            okay = true;
        }
        if (b == '[') {
            objCache.add(rio);
            save32(dis, rio.arrVal.length);
            for (int i = 0; i < rio.arrVal.length; i++)
                saveValue(dis, rio.arrVal[i], objCache, strCache);
            okay = true;
        }
        if (b == 'i') {
            save32(dis, rio.fixnumVal);
            okay = true;
        }
        if (b == '"') {
            objCache.add(rio);
            save32(dis, rio.strVal.length);
            dis.write(rio.strVal);
            okay = true;
        }
        if (b == 'f') {
            objCache.add(rio);
            save32(dis, rio.strVal.length);
            dis.write(rio.strVal);
            okay = true;
        }
        if (b == 'u') {
            shouldWriteObjCacheLate = true;
            saveSymbol(dis, rio.symVal, strCache);
            save32(dis, rio.userVal.length);
            dis.write(rio.userVal);
            okay = true;
        }
        if (b == 'T')
            okay = true;
        if (b == 'F')
            okay = true;
        if (b == '0')
            okay = true;
        if (!okay)
            throw new IOException("Cannot save " + ((char) rio.type));
        if (ivarData)
            saveIVarsCore(dis, rio.iVars, objCache, strCache);
        if (shouldWriteObjCacheLate)
            objCache.add(rio);
    }

    private static void saveHashCore(DataOutputStream dis, HashMap<RubyIO, RubyIO> iVars, LinkedList<RubyIO> objCache, LinkedList<String> strCache) throws IOException {
        Set<Map.Entry<RubyIO, RubyIO>> se = iVars.entrySet();
        // damned if you do (IDE warning), damned if you don't (compiler warning).
        Map.Entry<RubyIO, RubyIO>[] me = se.toArray(new Map.Entry[0]);
        save32(dis, me.length);
        for (Map.Entry<RubyIO, RubyIO> e : me) {
            saveValue(dis, e.getKey(), objCache, strCache);
            saveValue(dis, e.getValue(), objCache, strCache);
        }
    }

    private static void saveIVarsCore(DataOutputStream dis, HashMap<String, RubyIO> iVars, LinkedList<RubyIO> objCache, LinkedList<String> strCache) throws IOException {
        Set<Map.Entry<String, RubyIO>> se = iVars.entrySet();
        // damned if you do (IDE warning), damned if you don't (compiler warning).
        Map.Entry<String, RubyIO>[] me = se.toArray(new Map.Entry[0]);
        save32(dis, me.length);
        for (Map.Entry<String, RubyIO> e : me) {
            RubyIO key = new RubyIO();
            key.type = ':';
            key.symVal = e.getKey();
            saveValue(dis, key, objCache, strCache);
            saveValue(dis, e.getValue(), objCache, strCache);
        }
    }

    private RubyIO loadValue(DataInputStream dis, LinkedList<RubyIO> objs, LinkedList<String> syms) throws IOException {
        int b = dis.readUnsignedByte();
        RubyIO rio = new RubyIO();
        // r_entry0 is responsible for adding into the object cache.
        boolean handlingInstVars = false;
        boolean shouldWriteObjCacheLate = false;
        // Only handle 'I' once, otherwise error, for sanity purposes
        if (b == 'I') {
            b = dis.readUnsignedByte();
            handlingInstVars = true;
        }
        rio.type = b;
        // "nocareivar" means it will run Ivar stuff if the I prefix is given in theory
        if (b == 'o') {
            // 1889 runs entry before iVars, nocareivar.
            objs.add(rio);
            rio.symVal = loadValue(dis, objs, syms).symVal;
            if (handlingInstVars)
                throw new IOException("Can't stack instance variables");
            handlingInstVars = true;
        } else if (b == '@') {
            // 1574: No special handling at all. No entry, nocareivar.
            // THE DOCUMENTS LIED, NO -1
            // additional note:
            // Yes I am breaking spec by automatically cloning things,
            //  in context this actually makes more sense than NOT,
            //  because of some assumptions made by the Schema system.
            // How did I not think of this EARLIER?
            if (assumeDAG) {
                rio.setDeepClone(objs.get((int) load32(dis)));
            } else {
                rio = objs.get((int) load32(dis));
            }
        } else if ((b == '{') || (b == '}')) {
            // 1772: Runs entry first thing after creating the hash, nocareivar
            objs.add(rio);
            rio.hashVal = new HashMap<RubyIO, RubyIO>();
            long vars = load32(dis);
            for (long i = 0; i < vars; i++) {
                RubyIO k = loadValue(dis, objs, syms);
                RubyIO v = loadValue(dis, objs, syms);
                rio.hashVal.put(k, v);
            }
            if (b == '}')
                rio.hashDefVal = loadValue(dis, objs, syms);
        } else if (b == '[') {
            // 1756: Runs entry first thing after creating the array, nocareivar
            objs.add(rio);
            rio.arrVal = new RubyIO[(int) load32(dis)];
            for (long i = 0; i < rio.arrVal.length; i++)
                rio.arrVal[(int) i] = loadValue(dis, objs, syms);
        } else if (b == ':') {
            // 1957, Never performs an entry, explicitly cancels out iVar
            handlingInstVars = false;
            long len = load32(dis);
            byte[] data = new byte[(int) len];
            if (dis.read(data) != len)
                throw new IOException("Didn't read all of data");
            rio.symVal = new String(data, Charset.forName("UTF-8"));
            syms.add(rio.symVal);
        } else if (b == ';') {
            // 1969, Never performs an entry, nocareivar
            rio.type = ':';
            rio.symVal = syms.get((int) load32(dis));
        } else if (b == 'i') {
            // Never performs an entry, nocareivar
            rio.fixnumVal = load32(dis);
        } else if (b == '"') {
            // 1715, nocareivar, just runs entry.
            objs.add(rio);
            long len = load32(dis);
            byte[] data = new byte[(int) len];
            if (dis.read(data) != len)
                throw new IOException("Didn't read all of data");
            rio.strVal = data;
        } else if (b == 'f') {
            objs.add(rio);
            long len = load32(dis);
            byte[] data = new byte[(int) len];
            if (dis.read(data) != len)
                throw new IOException("Didn't read all of data");
            rio.strVal = data;
        } else if (b == 'u') {
            // 1832, performs ivars before entry.
            shouldWriteObjCacheLate = true;
            rio.symVal = loadValue(dis, objs, syms).symVal;
            rio.userVal = new byte[(int) load32(dis)];
            if (dis.read(rio.userVal) != rio.userVal.length)
                throw new IOException("Interrupted Userval");
            // The following 3 just don't add themselves to the object table
        } else if (b == 'T') {
        } else if (b == 'F') {
        } else if (b == '0') {
        } else {
            throw new IOException("Unknown Marshal " + ((char) b));
        }
        if (handlingInstVars) {
            long vars = load32(dis);
            for (long i = 0; i < vars; i++) {
                RubyIO k = loadValue(dis, objs, syms);
                RubyIO v = loadValue(dis, objs, syms);
                rio.iVars.put(k.symVal, v);
            }
        }
        if (shouldWriteObjCacheLate)
            objs.add(rio);
        return rio;
    }

}
