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
 * As the street-lights are turning on outside...
 * Created on 31/05/17.
 */
public class Music extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct fadeTime = new IntegerR2kStruct(0);
    public IntegerR2kStruct volume = new IntegerR2kStruct(100);
    public IntegerR2kStruct tempo = new IntegerR2kStruct(100);
    public IntegerR2kStruct balance = new IntegerR2kStruct(50);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, fadeTime, "@fadeTime"),
                new Index(0x03, volume, "@volume"),
                new Index(0x04, tempo, "@tempo"),
                new Index(0x05, balance, "@balance")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Music", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
