/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.R2kUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created on 31/05/17.
 */
public class ArrayR2kProp<T extends IR2kInterpretable> implements IR2kProp {
    public LinkedList<T> array = new LinkedList<T>();
    public final ISupplier<T> cons;
    public ArrayR2kProp(ArraySizeR2kProp<T> other, ISupplier<T> c) {
        cons = c;
        if (other != null)
            other.target = this;
    }
    @Override
    public byte[] getData() throws IOException {
        throw new IOException("Incomplete");
    }

    @Override
    public void importData(byte[] data) throws IOException {
        array.clear();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        while (bais.available() > 0) {
            T v = cons.get();
            v.importData(bais);
            array.add(v);
        }
        bais.close();
    }

    public RubyIO toRIOArray() {
        RubyIO rio = new RubyIO();
        rio.type = '[';
        rio.arrVal = new RubyIO[array.size()];
        for (int i = 0; i < array.size(); i++)
            rio.arrVal[i] = array.get(i).asRIO();
        return rio;
    }
}
