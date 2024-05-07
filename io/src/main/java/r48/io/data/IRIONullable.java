/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.nio.charset.Charset;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An annoying but necessary wrapper for cases where an IRIO may be null.
 * Created on December 06, 2018.
 */
public class IRIONullable<V extends IRIO> extends IRIO {
    public final V target;
    public boolean nulled;

    public IRIONullable(@NonNull V other, boolean n) {
        super(other.context);
        target = other;
        nulled = n;
    }

    @Override
    public int getType() {
        return nulled ? '0' : target.getType();
    }

    @Override
    public IRIO setNull() {
        nulled = true;
        return this;
    }

    @Override
    public IRIO setFX(long fx) {
        target.setFX(fx);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setBool(boolean b) {
        target.setBool(b);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setSymbol(String s) {
        target.setSymbol(s);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setString(String s) {
        target.setString(s);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setString(byte[] s, Charset jenc) {
        target.setString(s, jenc);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setFloat(byte[] s) {
        target.setFloat(s);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setHash() {
        target.setHash();
        nulled = false;
        return this;
    }

    @Override
    public IRIO setHashWithDef() {
        target.setHashWithDef();
        nulled = false;
        return this;
    }

    @Override
    public IRIO setArray() {
        target.setArray();
        nulled = false;
        return this;
    }

    @Override
    public IRIO setArray(int length) {
        target.setArray(length);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setObject(String symbol) {
        target.setObject(symbol);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setUser(String symbol, byte[] data) {
        target.setUser(symbol, data);
        nulled = false;
        return this;
    }

    @Override
    public IRIO setBignum(byte[] data) {
        target.setBignum(data);
        nulled = false;
        return this;
    }

    // ----

    @Override
    public String[] getIVars() {
        if (nulled)
            return new String[0];
        return target.getIVars();
    }

    @Override
    public void rmIVar(String sym) {
        if (nulled)
            throw new UnsupportedOperationException();
        target.rmIVar(sym);
    }

    @Override
    public IRIO addIVar(String sym) {
        if (nulled)
            return null;
        return target.addIVar(sym);
    }

    @Override
    public IRIO getIVar(String sym) {
        if (nulled)
            return null;
        return target.getIVar(sym);
    }

    @Override
    public long getFX() {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getFX();
    }

    @Override
    public Charset getBufferEnc() {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getBufferEnc();
    }

    @Override
    public String getSymbol() {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getSymbol();
    }

    @Override
    public byte[] getBuffer() {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getBuffer();
    }

    @Override
    public void putBuffer(byte[] data) {
        if (nulled)
            throw new UnsupportedOperationException();
        target.putBuffer(data);
    }

    @Override
    public int getALen() {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getALen();
    }

    @Override
    public IRIO getAElem(int i) {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getAElem(i);
    }

    @Override
    public IRIO addAElem(int i) {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.addAElem(i);
    }

    @Override
    public void rmAElem(int i) {
        if (nulled)
            throw new UnsupportedOperationException();
        target.rmAElem(i);
    }

    @Override
    public DMKey[] getHashKeys() {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getHashKeys();
    }

    @Override
    public IRIO addHashVal(DMKey key) {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.addHashVal(key);
    }

    @Override
    public IRIO getHashVal(DMKey key) {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getHashVal(key);
    }

    @Override
    public void removeHashVal(DMKey key) {
        if (nulled)
            throw new UnsupportedOperationException();
        target.removeHashVal(key);
    }

    @Override
    public IRIO getHashDefVal() {
        if (nulled)
            throw new UnsupportedOperationException();
        return target.getHashDefVal();
    }
}
