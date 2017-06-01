/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.ArrayR2kProp;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.SparseArrayR2kProp;

/**
 * Bare minimum needed to get ChipSet data out for now
 * Created on 01/06/17.
 */
public class Database extends R2kObject {
    public SparseArrayR2kProp<Tileset> tilesets = new SparseArrayR2kProp<Tileset>(new ISupplier<Tileset>() {
        @Override
        public Tileset get() {
            return new Tileset();
        }
    });

    @Override
    public boolean terminatable() {
        return true;
    }

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x14, tilesets)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Database", true);
        mt.iVars.put("@tilesets", tilesets.toRIOArray());
        R2kUtil.unkToRio(mt, unknownChunks);
        return mt;
    }
}
