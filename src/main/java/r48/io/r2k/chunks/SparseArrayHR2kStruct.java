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

import java.util.Map;

/**
 * Created on 01/06/17.
 */
public class SparseArrayHR2kStruct<T extends IR2kStruct> extends SparseArrayR2kInterpretable<T> implements IR2kStruct {

    public SparseArrayHR2kStruct(ISupplier<T> call) {
        super(call);
    }

    @Override
    public RubyIO asRIO() {
        RubyIO ev = new RubyIO().setHash();
        for (Map.Entry<Integer, T> e : map.entrySet())
            ev.hashVal.put(new RubyIO().setFX(e.getKey()), e.getValue().asRIO());
        return ev;
    }

    @Override
    public void fromRIO(IRIO src) {
        map.clear();
        for (IRIO e : src.getHashKeys()) {
            T n = constructor.get();
            n.fromRIO(src.getHashVal(e));
            map.put((int) e.getFX(), n);
        }
    }
}
