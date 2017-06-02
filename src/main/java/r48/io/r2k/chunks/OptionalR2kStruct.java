/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wraps optional things.
 * Created on 01/06/17.
 */
public class OptionalR2kStruct<T extends IR2kStruct> implements IR2kStruct {
    public T instance;
    public ISupplier<T> constructor;

    public OptionalR2kStruct(ISupplier<T> s) {
        constructor = s;
    }

    @Override
    public RubyIO asRIO() {
        if (instance == null)
            return null;
        return instance.asRIO();
    }

    @Override
    public void fromRIO(RubyIO src) {
        if (src == null) {
            instance = null;
        } else {
            instance = constructor.get();
            instance.fromRIO(src);
        }
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        instance = constructor.get();
        instance.importData(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        if (instance == null)
            return true;
        instance.exportData(baos);
        return false;
    }
}
