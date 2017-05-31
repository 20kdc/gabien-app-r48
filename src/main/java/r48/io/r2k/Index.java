/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k;

import r48.io.r2k.chunks.IR2kProp;

/**
 * Created on 31/05/17.
 */
public class Index {
    public final int index;
    public final IR2kProp chunk;
    public Index(int i, IR2kProp c) {
        index = i;
        chunk = c;
    }
}
