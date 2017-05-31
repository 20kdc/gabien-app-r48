/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k;

import java.io.IOException;

/**
 * Ugggh. I blame 'LCF', whatever that means, for everything.
 * Created on 31/05/17.
 */
public interface IR2kProp {
    byte[] getData() throws IOException;
    void importData(byte[] data) throws IOException;
}
