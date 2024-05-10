/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.nio.charset.Charset;

import org.eclipse.jdt.annotation.NonNull;

import gabien.uslx.io.MemoryishR;
import gabien.uslx.io.MemoryishRW;

/**
 * Keep in sync with IRIOTypedData!
 * Copied from IRIOTypedData 10th May 2024.
 */
public abstract class IRIOTypedMask extends IRIO {
    public IRIOTypedMask(@NonNull DMContext context) {
        super(context);
    }

    @Override
    public IRIO setNull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setFX(long fx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setBool(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setSymbol(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setString(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setFloat(byte[] s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setHash() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setHashWithDef() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setArray(int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setObject(String symbol) {
        throw new UnsupportedOperationException(getClass() + " can't be set to object of symbol " + symbol);
    }

    @Override
    public IRIO setUser(String symbol, byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO setBignum(byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rmIVar(String sym) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getFX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Charset getBufferEnc() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSymbol() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MemoryishR getBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MemoryishRW editUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putBuffer(byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getALen() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO getAElem(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO addAElem(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rmAElem(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DMKey[] getHashKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO addHashVal(DMKey key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO getHashVal(DMKey key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeHashVal(DMKey key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO getHashDefVal() {
        throw new UnsupportedOperationException();
    }
}
