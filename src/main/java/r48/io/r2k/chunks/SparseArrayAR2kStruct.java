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
 * NOTE: This uses nulls as padding, so it won't stuff things into index 0 when there shouldn't be anything there.
 * Just a reminder to myself when looking at this again and probably worrying about the same thing.
 * Created on 01/06/17.
 */
public class SparseArrayAR2kStruct<T extends IR2kStruct> extends SparseArrayR2kInterpretable<T> implements IR2kStruct {
    public SparseArrayAR2kStruct(ISupplier<T> call) {
        super(call);
    }

    @Override
    public RubyIO asRIO() {
        int max = -1;
        for (Map.Entry<Integer, T> e : map.entrySet())
            if (e.getKey() > max)
                max = e.getKey();
        RubyIO[] resArray = new RubyIO[max + 1];
        for (int i = 0; i < resArray.length; i++)
            resArray[i] = new RubyIO().setNull();
        for (Map.Entry<Integer, T> e : map.entrySet())
            resArray[e.getKey()] = e.getValue().asRIO();
        RubyIO r = new RubyIO();
        r.type = '[';
        r.arrVal = resArray;
        return r;
    }

    @Override
    public void fromRIO(IRIO src) {
        map.clear();
        int alen = src.getALen();
        for (int i = 0; i < alen; i++) {
            IRIO srcElem = src.getAElem(i);
            // Nulls are used for padding here, don't include them
            if (srcElem.getType() != '0') {
                T n = constructor.get();
                try {
                    n.fromRIO(srcElem);
                } catch (RuntimeException e) {
                    throw new RuntimeException("In element " + i, e);
                }
                map.put(i, n);
            }
        }
    }
}
