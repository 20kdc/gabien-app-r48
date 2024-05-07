/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.io.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents a hash key.
 * Note that this particular RORIO variant actually works as a hash key.
 * Created 25th March 2023.
 */
public class DMKey extends RORIO {
    public static final DMKey NULL = new DMKey(Subtype.Null, 0, null, null, null);
    public static final DMKey TRUE = new DMKey(Subtype.True, 0, null, null, null);
    public static final DMKey FALSE = new DMKey(Subtype.False, 0, null, null, null);

    private final Subtype st;
    // i
    private final long fxVal;
    // l
    private final byte[] flVal;
    // f " :
    private final String strVal;
    // (any)
    private final IRIOGeneric refVal;

    private DMKey(Subtype st, long fxVal, byte[] flVal, String strVal, IRIOGeneric refVal) {
        this.st = st;
        this.fxVal = fxVal;
        this.flVal = flVal;
        this.strVal = strVal;
        this.refVal = refVal;
    }

    public static DMKey of(RORIO src) {
        int t = src.getType();
        if (t == 'i') {
            return of(src.getFX());
        } else if (t == 'l') {
            return new DMKey(Subtype.Bignum, 0, src.getBuffer().clone(), null, null);
        } else if (t == '"') {
            return ofStr(src.decString());
        } else if (t == 'f') {
            return ofFloat(src.decString());
        } else if (t == ':') {
            return ofSym(src.decString());
        } else if (t == 'T') {
            return TRUE;
        } else if (t == 'F') {
            return FALSE;
        } else if (t == '0') {
            return NULL;
        } else {
            IRIOGeneric refVal = new IRIOGeneric(IDM3Context.Null.DMKEY_EMBEDDED, StandardCharsets.UTF_8);
            refVal.setDeepClone(src);
            return new DMKey(Subtype.Reference, 0, null, null, refVal);
        }
    }

    public static DMKey of(long l) {
        return new DMKey(Subtype.Fixnum, l, null, null, null);
    }

    public static DMKey of(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static DMKey ofStr(String s) {
        return new DMKey(Subtype.String, 0, null, s, null);
    }

    public static DMKey ofFloat(String s) {
        return new DMKey(Subtype.String, 0, null, s, null);
    }

    public static DMKey ofSym(String s) {
        return new DMKey(Subtype.Symbol, 0, null, s, null);
    }

    // -- Equality, etc --

    @Override
    public int hashCode() {
        if (st == Subtype.Bignum) {
            return 0;
        } else if (st == Subtype.Fixnum) {
            return (int) fxVal;
        } else if (st == Subtype.False) {
            return 1;
        } else if (st == Subtype.True) {
            return 2;
        } else if (st == Subtype.Null) {
            return 3;
        } else if (st == Subtype.String || st == Subtype.Symbol) {
            return strVal.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object var1) {
        if (!(var1 instanceof DMKey))
            return false;
        return IRIO.rubyEquals(this, (DMKey) var1);
    }

    // -- RORIO IMPL --

    @Override
    public int getType() {
        switch (st) {
        case Fixnum:
            return 'i';
        case Bignum:
            return 'l';
        case Float:
            return 'f';
        case Reference:
            return refVal.getType();
        case String:
            return '"';
        case Symbol:
            return ':';
        case True:
            return 'T';
        case False:
            return 'F';
        case Null:
            return '0';
        }
        throw new RuntimeException("getType missing a subtype");
    }

    @Override
    public String[] getIVars() {
        if (st == Subtype.Reference)
            return refVal.getIVars();
        return new String[0];
    }

    @Override
    public RORIO getIVar(String sym) {
        if (st == Subtype.Reference)
            return refVal.getIVar(sym);
        return null;
    }

    @Override
    public Charset getBufferEnc() {
        if (st == Subtype.Reference)
            return refVal.getBufferEnc();
        return StandardCharsets.UTF_8;
    }

    @Override
    public String getSymbol() {
        if (st == Subtype.Symbol)
            return strVal;
        return refVal.getSymbol();
    }

    @Override
    public long getFX() {
        if (st == Subtype.Fixnum)
            return fxVal;
        return refVal.getFX();
    }

    @Override
    public String decString() {
        if (st == Subtype.String)
            return strVal;
        return refVal.decString();
    }

    @Override
    public byte[] getBuffer() {
        if (st == Subtype.String || st == Subtype.Float)
            return strVal.getBytes(StandardCharsets.UTF_8);
        if (st == Subtype.Bignum)
            return flVal;
        return refVal.getBuffer();
    }

    @Override
    public int getALen() {
        return refVal.getALen();
    }

    @Override
    public RORIO getAElem(int i) {
        return refVal.getAElem(i);
    }

    @Override
    public DMKey[] getHashKeys() {
        return refVal.getHashKeys();
    }

    @Override
    public RORIO getHashVal(DMKey key) {
        return refVal.getHashVal(key);
    }

    @Override
    public RORIO getHashDefVal() {
        return refVal.getHashDefVal();
    }

    public enum Subtype {
        // i
        Fixnum,
        // l
        Bignum,
        // "
        String,
        // f
        Float,
        // :
        Symbol,
        // T
        True,
        // F
        False,
        // 0
        Null,
        // (any)
        Reference;
    }
}
