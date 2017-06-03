/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * just doing some restructuring...
 * Created on 01/06/17.
 */
public interface IR2kInterpretable {
    void importData(InputStream bais) throws IOException;

    // If this returns true, nothing is supposed to be omitted
    boolean exportData(OutputStream baos) throws IOException;
}
