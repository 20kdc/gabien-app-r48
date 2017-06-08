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
public class BattlerAnimation extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct speed = new IntegerR2kStruct(0);
    public SparseArrayAR2kStruct<BAE> baseData = new SparseArrayAR2kStruct<BAE>(new ISupplier<BAE>() {
        @Override
        public BAE get() {
            return new BAE();
        }
    });
    public SparseArrayAR2kStruct<BAE> weaponData = new SparseArrayAR2kStruct<BAE>(new ISupplier<BAE>() {
        @Override
        public BAE get() {
            return new BAE();
        }
    });
    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, speed, "@speed"),
                new Index(0x0A, baseData, "@base_data"),
                new Index(0x0B, weaponData, "@weapon_data"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::BattlerAnimationSet", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }

    // ... #unintentionalJokeOfTheDay
    public static class BAE extends R2kObject {
        public StringR2kStruct name = new StringR2kStruct();
        public StringR2kStruct battlerName = new StringR2kStruct();
        public IntegerR2kStruct battlerIndex = new IntegerR2kStruct(0);
        public IntegerR2kStruct animationType = new IntegerR2kStruct(0);
        public IntegerR2kStruct animationId = new IntegerR2kStruct(1);

        @Override
        public Index[] getIndices() {
            return new Index[] {
                    new Index(0x01, name, "@name"),
                    new Index(0x02, battlerName, "@battler_name"),
                    new Index(0x03, battlerIndex, "@battler_index"),
                    new Index(0x04, animationType, "@type"),
                    new Index(0x05, animationId, "@animation_id")
            };
        }

        @Override
        public RubyIO asRIO() {
            RubyIO rio = new RubyIO().setSymlike("RPG::BattlerAnimation", true);
            asRIOISF(rio);
            return rio;
        }

        @Override
        public void fromRIO(RubyIO src) {
            fromRIOISF(src);
        }
    }
}
