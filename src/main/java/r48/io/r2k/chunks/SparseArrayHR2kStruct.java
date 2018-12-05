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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 01/06/17.
 */
public class SparseArrayHR2kStruct<T extends IR2kStruct> implements IR2kStruct {
    public final ISupplier<T> constructor;
    public final HashMap<Integer, T> map = new HashMap<Integer, T>();

    public SparseArrayHR2kStruct(ISupplier<T> call) {
        constructor = call;
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

    @Override
    public void importData(InputStream bais) throws IOException {
        SparseArrayR2kInterpretable.importData(map, constructor, bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        SparseArrayR2kInterpretable.exportData(map, baos);
        return false;
    }
}
