/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.data.IRIO;

/**
 * Created on 01/06/17.
 */
public class ArrayR2kStruct<T extends IR2kStruct> extends ArrayR2kInterpretable<T> implements IR2kStruct {
    public ArrayR2kStruct(ArraySizeR2kInterpretable<T> other, ISupplier<T> c) {
        super(other, c, true);
    }
    public ArrayR2kStruct(ArraySizeR2kInterpretable<T> other, ISupplier<T> c, boolean trust) {
        super(other, c, trust);
    }
    public ArrayR2kStruct(ArraySizeR2kInterpretable<T> other, ISupplier<T> c, int defSize) {
        super(other, c, true, defSize);
    }
    public ArrayR2kStruct(ArraySizeR2kInterpretable<T> other, ISupplier<T> c, boolean trust, int defSize) {
        super(other, c, trust, defSize);
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO();
        rio.type = '[';
        rio.arrVal = new RubyIO[array.size()];
        for (int i = 0; i < array.size(); i++)
            rio.arrVal[i] = array.get(i).asRIO();
        return rio;
    }

    @Override
    public void fromRIO(IRIO src) {
        array.clear();
        int alen = src.getALen();
        for (int i = 0; i < alen; i++) {
            T x = cons.get();
            x.fromRIO(src.getAElem(i));
            array.add(x);
        }
    }
}
