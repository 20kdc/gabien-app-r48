/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import r48.RubyIO;

/**
 * Created on 02/06/17.
 */
public class BooleanR2kStruct extends IntegerR2kStruct {
    public BooleanR2kStruct(boolean i2) {
        super(i2 ? 1 : 0);
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setBool(i != 0);
    }

    @Override
    public void fromRIO(RubyIO src) {
        i = src.type == 'T' ? 1 : 0;
    }

}
