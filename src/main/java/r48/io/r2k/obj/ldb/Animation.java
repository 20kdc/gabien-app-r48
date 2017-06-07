/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.SparseArrayAR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * COPY jun6-2017
 */
public class Animation extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct animationName = new StringR2kStruct();
    public IntegerR2kStruct unknown3 = new IntegerR2kStruct(-1);
    public SparseArrayAR2kStruct<AnimationTiming> timings = new SparseArrayAR2kStruct<AnimationTiming>(new ISupplier<AnimationTiming>() {
        @Override
        public AnimationTiming get() {
            return new AnimationTiming();
        }
    });
    public IntegerR2kStruct scope = new IntegerR2kStruct(0);
    public IntegerR2kStruct position = new IntegerR2kStruct(2);
    public SparseArrayAR2kStruct<AnimationFrame> frames = new SparseArrayAR2kStruct<AnimationFrame>(new ISupplier<AnimationFrame>() {
        @Override
        public AnimationFrame get() {
            return new AnimationFrame();
        }
    });
    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, animationName, "@animation_name"),
                new Index(0x03, unknown3, "@unknown_03"),
                new Index(0x06, timings, "@timings"),
                new Index(0x09, scope, "@scope"),
                new Index(0x0A, position, "@position"),
                new Index(0x0C, frames, "@frames"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Animation", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
