/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.data;

import r48.RubyBigNum;
import r48.io.IMIUtils;

import java.io.*;

/**
 * Ok, so here's the deal. Data Model 2 implementation attempt 1 *failed miserably*.
 * That doesn't mean Data Model 2 is completely unimplementable.
 * This interface represents the 'Data Model 2 plan'.
 * Methods are named as they will be in the final version of DM2.
 * RubyIO will provide stronger type guarantees on them so the code won't break.
 * The plan is: Make haste, carefully.
 * Created on November 19, 2018.
 */
public abstract class IRIO {
    public abstract int getType();

    // Primitive Setters. These make copies of any buffers given, among other things.
    // They return self.
    public abstract IRIO setNull();

    public abstract IRIO setFX(long fx);

    public abstract IRIO setBool(boolean b);

    public abstract IRIO setSymbol(String s);

    public abstract IRIO setString(String s);

    // The resulting encoding may not be the one provided.
    public IRIO setString(byte[] s, String jenc) {
        try {
            return setString(new String(s, jenc));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // This is weird...
    public abstract IRIO setFloat(byte[] s);

    public abstract IRIO setHash();

    public abstract IRIO setHashWithDef();

    public abstract IRIO setArray();

    public abstract IRIO setObject(String symbol);

    public abstract IRIO setUser(String symbol, byte[] data);

    public abstract IRIO setBignum(byte[] data);

    // IVar Access
    public abstract String[] getIVars();

    public abstract void rmIVar(String sym);

    // If an IVar cannot be created, this should return null.
    public abstract IRIO addIVar(String sym);

    public abstract IRIO getIVar(String sym);

    // 'i'
    public abstract long getFX();

    // '"', 'f' (NOTE: Changing string encoding requires reinitialization of the object.)
    public String decString() {
        // ignore the CP-setting madness for now
        // however, if it is to be implemented,
        // the specific details are that:
        // SOME (not all) strings, are tagged with an ":encoding" iVar.
        // This specifies their encoding.
        try {
            return new String(getBuffer(), getBufferEnc());
        } catch (UnsupportedEncodingException e) {
            // If this ever occurs, RubyEncodingTranslator's broke
            throw new RuntimeException(e);
        }
    }

    public abstract String getBufferEnc();

    // ':', 'o'
    public abstract String getSymbol();

    // '"', 'f', 'u', 'l'
    public abstract byte[] getBuffer();

    public abstract void putBuffer(byte[] data);

    // '['
    public abstract int getALen();

    public abstract IRIO getAElem(int i);

    public abstract IRIO addAElem(int i);

    public abstract void rmAElem(int i);

    public IRIO[] getANewArray() {
        IRIO[] contents = new IRIO[getALen()];
        for (int i = 0; i < contents.length; i++)
            contents[i] = getAElem(i);
        return contents;
    }

    // '{', '}'
    public abstract IRIO[] getHashKeys();

    // Actually key-based but marked 'Val' for consistency with the old API
    public abstract IRIO addHashVal(IRIO key);

    public abstract IRIO getHashVal(IRIO key);

    public abstract void removeHashVal(IRIO key);

    // '}' only
    public abstract IRIO getHashDefVal();

    public IRIO setDeepClone(IRIO clone) {
        int type = clone.getType();
        if (type == '0') {
            setNull();
        } else if (type == 'T') {
            setBool(true);
        } else if (type == 'F') {
            setBool(false);
        } else if (type == 'i') {
            setFX(clone.getFX());
        } else if (type == '"') {
            setString(clone.getBuffer(), clone.getBufferEnc());
        } else if (type == 'f') {
            setFloat(clone.getBuffer());
        } else if (type == 'o') {
            setObject(clone.getSymbol());
        } else if (type == ':') {
            setSymbol(clone.getSymbol());
        } else if (type == 'u') {
            setUser(clone.getSymbol(), clone.getBuffer());
        } else if (type == 'l') {
            setBignum(clone.getBuffer());
        } else if (type == '[') {
            setArray();
            int myi = getALen();
            int ai = clone.getALen();
            for (int i = 0; i < ai; i++) {
                IRIO ir;
                if (myi <= i) {
                    ir = addAElem(i);
                } else {
                    ir = getAElem(i);
                }
                ir.setDeepClone(clone.getAElem(i));
            }
        } else if ((type == '{') || (type == '}')) {
            if (type == '{') {
                setHash();
            } else {
                setHashWithDef();
                getHashDefVal().setDeepClone(clone.getHashDefVal());
            }
            for (IRIO key : clone.getHashKeys()) {
                IRIO v = addHashVal(key);
                v.setDeepClone(clone.getHashVal(key));
            }
        } else {
            throw new UnsupportedOperationException("Unable to handle this type.");
        }
        for (String iv : clone.getIVars())
            addIVar(iv).setDeepClone(clone.getIVar(iv));
        return this;
    }

    public static byte[] copyByteArray(byte[] buffer) {
        byte[] buffer2 = new byte[buffer.length];
        System.arraycopy(buffer, 0, buffer2, 0, buffer2.length);
        return buffer2;
    }

    public static String[] copyStringArray(String[] iVarKeys) {
        String[] n2 = new String[iVarKeys.length];
        System.arraycopy(iVarKeys, 0, n2, 0, n2.length);
        return iVarKeys;
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
    public String toString() {
        // NOTE: The following rules are relied upon by schema name-routines, at least in theory:
        // 1. "null" means t0.
        // 2. Any valid number is a number.
        // 3. T/F are booleans.
        String data = "";
        int type = getType();
        if (type == 'u')
            return getSymbol() + ";" + getBuffer().length + "b";
        if (type == 'o')
            return getSymbol();
        if (type == '[')
            data = getALen() + "]";
        if (type == ':')
            data = getSymbol();
        if (type == '"')
            return "\"" + decString() + "\"";
        if (type == 'f')
            return decString() + "f";
        if (type == 'i')
            return Long.toString(getFX());
        if (type == 'l') {
            String str2 = "L";
            RubyBigNum working = new RubyBigNum(getBuffer(), false);
            boolean negated = false;
            if (working.isNegative()) {
                negated = true;
                working = working.negate();
            }
            if (working.compare(RubyBigNum.ZERO) == 0) {
                str2 = "0L";
            } else {
                while (working.compare(RubyBigNum.ZERO) > 0) {
                    RubyBigNum[] res = working.divide(RubyBigNum.TEN);
                    str2 = ((char) ('0' + res[1].truncateToLong())) + str2;
                    working = res[0];
                }
            }
            if (negated)
                str2 = "-" + str2;
            return str2;
        }
        if (type == '0')
            return "null";
        return ((char) type) + data;
    }

    public static boolean rubyTypeEquals(IRIO a, IRIO b) {
        if (a == b)
            return true;
        int aType = a.getType();
        if (aType != b.getType())
            return false;
        if (aType == 'o')
            return a.getSymbol().equals(b.getSymbol());
        if (aType == 'u')
            return a.getSymbol().equals(b.getSymbol());
        return true;
    }

    // used to check Hash stuff
    public static boolean rubyEquals(IRIO a, IRIO b) {
        if (a == b)
            return true;
        int aType = a.getType();
        if (aType != b.getType())
            return false;
        // primitive types
        if (aType == 'i')
            return a.getFX() == b.getFX();
        if (aType == '\"')
            return a.decString().equals(b.decString());
        if (aType == 'f')
            return a.decString().equals(b.decString());
        if (aType == 'l')
            return new RubyBigNum(a.getBuffer(), true).compare(new RubyBigNum(b.getBuffer(), true)) == 0;
        if (aType == ':')
            return a.getSymbol().equals(b.getSymbol());
        if (aType == 'T')
            return true;
        if (aType == 'F')
            return true;
        if (aType == '0')
            return true;
        return false;
    }

}
