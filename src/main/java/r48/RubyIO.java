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
import java.util.Map;

/**
 * Being phased out as of November 19th, 2018 (beginning of DM2 implementation)
 * Created on 12/27/16.
 */
public class RubyIO extends IRIO {
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
    public HashMap<RubyIO, RubyIO> hashVal;
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

    public RubyIO setString(byte[] s, String javaEncoding) {
        setNull();
        type = '"';
        strVal = copyByteArray(s);
        RubyEncodingTranslator.inject(this, javaEncoding);
        return this;
    }

    public RubyIO setSymlike(String s, boolean object) {
        setNull();
        type = object ? 'o' : ':';
        symVal = s;
        return this;
    }

    public RubyIO setUser(String s, byte[] data) {
        setNull();
        type = 'u';
        symVal = s;
        userVal = data;
        return this;
    }

    public RubyIO setHash() {
        setNull();
        type = '{';
        hashVal = new HashMap<RubyIO, RubyIO>();
        return this;
    }

    // ----

    public RubyIO setShallowClone(RubyIO clone) {
        type = clone.type;
        strVal = clone.strVal;
        symVal = clone.symVal;
        iVarKeys = null;
        iVarVals = null;
        if (clone.iVarKeys != null)
            for (String s : clone.iVarKeys)
                addIVar(s, clone.getInstVarBySymbol(s));
        if (clone.hashVal != null) {
            hashVal = new HashMap<RubyIO, RubyIO>();
            hashVal.putAll(clone.hashVal);
        } else {
            hashVal = null;
        }
        hashDefVal = clone.hashDefVal;
        if (clone.arrVal != null) {
            arrVal = new RubyIO[clone.arrVal.length];
            System.arraycopy(clone.arrVal, 0, arrVal, 0, arrVal.length);
        } else {
            arrVal = null;
        }
        if (clone.userVal != null) {
            userVal = new byte[clone.userVal.length];
            System.arraycopy(clone.userVal, 0, userVal, 0, userVal.length);
        } else {
            userVal = null;
        }
        fixnumVal = clone.fixnumVal;
        return this;
    }

    // That's deep, man. [/decadesIDidntLiveIn]
    public RubyIO setDeepClone(RubyIO clone) {
        setShallowClone(clone);
        if (clone.iVarKeys != null)
            for (String s : clone.iVarKeys)
                addIVar(s, new RubyIO().setDeepClone(clone.getInstVarBySymbol(s)));
        if (hashDefVal != null)
            hashDefVal = new RubyIO();
        if (hashVal != null) {
            hashVal.clear();
            for (Map.Entry<RubyIO, RubyIO> a : clone.hashVal.entrySet())
                hashVal.put(new RubyIO().setDeepClone(a.getKey()), new RubyIO().setDeepClone(a.getValue()));
        }
        if (arrVal != null)
            for (int i = 0; i < arrVal.length; i++)
                arrVal[i] = new RubyIO().setDeepClone(arrVal[i]);
        // userVal is actually copied over by the shallow clone
        return this;
    }

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
    public RubyIO getHashVal(IRIO rio) {
        for (Map.Entry<RubyIO, RubyIO> e : hashVal.entrySet())
            if (rubyEquals(e.getKey(), rio))
                return e.getValue();
        return null;
    }

    public void removeHashVal(IRIO rubyIO) {
        for (Map.Entry<RubyIO, RubyIO> e : hashVal.entrySet())
            if (rubyEquals(e.getKey(), rubyIO)) {
                hashVal.remove(e.getKey());
                // hopefully don't trigger a CME
                return;
            }
    }

    public static byte[] copyByteArray(byte[] s) {
        byte[] t = new byte[s.length];
        System.arraycopy(s, 0, t, 0, t.length);
        return t;
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
    public int getALen() {
        return arrVal.length;
    }
}
