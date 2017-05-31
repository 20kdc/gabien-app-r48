package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.RubyIO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Just holding a single one.
 * Created on 31/05/17.
 */
public class InterpretableR2kProp<T extends IR2kInterpretable> implements IR2kProp {
    public final ISupplier<T> constructor;
    public T instance;
    public InterpretableR2kProp(ISupplier<T> construct) {
        constructor = construct;
        instance = construct.get();
    }

    @Override
    public byte[] getData() throws IOException {
        throw new IOException("Incomplete");
    }

    @Override
    public void importData(byte[] data) throws IOException {
        instance = constructor.get();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        instance.importData(bais);
        bais.close();
    }
}
