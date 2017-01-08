/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * 'Dear [REDACTED].
 *  I have recently learned a valuable lesson about friendship.
 *  Specifically, that if you are friends with someone who uses Ruby Marshal
 *  as anything but a temporary serialization mechanism, and I emphasize TEMPORARY,
 *  maybe reconsidering your friendships is a good idea.'
 * Thankfully, I wasn't ever friends with [REDACTED] to begin with, since they're a company.
 * Not a person.
 * I pity the fool who downloads the first poisoned [NAME HERE] savefile.
 * Created on 12/27/16.
 */
public class RubyIO {
    public int type;
    public byte[] strVal; // actual meaning depends on iVars. Should be treated as immutable - replace strVal on change
    public String symVal;
    public HashMap<String, RubyIO> iVars = new HashMap<String, RubyIO>();
    public HashMap<RubyIO, RubyIO> hashVal;
    public RubyIO hashDefVal;
    public RubyIO[] arrVal;
    public byte[] userVal;
    public long fixnumVal;

    public RubyIO() {

    }

    public RubyIO setNull() {
        type = '0';
        strVal = null;
        symVal = null;
        iVars.clear();
        hashVal = null;
        hashDefVal = null;
        arrVal = null;
        userVal = null;
        fixnumVal = 0;
        return this;
    }

    public RubyIO setFX(long fx) {
        setNull();
        type = 'i';
        fixnumVal = fx;
        return this;
    }

    public RubyIO setShallowClone(RubyIO clone) {
        type = clone.type;
        strVal = clone.strVal;
        symVal = clone.symVal;
        iVars.clear();
        iVars.putAll(clone.iVars);
        if (clone.hashVal != null) {
            hashVal = new HashMap<RubyIO, RubyIO>();
            hashVal.putAll(clone.hashVal);
        } else {
            hashVal = null;
        }
        hashDefVal = clone.hashDefVal;
        if (clone.arrVal != null) {
            arrVal = new RubyIO[clone.arrVal.length];
            for (int i = 0; i < arrVal.length; i++)
                arrVal[i] = clone.arrVal[i];
        } else {
            arrVal = null;
        }
        if (clone.userVal != null) {
            userVal = new byte[clone.userVal.length];
            for (int i = 0; i < userVal.length; i++)
                userVal[i] = clone.userVal[i];
        } else {
            userVal = null;
        }
        fixnumVal = clone.fixnumVal;
        return this;
    }

    // That's deep, man. [/decadesIDidntLiveIn]
    public RubyIO setDeepClone(RubyIO clone) {
        setShallowClone(clone);
        for (Map.Entry<String, RubyIO> a : clone.iVars.entrySet())
            iVars.put(a.getKey(), new RubyIO().setDeepClone(a.getValue()));
        if (hashDefVal != null)
            hashDefVal = new RubyIO();
        if (hashVal != null)
            for (Map.Entry<RubyIO, RubyIO> a : clone.hashVal.entrySet())
                hashVal.put(new RubyIO().setDeepClone(a.getKey()), new RubyIO().setDeepClone(a.getValue()));
        if (arrVal != null)
            for (int i = 0; i < arrVal.length; i++)
                arrVal[i] = new RubyIO().setDeepClone(arrVal[i]);
        // userVal is actually copied over by the shallow clone
        return this;
    }

    public static boolean rubyTypeEquals(RubyIO a, RubyIO b) {
        if (a == b)
            return true;
        if (a.type != b.type)
            return false;
        if (a.type == 'o')
            return a.symVal.equals(b.symVal);
        if (a.type == 'u')
            return a.symVal.equals(b.symVal);
        return true;
    }

    // used to check Hash stuff
    public static boolean rubyEquals(RubyIO a, RubyIO b) {
        if (a == b)
            return true;
        if (a.type != b.type)
            return false;
        // primitive types
        if (a.type == 'i')
            return a.fixnumVal == b.fixnumVal;
        if (a.type == '\"')
            return a.decString().equals(b.decString());
        if (a.type == ':')
            return a.symVal.equals(b.symVal);
        if (a.type == 'T')
            return true;
        if (a.type == 'F')
            return true;
        if (a.type == '0')
            return true;
        return false;
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

    public static RubyIO loadObjectFromFile(String filename) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(filename));
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

    public static void saveObjectToFile(String filename, RubyIO object) {
        try {
            DataOutputStream dis = new DataOutputStream(new FileOutputStream(filename));
            // Marshal v4.8
            dis.write(new byte[]{4, 8});
            LinkedList<RubyIO> objCache = new LinkedList<RubyIO>();
            LinkedList<String> strCache = new LinkedList<String>();
            saveValue(dis, object, objCache, strCache);
            dis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
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

    private static RubyIO loadValue(DataInputStream dis, LinkedList<RubyIO> objs, LinkedList<String> syms) throws IOException {
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
            rio = objs.get((int) load32(dis));
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

    @Override
    public String toString() {
        String data = "";
        if (type == 'u')
            return symVal + ";" +  userVal.length + "b";
        if (type == 'o')
            return symVal;
        if (type == '[')
            data = arrVal.length + "]";
        if (type == ':')
            data = symVal;
        if (type == '"')
            return "\"" + decString() + "\"";
        if (type == 'i')
            return Long.toString(fixnumVal);
        return ((char) type) + data;
    }

    public String decString() {
        // ignore the CP-setting madness for now
        // however, if it is to be implemented,
        // the specific details are that:
        // SOME (not all) strings, are tagged with an ":encoding" iVar.
        // This specifies their encoding.
        return new String(strVal, Charset.forName("UTF-8"));
    }
    public void encString(String text) {
        try {
            strVal = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public RubyIO getInstVarBySymbol(String cmd) {
        return iVars.get(cmd);
    }

    // NOTE: this is solely for cases where an external primitive is being thrown in.
    //       in most cases, we already have the RubyIO object by-ref.
    //       (Can't implement equals on RubyIO objects safely due to ObjectDB backreference tracing.)
    public RubyIO getHashVal(RubyIO rio) {
        for (Map.Entry<RubyIO, RubyIO> e : hashVal.entrySet())
            if (rubyEquals(e.getKey(), rio))
                return e.getValue();
        return null;
    }
}
