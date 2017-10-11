/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;

/**
 * Created on 06/06/17.
 */
public class ArraySetR2kStruct<T extends IR2kStruct> extends ArrayR2kInterpretable<T> implements IR2kStruct {
    public ArraySetR2kStruct(ArraySizeR2kInterpretable<T> other, ISupplier<T> c, boolean trust) {
        super(other, c, trust);
    }

    @Override
    public RubyIO asRIO() {
        RubyIO examineElement = cons.get().asRIO();
        RubyIO hash = new RubyIO().setHash();
        int idx = 0;
        for (T t : array) {
            RubyIO rio = t.asRIO();
            // remove useless elements
            if (!RubyIO.rubyEquals(rio, examineElement))
                hash.hashVal.put(new RubyIO().setFX(idx++), rio);
        }
        return hash;
    }

    @Override
    public void fromRIO(RubyIO src) {
        int maxIdx = -1;
        for (RubyIO rio : src.hashVal.keySet())
            if (rio.fixnumVal > maxIdx)
                maxIdx = (int) rio.fixnumVal;
        array.clear();
        for (int i = 0; i < maxIdx; i++) {
            T v = cons.get();
            RubyIO potential = src.getHashVal(new RubyIO().setFX(i));
            if (potential != null)
                v.fromRIO(potential);
            array.add(v);
        }
    }
}
