/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import gabien.uslx.io.HexByteEncoding;
import gabien.uslx.vfs.FSBackend;
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Created on 1/27/17.
 */
public class R48ObjectBackend extends OldObjectBackend<RORIO, IRIO> {
    private final String prefix, postfix;
    public final FSBackend fs;

    public R48ObjectBackend(FSBackend fs, String s, String dataExt) {
        super();
        this.fs = fs;
        prefix = s;
        postfix = dataExt;
    }

    @Override
    public IRIOGeneric newObjectO(String n, @NonNull DMContext context) {
        return new IRIOGeneric(context);
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

    public static void save32STM(DataOutputStream dis, long v) throws IOException {
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

    public void save32(DataOutputStream dis, long v) throws IOException {
        save32STM(dis, v);
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
    public IRIOGeneric loadObjectFromFile(String filename, @NonNull DMContext context) {
        return loadObjectFromFile(new IRIOGeneric(context), filename);
    }

    public IRIOGeneric loadObjectFromFile(IRIOGeneric rio, String filename) {
        try {
            String fullPath = prefix + filename + postfix;
            InputStream inp;
            try {
                inp = fs.intoPath(fullPath).openRead();
            } catch (Exception e) {
                System.err.println(fullPath + " wasn't found.");
                return null;
            }
            DataInputStream dis = new DataInputStream(inp);
            // Marshal v4.8
            if (dis.readUnsignedByte() != 0x04)
                throw new IOException("mgk[0]!=0x04");
            if (dis.readUnsignedByte() != 0x08)
                throw new IOException("mgk[1]!=0x08");
            LinkedList<IRIO> objCache = new LinkedList<>();
            LinkedList<String> strCache = new LinkedList<>();
            loadValue(rio, dis, objCache, strCache);
            dis.close();
            return rio;
        } catch (Exception ioe) {
            System.err.println("In file " + filename + ":");
            ioe.printStackTrace();
            return null;
        }
    }

    @Override
    public void saveObjectToFile(String filename, RORIO object) throws IOException {
        OutputStream oup = fs.intoPath(prefix + filename + postfix).openWrite();
        DataOutputStream dis = new DataOutputStream(oup);
        // Marshal v4.8
        dis.write(new byte[] {4, 8});
        saveValue(dis, object, new SaveCaches());
        dis.close();
    }

    private class SaveCaches {
        public int symCacheNextIndex = 0;
        public final HashMap<String, Integer> symCache = new HashMap<>();

        public int indexOfSym(String s) {
            Integer idx = symCache.get(s);
            if (idx == null)
                return -1;
            return idx;
        }
        public void addSym(String s) {
            symCache.put(s, symCacheNextIndex++);
        }
    }

    @Override
    public String userspaceBindersPrefix() {
        return null;
    }

    private void saveSymbol(DataOutputStream dis, String sym, SaveCaches caches) throws IOException {
        int symInd = caches.indexOfSym(sym);
        if (symInd >= 0) {
            dis.write((int) ';');
            save32(dis, symInd);
        } else {
            dis.write((int) ':');
            byte[] d = sym.getBytes("UTF-8");
            save32(dis, d.length);
            dis.write(d);
            caches.addSym(sym);
        }
    }

    private void saveValue(DataOutputStream dis, RORIO rio, SaveCaches caches) throws IOException {
        // Deduplicatables
        int b = rio.getType();
        // there used to be code that wrote @ here, but seeing as we haven't been crosslinking symbols for ages...
        if (b == ':') {
            saveSymbol(dis, rio.getSymbol(), caches);
            return;
        }
        // Everything else.
        // Firstly, pre-process (iVars wrapping)
        String[] iVarKeys = rio.getIVars();
        boolean ivarData = iVarKeys != null;
        if (ivarData)
            ivarData = iVarKeys.length > 0;
        if (b == 'o')
            ivarData = false;
        if (ivarData)
            dis.write((int) 'I');
        // -- Actually write stuff
        dis.write(b);
        if (b == 'o') {
            saveSymbol(dis, rio.getSymbol(), caches);
            saveIVarsCore(dis, rio, caches);
        } else if (b == '{') {
            saveHashCore(dis, rio, caches);
        } else if (b == '}') {
            saveHashCore(dis, rio, caches);
            saveValue(dis, rio.getHashDefVal(), caches);
        } else if (b == '[') {
            int alen = rio.getALen();
            save32(dis, alen);
            for (int i = 0; i < alen; i++)
                saveValue(dis, rio.getAElem(i), caches);
        } else if (b == 'i') {
            save32(dis, rio.getFX());
        } else if ((b == '"') || (b == 'f')) {
            byte[] data = rio.getBuffer();
            save32(dis, data.length);
            dis.write(data);
        } else if (b == 'l') {
            byte[] dat = rio.getBuffer();
            dis.write(dat[0]);
            // the + 1 is implied thanks to the extra sign byte
            save32(dis, dat.length / 2);
            dis.write(dat, 1, dat.length - 1);
            // again, extra sign byte explains the inversion
            if ((dat.length & 1) == 0)
                dis.write(0);
        } else if (b == 'u') {
            saveSymbol(dis, rio.getSymbol(), caches);
            save32(dis, rio.getBuffer().length);
            dis.write(rio.getBuffer());
        } else if ((b != 'T') && (b != 'F') && (b != '0')) {
            throw new IOException("Cannot save " + rio.getType() + " : " + ((char) rio.getType()));
        }
        if (ivarData)
            saveIVarsCore(dis, rio, caches);
    }

    private void saveHashCore(DataOutputStream dis, RORIO content, SaveCaches caches) throws IOException {
        DMKey[] me = content.getHashKeys();
        save32(dis, me.length);
        for (DMKey cKey : me) {
            try {
                saveValue(dis, cKey, caches);
                saveValue(dis, content.getHashVal(cKey), caches);
            } catch (Exception ex) {
                throw new IOException("Hit catch at HK " + cKey, ex);
            }
        }
    }

    private void saveIVarsCore(DataOutputStream dis, RORIO iVars, SaveCaches caches) throws IOException {
        String[] iVarKeys = iVars.getIVars();
        if (iVarKeys == null) {
            save32(dis, 0);
            return;
        }
        save32(dis, iVarKeys.length);
        for (int i = 0; i < iVarKeys.length; i++) {
            String key = iVarKeys[i];
            try {
                saveSymbol(dis, key, caches);
                saveValue(dis, iVars.getIVar(key), caches);
            } catch (Exception ex) {
                throw new IOException("Hit catch at IVar " + key, ex);
            }
        }
    }

    private IRIOGeneric loadValue(DataInputStream dis, LinkedList<IRIO> objs, LinkedList<String> syms, @NonNull DMContext context) throws IOException {
        IRIOGeneric rio = new IRIOGeneric(context);
        loadValue(rio, dis, objs, syms);
        return rio;
    }
    private void loadValue(IRIO rio, DataInputStream dis, LinkedList<IRIO> objs, LinkedList<String> syms) throws IOException {
        int b = dis.readUnsignedByte();
        // r_entry0 is responsible for adding into the object cache.
        boolean handlingInstVars = false;
        boolean shouldWriteObjCacheLate = false;
        // Only handle 'I' once, otherwise error, for sanity purposes
        if (b == 'I') {
            b = dis.readUnsignedByte();
            handlingInstVars = true;
        }
        // "nocareivar" means it will run Ivar stuff if the I prefix is given in theory
        if (b == 'o') {
            // 1889 runs entry before iVars, nocareivar.
            objs.add(rio);
            rio.setObject(loadValue(dis, objs, syms, rio.context).getSymbol());
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
            rio.setDeepClone(objs.get((int) load32(dis)));
        } else if ((b == '{') || (b == '}')) {
            if (b == '{') {
                rio.setHash();    
            } else if (b == '}') {
                rio.setHashWithDef();
            }
            // 1772: Runs entry first thing after creating the hash, nocareivar
            objs.add(rio);
            long vars = load32(dis);
            for (long i = 0; i < vars; i++) {
                IRIOGeneric k = loadValue(dis, objs, syms, rio.context);
                loadValue(rio.addHashVal(DMKey.of(k)), dis, objs, syms);
            }
            if (b == '}')
                loadValue(rio.getHashDefVal(), dis, objs, syms);
        } else if (b == '[') {
            // 1756: Runs entry first thing after creating the array, nocareivar
            objs.add(rio);
            int arrLen = (int) load32(dis);
            rio.setArray(arrLen);
            for (int i = 0; i < arrLen; i++)
                loadValue(rio.getAElem(i), dis, objs, syms);
        } else if (b == ':') {
            // 1957, Never performs an entry, explicitly cancels out iVar
            handlingInstVars = false;
            long len = load32(dis);
            byte[] data = new byte[(int) len];
            dis.readFully(data);
            String str = new String(data, Charset.forName("UTF-8"));
            rio.setSymbol(str);
            syms.add(str);
        } else if (b == ';') {
            // 1969, Never performs an entry, nocareivar
            rio.setSymbol(syms.get((int) load32(dis)));
        } else if (b == 'i') {
            // Never performs an entry, nocareivar
            rio.setFX(load32(dis));
        } else if (b == '"') {
            // 1715, nocareivar, just runs entry.
            objs.add(rio);
            long len = load32(dis);
            byte[] data = new byte[(int) len];
            dis.readFully(data);
            rio.setString("");
            rio.putBuffer(data);
        } else if (b == 'f') {
            objs.add(rio);
            long len = load32(dis);
            byte[] data = new byte[(int) len];
            dis.readFully(data);
            rio.setFloat(data);
        } else if (b == 'l') {
            objs.add(rio);
            byte posneg = dis.readByte();
            if (posneg != '+')
                if (posneg != '-')
                    throw new IOException("Expected + or -");
            int len = (int) load32(dis) * 2;
            byte[] data = new byte[len + 1];
            data[0] = posneg;
            dis.readFully(data, 1, len);
            rio.setBignum(data);
        } else if (b == 'u') {
            // 1832, performs ivars before entry.
            shouldWriteObjCacheLate = true;
            String str = loadValue(dis, objs, syms, rio.context).getSymbol();
            byte[] userData = new byte[(int) load32(dis)];
            dis.readFully(userData);
            rio.setUser(str, userData);
            // The following 3 just don't add themselves to the object table
        } else if (b == 'T') {
            rio.setBool(true);
        } else if (b == 'F') {
            rio.setBool(false);
        } else if (b == '0') {
            rio.setNull();
        } else {
            // Unknown Marshal value. This implies we totally desynced.
            int cA = dis.read();
            int cB = dis.read();
            int cC = dis.read();
            int cD = dis.read();
            String hex = HexByteEncoding.toHexString(b, cA, cB, cC, cD);
            throw new IOException("Unknown Marshal " + ((char) b) + ": " + hex);
        }
        if (handlingInstVars) {
            long vars = load32(dis);
            for (long i = 0; i < vars; i++) {
                IRIOGeneric k = loadValue(dis, objs, syms, rio.context);
                loadValue(rio.addIVar(k.getSymbol()), dis, objs, syms);
            }
        }
        if (shouldWriteObjCacheLate)
            objs.add(rio);
    }
}
