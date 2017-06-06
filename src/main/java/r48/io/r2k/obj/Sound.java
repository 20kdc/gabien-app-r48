/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * Created on 06/06/17.
 */
public class Sound extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct volume = new IntegerR2kStruct(100);
    public IntegerR2kStruct tempo = new IntegerR2kStruct(100);
    public IntegerR2kStruct balance = new IntegerR2kStruct(50);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                // you know, I'd almost think this and music are the same class
                // I wonder why
                // (gap in indexes...)
                new Index(0x03, volume, "@volume"),
                new Index(0x04, tempo, "@tempo"),
                new Index(0x05, balance, "@balance")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Sound", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
