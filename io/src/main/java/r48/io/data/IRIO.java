/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.nio.charset.Charset;

import org.eclipse.jdt.annotation.NonNull;

import gabien.uslx.io.MemoryishRW;

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
    /**
     * Data context. Change tracker may one day handle undo/redo tracking.
     */
    public final @NonNull DMContext context;

    /**
     * Cached DMKey to prevent GC churn.
     */
    private DMKey cachedDMKey;

    public IRIO(@NonNull DMContext context) {
        this.context = context;
    }

    @Override
    public final DMKey asKey() {
        if (cachedDMKey != null && RORIO.rubyEquals(this, cachedDMKey))
            return cachedDMKey;
        return cachedDMKey = DMKey.ofInternal(this);
    }

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
    public IRIO setString(byte[] src, Charset srcCharset) {
        return setString(new String(src, srcCharset));
    }

    // This is weird...
    public abstract IRIO setFloat(byte[] s);

    public abstract IRIO setHash();

    public abstract IRIO setHashWithDef();

    /**
     * Sets to an empty array.
     */
    public abstract IRIO setArray();

    /**
     * Sets to an array of a given size. Destroys existing data.
     */
    public abstract IRIO setArray(int length);

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

    public abstract void putBuffer(byte[] data);

    // 'u'
    /**
     * Accesses the current state of the userval for editing.
     * This accessor becomes useless if putBuffer is called or such.
     * Modifications via this accessor are still properly tracked for DMChangeTracker.
     */
    public abstract MemoryishRW editUser();

    // '['
    @Override
    public abstract IRIO getAElem(int i);

    public abstract IRIO addAElem(int i);

    /**
     * Appends a new array element to the end of the array.
     */
    public IRIO appendAElem() {
        return addAElem(getALen());
    }

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
    public abstract DMKey[] getHashKeys();

    // Actually key-based but marked 'Val' for consistency with the old API
    public abstract IRIO addHashVal(DMKey key);

    @Override
    public abstract IRIO getHashVal(DMKey key);

    public abstract void removeHashVal(DMKey key);

    // '}' only
    @Override
    public abstract IRIO getHashDefVal();

    // Utils

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
            setString(clone.getBufferCopy(), clone.getBufferEnc());
            // Due to Ruby using encoding IVars, but encoding IVars being an inherent property in other cases,
            //  *DO NOT* support cross-backend IVar retention on strings.
            return this;
        } else if (type == 'f') {
            setFloat(clone.getBufferCopy());
        } else if (type == 'o') {
            setObject(clone.getSymbol());
        } else if (type == ':') {
            setSymbol(clone.getSymbol());
        } else if (type == 'u') {
            setUser(clone.getSymbol(), clone.getBufferCopy());
        } else if (type == 'l') {
            setBignum(clone.getBufferCopy());
        } else if (type == '[') {
            int ai = clone.getALen();
            setArray(ai);
            for (int i = 0; i < ai; i++)
                getAElem(i).setDeepClone(clone.getAElem(i));
        } else if ((type == '{') || (type == '}')) {
            if (type == '{') {
                setHash();
            } else {
                setHashWithDef();
                getHashDefVal().setDeepClone(clone.getHashDefVal());
            }
            for (DMKey key : clone.getHashKeys()) {
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

    public static String[] copyStringArray(String[] iVarKeys) {
        String[] n2 = new String[iVarKeys.length];
        System.arraycopy(iVarKeys, 0, n2, 0, n2.length);
        return iVarKeys;
    }

    /**
     * Utility function to use an IRIO to reliably get the index of a specific array element.
     * Returns -1 on failure.
     */
    public final int findAElemByIRIO(IRIO irio) {
        int aLen = getALen();
        for (int i = 0; i < aLen; i++) {
            IRIO ref = getAElem(i);
            if (ref == irio)
                return i;
        }
        return -1;
    }

    /**
     * Utility function to use an IRIO to reliably remove a specific array element.
     * (May do nothing if it isn't found.)
     */
    public final void rmAElemByIRIO(IRIO last) {
        int idx = findAElemByIRIO(last);
        if (idx >= 0) {
            rmAElem(idx);
        } else {
            System.err.println("WARNING: rmAElemByIRIO was called, but we couldn't find the IRIO.");
        }
    }
}
