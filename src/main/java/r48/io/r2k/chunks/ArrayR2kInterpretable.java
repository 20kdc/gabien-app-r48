/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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

    public ArrayR2kInterpretable(ArraySizeR2kInterpretable<T> other, ISupplier<T> c, boolean trust) {
        cons = c;
        if (other != null)
            other.target = this;
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
        for (T v : array)
            v.exportData(baos);
        return false;
    }
}
