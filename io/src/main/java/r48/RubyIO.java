/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.io.data.RORIO;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Being phased out as of November 19th, 2018 (beginning of DM2 implementation)
 * Created on 12/27/16.
 */
public class RubyIO extends IRIO {

    private static RubyIO[] globalZeroRubyIOs = new RubyIO[0];

    /*
     * The Grand List Of Objects R48 Supports:
     * NOTE: All objects can theoretically have iVars.
     *       Serialization support varies, and sometimes iVars may be lost.
     * 0, T, F : singletons. no values set
     * i       : fixnumVal
     * f, "    : userVal (as immutable)
     * :, o    : symVal
     * u       : symVal, userVal
     * {       : hashVal
     * }       : hashVal, hashDefVal
     * [       : arrVal
     * l       : userVal (first byte is the +/- byte, remainder is data)
     */
    private int type;
    private String symVal;
    // Reduced for memory usage. *sigh*
    // public HashMap<String, RubyIO> iVars = new HashMap<String, RubyIO>();
    private String[] iVarKeys;
    private RubyIO[] iVarVals;
    public HashMap<IRIO, IRIO> hashVal;
    public RubyIO hashDefVal;
    public IRIO[] arrVal;
    // actual meaning depends on iVars.
    // For string-likes (f, "): Should be treated as immutable - replace strVal on change
    private byte[] userVal;
    private long fixnumVal;

    public RubyIO() {

    }

    // ---- Value creators ----

    @Override
    public RubyIO setNull() {
        type = '0';
        symVal = null;
        iVarKeys = null;
        iVarVals = null;
        hashVal = null;
        hashDefVal = null;
        arrVal = null;
        userVal = null;
        fixnumVal = 0;
        return this;
    }

    @Override
    public RubyIO setFX(long fx) {
        setNull();
        type = 'i';
        fixnumVal = fx;
        return this;
    }

    @Override
    public RubyIO setBool(boolean b) {
        setNull();
        type = b ? 'T' : 'F';
        return this;
    }

    @Override
    public IRIO setSymbol(String s) {
        return setSymlike(s, false);
    }

    @Override
    public IRIO setString(String s) {
        return setString(s, false);
    }

    public RubyIO setString(String s, boolean intern) {
        setNull();
        type = '"';
        encString(s, intern);
        return this;
    }

    @Override
    public RubyIO setString(byte[] s, String javaEncoding) {
        setNull();
        type = '"';
        userVal = s;
        String st = rubyInjectEncoding(this, javaEncoding);
        if (!st.equals(javaEncoding)) {
            try {
                userVal = new String(s, javaEncoding).getBytes(st);
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException(uee);
            }
        }
        return this;
    }

    @Override
    public IRIO setStringNoEncodingIVars() {
        setNull();
        type = '"';
        userVal = new byte[0];
        return this;
    }

    @Override
    public IRIO setFloat(byte[] s) {
        setNull();
        type = 'f';
        userVal = s;
        return this;
    }

    private RubyIO setSymlike(String s, boolean object) {
        setNull();
        type = object ? 'o' : ':';
        symVal = s;
        return this;
    }

    @Override
    public RubyIO setUser(String s, byte[] data) {
        setNull();
        type = 'u';
        symVal = s;
        userVal = data;
        return this;
    }

    @Override
    public RubyIO setHash() {
        setNull();
        type = '{';
        hashVal = new HashMap<IRIO, IRIO>();
        return this;
    }

    @Override
    public IRIO setHashWithDef() {
        setNull();
        type = '}';
        hashVal = new HashMap<IRIO, IRIO>();
        hashDefVal = new RubyIO().setNull();
        return this;
    }

    @Override
    public RubyIO setArray() {
        setNull();
        type = '[';
        arrVal = globalZeroRubyIOs;
        return this;
    }

    @Override
    public IRIO setArray(int length) {
        setArray();
        if (length != 0)
            arrVal = new RubyIO[length];
        for (int i = 0; i < length; i++)
            arrVal[i] = new RubyIO();
        return this;
    }

    @Override
    public IRIO setObject(String symbol) {
        return setSymlike(symbol, true);
    }

    @Override
    public IRIO setBignum(byte[] data) {
        setNull();
        type = 'l';
        userVal = data;
        return this;
    }

    @Override
    public String[] getIVars() {
        if (iVarKeys == null)
            return new String[0];
        return copyStringArray(iVarKeys);
    }

    @Override
    public RubyIO setDeepClone(RORIO clone) {
        super.setDeepClone(clone);
        return this;
    }

    // ----

    @Override
    public String getBufferEnc() {
        return rubyGetEncoding(this);
    }

    // intern means "use UTF-8"
    private RubyIO encString(String text, boolean intern) {
        try {
            String encoding = "UTF-8";
            if (!intern)
                encoding = IObjectBackend.Factory.encoding;
            userVal = text.getBytes(encoding);
            rubyInjectEncoding(this, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void addIVar(String s, RubyIO rio) {
        if (iVarKeys == null) {
            iVarKeys = new String[] {s};
            iVarVals = new RubyIO[] {rio};
            return;
        }
        rmIVar(s);
        String[] oldKeys = iVarKeys;
        RubyIO[] oldVals = iVarVals;
        iVarKeys = new String[oldKeys.length + 1];
        iVarVals = new RubyIO[oldVals.length + 1];
        System.arraycopy(oldKeys, 0, iVarKeys, 1, iVarKeys.length - 1);
        System.arraycopy(oldVals, 0, iVarVals, 1, iVarVals.length - 1);
        iVarKeys[0] = s;
        iVarVals[0] = rio;
    }

    @Override
    public void rmIVar(String s) {
        if (iVarKeys == null)
            return;
        for (int i = 0; i < iVarKeys.length; i++) {
            if (iVarKeys[i].equals(s)) {
                String[] oldKeys = iVarKeys;
                RubyIO[] oldVals = iVarVals;
                iVarKeys = new String[oldKeys.length - 1];
                iVarVals = new RubyIO[oldVals.length - 1];
                System.arraycopy(oldKeys, 0, iVarKeys, 0, i);
                System.arraycopy(oldVals, 0, iVarVals, 0, i);
                System.arraycopy(oldKeys, i + 1, iVarKeys, i, oldKeys.length - (i + 1));
                System.arraycopy(oldVals, i + 1, iVarVals, i, oldKeys.length - (i + 1));
                return;
            }
        }
    }

    // NOTE: this is solely for cases where an external primitive is being thrown in.
    //       in most cases, we already have the RubyIO object by-ref.
    //       (Can't implement equals on RubyIO objects safely due to ObjectDB backreference tracing.)
    @Override
    public IRIO getHashVal(RORIO rio) {
        IRIO basis = hashVal.get(rio);
        if (basis != null)
            return basis;
        for (Map.Entry<IRIO, IRIO> e : hashVal.entrySet())
            if (rubyEquals(e.getKey(), rio))
                return e.getValue();
        return null;
    }

    @Override
    public void removeHashVal(RORIO rubyIO) {
        for (Map.Entry<IRIO, IRIO> e : hashVal.entrySet())
            if (rubyEquals(e.getKey(), rubyIO)) {
                hashVal.remove(e.getKey());
                // hopefully don't trigger a CME
                return;
            }
    }

    @Override
    public IRIO getHashDefVal() {
        if (getType() != '}')
            throw new UnsupportedOperationException();
        return hashDefVal;
    }

    // IRIO compat.

    @Override
    public RubyIO addIVar(String sym) {
        RubyIO rio = new RubyIO().setNull();
        addIVar(sym, rio);
        return rio;
    }

    @Override
    public RubyIO getIVar(String sym) {
        if (iVarKeys == null)
            return null;
        for (int i = 0; i < iVarKeys.length; i++)
            if (sym.equals(iVarKeys[i]))
                return iVarVals[i];
        return null;
        // return iVars.get(cmd);
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public long getFX() {
        if (type != 'i')
            throw new RuntimeException("Not fixnum.");
        return fixnumVal;
    }

    @Override
    public String getSymbol() {
        return symVal;
    }

    @Override
    public byte[] getBuffer() {
        return userVal;
    }

    @Override
    public void putBuffer(byte[] data) {
        userVal = data;
    }

    @Override
    public int getALen() {
        return arrVal.length;
    }

    @Override
    public IRIO getAElem(int i) {
        return arrVal[i];
    }

    @Override
    public RubyIO addAElem(int i) {
        RubyIO rio = new RubyIO().setNull();
        IRIO[] old = arrVal;
        IRIO[] newArr = new IRIO[old.length + 1];
        System.arraycopy(old, 0, newArr, 0, i);
        newArr[i] = rio;
        System.arraycopy(old, i, newArr, i + 1, old.length - i);
        arrVal = newArr;
        return rio;
    }

    @Override
    public void rmAElem(int i) {
        IRIO[] old = arrVal;
        IRIO[] newArr = new IRIO[old.length - 1];
        System.arraycopy(old, 0, newArr, 0, i);
        System.arraycopy(old, i + 1, newArr, i + 1 - 1, old.length - (i + 1));
        arrVal = newArr;
    }

    @Override
    public IRIO[] getHashKeys() {
        return new LinkedList<IRIO>(hashVal.keySet()).toArray(new IRIO[0]);
    }

    @Override
    public RubyIO addHashVal(RORIO key) {
        removeHashVal(key);
        RubyIO rt = new RubyIO().setNull();
        hashVal.put(new RubyIO().setDeepClone(key), rt);
        return rt;
    }

    // Returns the resulting encoding.
    private static String rubyInjectEncoding(RubyIO rubyIO, String s) {
        rubyIO.rmIVar("jEncoding");
        rubyIO.rmIVar("encoding");
        rubyIO.rmIVar("E");
        if (s.equalsIgnoreCase("UTF-8")) {
            rubyIO.addIVar("E").setBool(true);
            return s;
        }
        String basicEncoding = null;
        if (s.equalsIgnoreCase("Cp1252")) {
            basicEncoding = "Windows-1252";
        } else if (s.equalsIgnoreCase("MS949")) {
            // Korean
            basicEncoding = "CP949";
        } else if (s.equalsIgnoreCase("Cp943C") || s.equalsIgnoreCase("MS932")) {
            basicEncoding = "SHIFT-JIS";
        }
        if (basicEncoding != null) {
            RubyIO ri = new RubyIO().setNull();
            ri.type = '"';
            try {
                ri.userVal = basicEncoding.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            rubyIO.addIVar("encoding", ri);
            return s;
        }

        // Can't translate, use fallback
        // NOTE: This isn't too critically important, *unless a file from an "old" backend is copied to a "new" backend.*
        rubyIO.addIVar("jEncoding").setSymbol(s);
        return s;
    }

    // Returns "Canonical Name for java.io API and java.lang API" as documented on "https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html".
    private static String rubyGetEncoding(RubyIO rubyIO) {
        IRIO easy = rubyIO.getIVar("E");
        if (easy != null)
            return "UTF-8";
        IRIO jencoding = rubyIO.getIVar("jEncoding");
        if (jencoding != null)
            return jencoding.getSymbol();
        IRIO encoding = rubyIO.getIVar("encoding");
        if (encoding != null) {
            String s = encoding.decString();
            // Japanese (see above function to explain the mapping)
            if (s.equalsIgnoreCase("SHIFT-JIS"))
                return "Cp943C";
            // Korean
            if (s.equalsIgnoreCase("CP949"))
                return "MS949";
            if (s.equalsIgnoreCase("CP850")) {
                // CP850 - OneShot
                return "Cp850";
            }
            // 1. Check known encoding names
            // 2. Guess the encoding name
            // 3. Throw monitors at user until they tell us the encoding
            try {
                new String(new byte[0], s);
                // let's just pretend this is a good idea, 'kay?
                return s;
            } catch (Exception e) {
            }
            s = s.replace('P', 'p');
            try {
                new String(new byte[0], s);
                // ...just in case.
                return s;
            } catch (Exception e) {
            }
        }

        // Sane default
        return "UTF-8";
    }
}
