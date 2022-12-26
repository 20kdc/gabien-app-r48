/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Not really much in this of use.
 * Supposed to be a class for Colors/Tones, but in practice it might as well be the base class of RubyTable.
 * It isn't (no reason to - if the userdata gets out of hand,
 * then I'll stuff anything beginning with Ruby in it's own package,
 * and make this the baseclass of userdata wrappers), but it might as well be.
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
