/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;

/**
 * Created on 05/06/17.
 */
public class Learning extends R2kObject {
    public IntegerR2kStruct level = new IntegerR2kStruct(1);
    public IntegerR2kStruct skill = new IntegerR2kStruct(1);

    // Skill = Blob;2b 01 04
    public boolean disableSanity() {
        return true;
    }

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, level, "@level"),
                new Index(0x02, skill, "@skill")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Learning", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
