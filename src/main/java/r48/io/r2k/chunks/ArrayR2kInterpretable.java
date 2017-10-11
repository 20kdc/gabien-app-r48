/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import gabien.ui.ISupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * And now this thing has a workaround in it for broken MoveCommands. *sigh* -- June 3rd 2017, according to the clock (but < 1AM)
 * Created on 31/05/17.
 */
public class ArrayR2kInterpretable<T extends IR2kInterpretable> implements IR2kInterpretable {
    public LinkedList<T> array = new LinkedList<T>();
    public final ISupplier<T> cons;
    public final boolean trustData;
    public final ArraySizeR2kInterpretable<T> linked;

    public ArrayR2kInterpretable(ArraySizeR2kInterpretable<T> other, ISupplier<T> c, boolean trust) {
        cons = c;
        linked = other;
        if (other != null)
            other.target = new ISupplier<ArrayR2kInterpretable<T>>() {
                @Override
                public ArrayR2kInterpretable<T> get() {
                    return ArrayR2kInterpretable.this;
                }
            };
        trustData = trust;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        array.clear();
        while (bais.available() > 0) {
            try {
                T v = cons.get();
                v.importData(bais);
                array.add(v);
            } catch (IOException re) {
                if (trustData)
                    throw new IOException("While parsing in array of " + (cons.get().getClass()), re);
            } catch (RuntimeException re) {
                if (trustData)
                    throw new IOException("While parsing in array of " + (cons.get().getClass()), re);
            }
        }
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        if (linked != null)
            if (linked.resultBytes != null) {
                baos.write(linked.resultBytes);
                return false;
            }
        for (T v : array)
            v.exportData(baos);
        return false;
    }
}
