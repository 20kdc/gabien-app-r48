/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.R2kObject;

/**
 * COPY jun6-2017
 */
public class BattleCommands extends R2kObject {
    @Override
    public Index[] getIndices() {
        return new Index[0];
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::BattleCommands", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
