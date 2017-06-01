/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IntegerR2kProp;
import r48.io.r2k.chunks.R2kObject;

/**
 * the world's most boring class
 * Created on 01/06/17.
 */
public class Encounter extends R2kObject {

    public IntegerR2kProp troop = new IntegerR2kProp(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, troop)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Encounter", true);
        mt.iVars.put("@troop", new RubyIO().setFX(troop.i));
        R2kUtil.unkToRio(mt, unknownChunks);
        return mt;
    }
}
