/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * Created on 05/06/17.
 */
public class SVStore extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO str = name.asRIO();
        asRIOISF(str);
        return str;
    }

    @Override
    public void fromRIO(RubyIO src) {
        name.fromRIO(src);
        fromRIOISF(src);
    }
}
