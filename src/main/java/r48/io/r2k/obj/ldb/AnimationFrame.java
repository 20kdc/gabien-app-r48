/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.SparseArrayAR2kStruct;

/**
 * Created on 07/06/17.
 */
public class AnimationFrame extends R2kObject {
    public SparseArrayAR2kStruct<AnimationCell> cells = new SparseArrayAR2kStruct<AnimationCell>(new ISupplier<AnimationCell>() {
        @Override
        public AnimationCell get() {
            return new AnimationCell();
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, cells, "@cells")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Animation::Frame", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
