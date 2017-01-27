/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Not really much in this of use.
 * Supposed to be a class for Colors/Tones, but in practice it might as well be the base class of RubyTable.
 * It isn't (no reason to - if the userdata gets out of hand,
 *  then I'll stuff anything beginning with Ruby in it's own package,
 *  and make this the baseclass of userdata wrappers), but it might as well be.
 * Created on 1/3/17.
 */
public class RubyCT {
    public final byte[] innerBytes;
    public final ByteBuffer innerTable;
    public RubyCT(byte[] data) {
        innerBytes = data;
        innerTable = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }
}
