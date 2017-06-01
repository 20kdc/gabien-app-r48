/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kProp;

/**
 * Another bare-minimum for now
 * Created on 01/06/17.
 */
public class Tileset extends R2kObject {

    public StringR2kProp tilesetName = new StringR2kProp();

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x02, tilesetName)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Tileset", true);
        mt.iVars.put("@tileset_name", new RubyIO().setString(tilesetName.data));
        R2kUtil.unkToRio(mt, unknownChunks);
        return mt;
    }
}
