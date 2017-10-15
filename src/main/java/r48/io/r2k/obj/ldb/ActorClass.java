/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
import r48.io.r2k.struct.BPB;

/**
 * COPY jun6-2017
 */
public class ActorClass extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public BooleanR2kStruct dualWield = new BooleanR2kStruct(false);
    public BooleanR2kStruct lockEquipment = new BooleanR2kStruct(false);
    public BooleanR2kStruct autoBattle = new BooleanR2kStruct(false);
    public BooleanR2kStruct superGuard = new BooleanR2kStruct(false);
    public BPB parameters = new BPB();
    public IntegerR2kStruct initLevelExp = new IntegerR2kStruct(300);
    public IntegerR2kStruct eachLevelExpP = new IntegerR2kStruct(300);
    public IntegerR2kStruct eachLevelExpModC = new IntegerR2kStruct(0);
    public IntegerR2kStruct battlerAnimation = new IntegerR2kStruct(0);
    public SparseArrayAR2kStruct<Learning> learnSkills = new SparseArrayAR2kStruct<Learning>(new ISupplier<Learning>() {
        @Override
        public Learning get() {
            return new Learning();
        }
    });
    public ArraySizeR2kInterpretable<ByteR2kStruct> stateRanksSz = new ArraySizeR2kInterpretable<ByteR2kStruct>();
    public ArraySetR2kStruct<ByteR2kStruct> stateRanks = new ArraySetR2kStruct<ByteR2kStruct>(stateRanksSz, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(2);
        }
    }, true);
    public ArraySizeR2kInterpretable<ByteR2kStruct> attrRanksSz = new ArraySizeR2kInterpretable<ByteR2kStruct>();
    public ArraySetR2kStruct<ByteR2kStruct> attrRanks = new ArraySetR2kStruct<ByteR2kStruct>(attrRanksSz, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(2);
        }
    }, true);
    public ArrayR2kStruct<Int32R2kStruct> battleCommands = new ArrayR2kStruct<Int32R2kStruct>(null, new ISupplier<Int32R2kStruct>() {
        @Override
        public Int32R2kStruct get() {
            return new Int32R2kStruct(0);
        }
    }, true);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x15, dualWield, "@dual_wield"),
                new Index(0x16, lockEquipment, "@lock_equipment"),
                new Index(0x17, autoBattle, "@battle_auto"),
                new Index(0x18, superGuard, "@battle_super_guard"),
                new Index(0x1F, parameters, "@battle_parameters"),
                new Index(0x29, initLevelExp, "@init_level_exp"),
                new Index(0x2A, eachLevelExpP, "@each_level_exp_mul"),
                new Index(0x2B, eachLevelExpModC, "@each_level_exp_add"),
                new Index(0x3E, battlerAnimation, "@battler_animation"),
                new Index(0x3F, learnSkills, "@learn_skills"),
                new Index(0x47, stateRanksSz),
                new Index(0x48, stateRanks, "@state_ranks"),
                new Index(0x49, attrRanksSz),
                new Index(0x4A, attrRanks, "@element_ranks"),
                new Index(0x50, battleCommands, "@battle_commands")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Class", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
