/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import r48.io.IObjectBackend;
import r48.io.data.IRIO;

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
     * f, "    : strVal
     * :, o    : symVal
     * u       : symVal, userVal
     * {       : hashVal
     * }       : hashVal, hashDefVal
     * [       : arrVal
     * l       : userVal (first byte is the +/- byte, remainder is data)
     */
    public int type;
    public byte[] strVal; // actual meaning depends on iVars. Should be treated as immutable - replace strVal on change
    public String symVal;
    // Reduced for memory usage. *sigh*
    // public HashMap<String, RubyIO> iVars = new HashMap<String, RubyIO>();
    public String[] iVarKeys;
    public RubyIO[] iVarVals;
    public HashMap<IRIO, RubyIO> hashVal;
    public RubyIO hashDefVal;
    public RubyIO[] arrVal;
    public byte[] userVal;
    public long fixnumVal;

    public RubyIO() {

    }

    // ---- Value creators ----

    @Override
    public RubyIO setNull() {
        type = '0';
        strVal = null;
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
        strVal = copyByteArray(s);
        String st = rubyInjectEncoding(this, javaEncoding);
        if (!st.equals(javaEncoding)) {
            try {
                strVal = new String(s, javaEncoding).getBytes(st);
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException(uee);
            }
        }
        return this;
    }

    @Override
    public IRIO setFloat(byte[] s) {
        setNull();
        type = 'f';
        strVal = s;
        return this;
    }

    public RubyIO setSymlike(String s, boolean object) {
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
        userVal = copyByteArray(data);
        return this;
    }

    @Override
    public RubyIO setHash() {
        setNull();
        type = '{';
        hashVal = new HashMap<IRIO, RubyIO>();
        return this;
    }

    @Override
    public IRIO setHashWithDef() {
        setNull();
        type = '}';
        hashVal = new HashMap<IRIO, RubyIO>();
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
    public IRIO setObject(String symbol) {
        return setSymlike(symbol, true);
    }

    @Override
    public IRIO setBignum(byte[] data) {
        setNull();
        type = 'l';
        userVal = copyByteArray(data);
        return this;
    }

    @Override
    public String[] getIVars() {
        if (iVarKeys == null)
            return new String[0];
        return copyStringArray(iVarKeys);
    }

    @Override
    public RubyIO setDeepClone(IRIO clone) {
        super.setDeepClone(clone);
        return this;
    }

    // ----

    @Override
    public String getBufferEnc() {
        return rubyGetEncoding(this);
    }

    // intern means "use UTF-8"
    public RubyIO encString(String text, boolean intern) {
        try {
            String encoding = "UTF-8";
            if (!intern)
                encoding = IObjectBackend.Factory.encoding;
            strVal = text.getBytes(encoding);
            rubyInjectEncoding(this, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void addIVar(String s, RubyIO rio) {
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

    public RubyIO getInstVarBySymbol(String cmd) {
        if (iVarKeys == null)
            return null;
        for (int i = 0; i < iVarKeys.length; i++)
            if (cmd.equals(iVarKeys[i]))
                return iVarVals[i];
        return null;
        // return iVars.get(cmd);
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
    public RubyIO getHashVal(IRIO rio) {
        RubyIO basis = hashVal.get(rio);
        if (basis != null)
            return basis;
        for (Map.Entry<IRIO, RubyIO> e : hashVal.entrySet())
            if (rubyEquals(e.getKey(), rio))
                return e.getValue();
        return null;
    }

    @Override
    public void removeHashVal(IRIO rubyIO) {
        for (Map.Entry<IRIO, RubyIO> e : hashVal.entrySet())
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
        return getInstVarBySymbol(sym);
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
        if (strVal != null)
            return strVal;
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
    public RubyIO getAElem(int i) {
        return arrVal[i];
    }

    @Override
    public RubyIO addAElem(int i) {
        RubyIO rio = new RubyIO().setNull();
        RubyIO[] old = arrVal;
        RubyIO[] newArr = new RubyIO[old.length + 1];
        System.arraycopy(old, 0, newArr, 0, i);
        newArr[i] = rio;
        System.arraycopy(old, i, newArr, i + 1, old.length - i);
        arrVal = newArr;
        return rio;
    }

    @Override
    public void rmAElem(int i) {
        RubyIO[] old = arrVal;
        RubyIO[] newArr = new RubyIO[old.length - 1];
        System.arraycopy(old, 0, newArr, 0, i);
        System.arraycopy(old, i + 1, newArr, i + 1 - 1, old.length - (i + 1));
        arrVal = newArr;
    }

    @Override
    public IRIO[] getHashKeys() {
        return new LinkedList<IRIO>(hashVal.keySet()).toArray(new IRIO[0]);
    }

    @Override
    public RubyIO addHashVal(IRIO key) {
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
                ri.strVal = basicEncoding.getBytes("UTF-8");
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
