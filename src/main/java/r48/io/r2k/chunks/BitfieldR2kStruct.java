/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import r48.RubyIO;

/**
 * Created on 02/06/17.
 */
public class BitfieldR2kStruct extends ByteR2kStruct {

    // Ascending
    public final String[] flags;

    public BitfieldR2kStruct(String[] f) {
        super(0);
        flags = f;
    }

    @Override
    public RubyIO asRIO() {
        RubyIO r = new RubyIO().setSymlike("__bitfield__", true);
        int pwr = 1;
        for (String s : flags) {
            r.iVars.put(s, new RubyIO().setBool((pwr & value) != 0));
            pwr <<= 1;
        }
        return r;
    }

    @Override
    public void fromRIO(RubyIO src) {
        int pwr = 1;
        value = 0;
        for (String s : flags) {
            if (src.getInstVarBySymbol(s).type == 'T')
                value |= pwr;
            pwr <<= 1;
        }
    }
}
