/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;

import java.io.*;
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
        RubyEncodingTranslator.inject(this, javaEncoding);
        return this;
    }

    @Override
    public IRIO setFloat(String s) {
        setString(s, true);
        type = 'f';
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

    // Outputs IMI-code for something so that there's a basically human-readable version of it.
    public String toStringLong(String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IMIUtils.createIMIDump(new DataOutputStream(baos), this, indent);
            // IMI is really 7-bit but UTF-8 is close enough
            return new String(baos.toByteArray(), "UTF-8");
        } catch (Exception ioe) {
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            return indent + "Couldn't dump: " + ioe + "\n" + sw;
        }
    }

    @Override
    public String decString() {
        // ignore the CP-setting madness for now
        // however, if it is to be implemented,
        // the specific details are that:
        // SOME (not all) strings, are tagged with an ":encoding" iVar.
        // This specifies their encoding.
        try {
            return new String(strVal, RubyEncodingTranslator.getStringCharset(this));
        } catch (UnsupportedEncodingException e) {
            // If this ever occurs, RubyEncodingTranslator's broke
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBufferEnc() {
        return RubyEncodingTranslator.getStringCharset(this);
    }

    // intern means "use UTF-8"
    public RubyIO encString(String text, boolean intern) {
        try {
            String encoding = "UTF-8";
            if (!intern)
                encoding = IObjectBackend.Factory.encoding;
            strVal = text.getBytes(encoding);
            RubyEncodingTranslator.inject(this, encoding);
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
    public IRIO addIVar(String sym) {
        RubyIO rio = new RubyIO().setNull();
        addIVar(sym, rio);
        return rio;
    }

    @Override
    public IRIO getIVar(String sym) {
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
    public void setBuffer(byte[] data) {
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
    public IRIO addAElem(int i) {
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
    public IRIO addHashVal(IRIO key) {
        removeHashVal(key);
        RubyIO rt = new RubyIO().setNull();
        hashVal.put(new RubyIO().setDeepClone(key), rt);
        return rt;
    }
}
