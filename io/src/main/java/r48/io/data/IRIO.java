/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.io.*;

/**
 * Ok, so here's the deal. Data Model 2 implementation attempt 1 *failed miserably*.
 * That doesn't mean Data Model 2 is completely unimplementable.
 * This interface represents the 'Data Model 2 plan'.
 * Methods are named as they will be in the final version of DM2.
 * RubyIO will provide stronger type guarantees on them so the code won't break.
 * The plan is: Make haste, carefully.
 *
 * -- ADDITIONAL NOTES --
 * All byte buffers passed into an IRIO must be copied beforehand unless you want the resulting linking.
 *
 * Created on November 19, 2018.
 */
public abstract class IRIO extends RORIO {

    // Primitive Setters. These make copies of any buffers given, among other things.
    // They return self.
    public abstract IRIO setNull();

    public abstract IRIO setFX(long fx);

    public abstract IRIO setBool(boolean b);

    public abstract IRIO setSymbol(String s);

    public abstract IRIO setString(String s);

    // The resulting encoding may not be the one provided.
    // This *should* be overridden for the specific encoding logic.
    // The byte buffer must be copied by the caller.
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
    public abstract void rmIVar(String sym);

    // If an IVar cannot be created, this should return null.
    // This and the other 'add' functions must create *defined* values (no "getType returns literal decimal 0 rather than '0'")
    public abstract IRIO addIVar(String sym);

    @Override
    public abstract IRIO getIVar(String sym);

    // '"', 'f', 'u', 'l'

    // For 'u', the buffer must be mutable ; for others it is variable.
    public abstract void putBuffer(byte[] data);

    // '['
    @Override
    public abstract IRIO getAElem(int i);

    public abstract IRIO addAElem(int i);

    public abstract void rmAElem(int i);

    // If true, safety measures are activated in IMI
    public boolean getAFixedFormat() {
        return false;
    }

    public IRIO[] getANewArray() {
        IRIO[] contents = new IRIO[getALen()];
        for (int i = 0; i < contents.length; i++)
            contents[i] = getAElem(i);
        return contents;
    }

    // '{', '}'
    @Override
    public abstract IRIO[] getHashKeys();

    // Actually key-based but marked 'Val' for consistency with the old API
    public abstract IRIO addHashVal(RORIO key);

    @Override
    public abstract IRIO getHashVal(RORIO key);

    public abstract void removeHashVal(RORIO key);

    @Override
    public abstract IRIO getHashDefVal();

    // Utils

    public IRIO setStringNoEncodingIVars() {
        return setString("");
    }

    public IRIO setDeepClone(RORIO clone) {
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
            setString(copyByteArray(clone.getBuffer()), clone.getBufferEnc());
            // Due to Ruby using encoding IVars, but encoding IVars being an inherent property in other cases,
            //  *DO NOT* support cross-backend IVar retention on strings.
            return this;
        } else if (type == 'f') {
            setFloat(copyByteArray(clone.getBuffer()));
        } else if (type == 'o') {
            setObject(clone.getSymbol());
        } else if (type == ':') {
            setSymbol(clone.getSymbol());
        } else if (type == 'u') {
            setUser(clone.getSymbol(), copyByteArray(clone.getBuffer()));
        } else if (type == 'l') {
            setBignum(copyByteArray(clone.getBuffer()));
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
            for (RORIO key : clone.getHashKeys()) {
                IRIO v = addHashVal(key);
                v.setDeepClone(clone.getHashVal(key));
            }
        } else {
            throw new UnsupportedOperationException("Unable to handle this type.");
        }
        for (String iv : clone.getIVars()) {
            try {
                addIVar(iv).setDeepClone(clone.getIVar(iv));
            } catch (RuntimeException re) {
                throw new RuntimeException("In " + iv + " of " + this.getClass() + " from " + clone.getClass(), re);
            }
        }
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
}
