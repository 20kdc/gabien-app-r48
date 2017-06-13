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
 * Battler Animation Data (used by Skill)
 * Created on 06/06/17.
 */
public class BAD extends R2kObject {

    public IntegerR2kStruct moveType = new IntegerR2kStruct(0);
    public IntegerR2kStruct aiType = new IntegerR2kStruct(0);
    public IntegerR2kStruct pose = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x05, moveType, "@move_type"),
                new Index(0x06, aiType, "@has_afterimage"),
                new Index(0x0E, pose, "@pose"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::BattlerAnimationData", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
