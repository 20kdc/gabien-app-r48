/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.data;

/**
 * Created on December 04, 2018.
 */
public abstract class IRIOFixedArray<T extends IRIO> extends IRIOFixed {
    public IRIO[] arrVal = new IRIO[0];

    public IRIOFixedArray() {
        super('[');
    }

    @Override
    public IRIO setArray() {
        arrVal = new IRIO[0];
        return this;
    }

    public abstract T newValue();

    @Override
    public int getALen() {
        return arrVal.length;
    }

    @Override
    public T getAElem(int i) {
        return (T) arrVal[i];
    }

    @Override
    public T addAElem(int i) {
        T rio = newValue();
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
        IRIO[] old = arrVal;
        IRIO[] newArr = new IRIO[old.length - 1];
        System.arraycopy(old, 0, newArr, 0, i);
        System.arraycopy(old, i + 1, newArr, i + 1 - 1, old.length - (i + 1));
        arrVal = newArr;
    }

    @Override
    public String[] getIVars() {
        return new String[0];
    }

    @Override
    public IRIO addIVar(String sym) {
        return null;
    }

    @Override
    public IRIO getIVar(String sym) {
        return null;
    }
}
