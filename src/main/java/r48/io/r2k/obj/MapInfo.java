/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.R2kObject;

import java.util.HashMap;

/**
 *
 * Created on 31/05/17.
 */
public class MapInfo extends R2kObject {
    @Override
    public Index[] getIndices() {
        return new Index[] {};
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::MapInfo", true);
        R2kUtil.unkToRio(mt, unknownChunks);
        return mt;
    }
}
