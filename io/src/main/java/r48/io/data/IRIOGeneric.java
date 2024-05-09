/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

/**
 * So, this has been through a long history.
 * It used to be called RubyIO and was the central hub for everything IO-related.
 * Then it became just a data class for an actual "object backend".
 * Then IRIO came along and made it only the data class for generic internal uses and also RGSS.
 * "Being phased out as of November 19th, 2018 (beginning of DM2 implementation)" attests to this.
 * Then finally a big revision has come along, which I'm going to call DM2.5, March 25th 2023.
 * Created December 27th, 2016.
 */
public class IRIOGeneric extends IRIOData {

    private static IRIO[] globalZero = new IRIO[0];

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
    private int type = '0';
    private String symVal;
    // Reduced for memory usage. *sigh*
    // public HashMap<String, RubyIO> iVars = new HashMap<String, RubyIO>();
    private String[] iVarKeys;
    private IRIO[] iVarVals;
    private HashMap<DMKey, IRIO> hashVal;
    private IRIO hashDefVal;
    private IRIO[] arrVal;
    // actual meaning depends on iVars.
    // For string-likes (f, "): Should be treated as immutable - replace strVal on change
    private byte[] userVal;
    private long fixnumVal;
    public final @NonNull Charset charset;

    public IRIOGeneric(@NonNull DMContext context) {
        super(context);
        charset = context.encoding;
    }

    // ---- Save States ----

    @Override
    @SuppressWarnings("unchecked")
    public Runnable saveState() {
        final int typeC = type;
        final String symValC = symVal;
        final String[] iVarKeysC = (iVarKeys != null) ? iVarKeys.clone() : null;
        final IRIO[] iVarValsC = (iVarVals != null) ? iVarVals.clone() : null;
        final HashMap<DMKey, IRIO> hashValC = (hashVal != null) ? (HashMap<DMKey, IRIO>) hashVal.clone() : null;
        final IRIO hashDefValC = hashDefVal;
        final IRIO[] arrValC = (arrVal != null) ? arrVal.clone() : null;
        final byte[] userValC = (userVal != null) ? userVal.clone() : null;
        final long fixnumValC = fixnumVal;
        return () -> {
            type = typeC;
            symVal = symValC;
            iVarKeys = (iVarKeysC != null) ? iVarKeysC.clone() : null;
            iVarVals = (iVarValsC != null) ? iVarValsC.clone() : null;
            hashVal = (hashValC != null) ? (HashMap<DMKey, IRIO>) hashValC.clone() : null;
            hashDefVal = hashDefValC;
            arrVal = (arrValC != null) ? arrValC.clone() : null;
            userVal = (userValC != null) ? userValC.clone() : null;
            fixnumVal = fixnumValC;
        };
    }

    // ---- Value creators ----

    @Override
    public IRIO setNull() {
        // all other set functions call this
        trackingWillChange();
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
    public IRIO setFX(long fx) {
        setNull();
        type = 'i';
        fixnumVal = fx;
        return this;
    }

    @Override
    public IRIO setBool(boolean b) {
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
        return setString(s.getBytes(charset), charset);
    }

    @Override
    public IRIO setString(byte[] s, Charset srcCharset) {
        if (!charset.equals(srcCharset))
            s = new String(s, srcCharset).getBytes(charset);
        setNull();
        type = '"';
        userVal = s;
        return this;
    }

    @Override
    public Charset getBufferEnc() {
        return charset;
    }

    @Override
    public IRIO setFloat(byte[] s) {
        setNull();
        type = 'f';
        userVal = s;
        return this;
    }

    private IRIO setSymlike(String s, boolean object) {
        setNull();
        type = object ? 'o' : ':';
        symVal = s;
        return this;
    }

    @Override
    public IRIO setUser(String s, byte[] data) {
        setNull();
        type = 'u';
        symVal = s;
        userVal = data;
        return this;
    }

    @Override
    public IRIO setHash() {
        setNull();
        type = '{';
        hashVal = new HashMap<>();
        return this;
    }

    @Override
    public IRIO setHashWithDef() {
        setNull();
        type = '}';
        hashVal = new HashMap<>();
        hashDefVal = new IRIOGeneric(context);
        return this;
    }

    @Override
    public IRIO setArray() {
        setNull();
        type = '[';
        arrVal = globalZero;
        return this;
    }

    @Override
    public IRIO setArray(int length) {
        setArray();
        if (length != 0)
            arrVal = new IRIO[length];
        for (int i = 0; i < length; i++)
            arrVal[i] = new IRIOGeneric(context);
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
    public IRIO setDeepClone(RORIO clone) {
        super.setDeepClone(clone);
        return this;
    }

    // ----

    @Override
    public void rmIVar(String s) {
        if (iVarKeys == null)
            return;
        for (int i = 0; i < iVarKeys.length; i++) {
            if (iVarKeys[i].equals(s)) {
                trackingWillChange();
                String[] oldKeys = iVarKeys;
                IRIO[] oldVals = iVarVals;
                iVarKeys = new String[oldKeys.length - 1];
                iVarVals = new IRIO[oldVals.length - 1];
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
    public IRIO getHashVal(DMKey rio) {
        return hashVal.get(rio);
    }

    @Override
    public void removeHashVal(DMKey rubyIO) {
        // addHashVal expects this unconditional call
        trackingWillChange();
        hashVal.remove(rubyIO);
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
        rmIVar(sym);
        trackingWillChange();
        IRIOGeneric rio = new IRIOGeneric(context);
        if (iVarKeys == null) {
            iVarKeys = new String[] {sym};
            iVarVals = new IRIO[] {rio};
            return rio;
        }
        String[] oldKeys = iVarKeys;
        IRIO[] oldVals = iVarVals;
        iVarKeys = new String[oldKeys.length + 1];
        iVarVals = new IRIO[oldVals.length + 1];
        System.arraycopy(oldKeys, 0, iVarKeys, 1, iVarKeys.length - 1);
        System.arraycopy(oldVals, 0, iVarVals, 1, iVarVals.length - 1);
        iVarKeys[0] = sym;
        iVarVals[0] = rio;
        return rio;
    }

    @Override
    public IRIO getIVar(String sym) {
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
        trackingWillChange();
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
        trackingWillChange();
        IRIO rio = new IRIOGeneric(context);
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
        trackingWillChange();
        IRIO[] old = arrVal;
        IRIO[] newArr = new IRIO[old.length - 1];
        System.arraycopy(old, 0, newArr, 0, i);
        System.arraycopy(old, i + 1, newArr, i + 1 - 1, old.length - (i + 1));
        arrVal = newArr;
    }

    @Override
    public DMKey[] getHashKeys() {
        return new LinkedList<DMKey>(hashVal.keySet()).toArray(new DMKey[0]);
    }

    @Override
    public IRIO addHashVal(DMKey key) {
        // already calls trackingWillChange
        removeHashVal(key);
        IRIO rt = new IRIOGeneric(context);
        hashVal.put(key, rt);
        return rt;
    }
}
