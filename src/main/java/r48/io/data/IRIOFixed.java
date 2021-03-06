/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.data;

/**
 * An IRIO with a fixed type.
 * The IRIO cannot be changed from this type.
 * All methods apart from IVars (left unimplemented except for rmIVar, which is not supported) are implemented as 'not supported' by default.
 * The setter method for your specific type should be reimplemented.
 * Created on November 22, 2018.
 */
public abstract class IRIOFixed extends IRIO {
    protected int type;

    public IRIOFixed(int t) {
        type = t;
    }

    @Override
    public int getType() {
        return type;
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
    public IRIO setObject(String symbol) {
        throw new UnsupportedOperationException();
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
    public String getBufferEnc() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSymbol() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBuffer() {
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
    public IRIO[] getHashKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO addHashVal(IRIO key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO getHashVal(IRIO key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeHashVal(IRIO key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRIO getHashDefVal() {
        throw new UnsupportedOperationException();
    }
}
