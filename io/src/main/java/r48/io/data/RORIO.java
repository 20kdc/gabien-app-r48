/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import r48.RubyBigNum;
import r48.io.IMIUtils;

import java.io.*;
import java.nio.charset.Charset;

import gabien.uslx.io.MemoryishR;

/**
 * Read-only IRIO subset.
 * Created 27th August, 2022.
 */
public abstract class RORIO {
    public abstract int getType();

    // IVar Access
    public abstract String[] getIVars();

    public abstract RORIO getIVar(String sym);

    // '"', 'f'
    public abstract Charset getBufferEnc();

    // ':', 'o'
    public abstract String getSymbol();

    // 'i'
    public abstract long getFX();

    // '"', 'f' (NOTE: Changing string encoding requires reinitialization of the object.)
    public String decString() {
        // ignore the CP-setting madness for now
        // however, if it is to be implemented,
        // the specific details are that:
        // SOME (not all) strings, are tagged with an ":encoding" iVar.
        // This specifies their encoding.
        return new String(getBufferCopy(), getBufferEnc());
    }

    // '"', 'f', 'u', 'l'

    // Gets the buffer.
    public abstract MemoryishR getBuffer();

    // can be fast-path'd depending on what the "primary" storage is
    public byte[] getBufferCopy() {
        MemoryishR mr = getBuffer();
        return mr.getBulk(0, (int) mr.length);
    }

    // '['
    public abstract int getALen();

    public abstract RORIO getAElem(int i);

    // '{', '}'
    public abstract DMKey[] getHashKeys();

    public abstract RORIO getHashVal(DMKey key);

    // '}' only
    public abstract RORIO getHashDefVal();

    // Utils

    // Outputs IMI-code for something so that there's a basically human-readable version of it.
    public final String toStringLong(String indent) {
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

    public final byte[] getBufferInEncoding(Charset encoding) {
        Charset enc = getBufferEnc();
        if (!enc.equals(encoding))
            return decString().getBytes(encoding);
        return getBufferCopy();
    }

    @Override
    public final String toString() {
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

    public static boolean rubyTypeEquals(RORIO a, RORIO b) {
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

    /**
     * Used to check Hash stuff.
     * Remember to also keep DMKey.hashCode and friends in sync.
     */
    public static boolean rubyEquals(RORIO a, RORIO b) {
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
