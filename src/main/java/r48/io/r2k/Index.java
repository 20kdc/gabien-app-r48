/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k;

import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.IR2kStruct;

/**
 * Created on 31/05/17.
 */
public class Index {
    public final int index;
    public final IR2kInterpretable chunk;
    public final String rioHelperName;

    public Index(int i, IR2kInterpretable c) {
        index = i;
        chunk = c;
        rioHelperName = null;
    }

    public Index(int i, IR2kStruct c, String rhn) {
        index = i;
        chunk = c;
        rioHelperName = rhn;
    }

    @Override
    public String toString() {
        if (rioHelperName != null)
            return "0x" + Integer.toHexString(index) + " -> " + rioHelperName;
        return "0x" + Integer.toHexString(index);
    }
}
