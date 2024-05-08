/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DM2Optional;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.*;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.BPB;

/**
 * Common things between Actor and ActorClass
 * Created on December 06, 2018.
 */
public class ActorClassBase extends DM2R2kObject {

    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DM2FXOBinding("@dual_wield") @DM2LcfBinding(21) @DMCXBoolean(false)
    public BooleanR2kStruct dualWield;
    @DM2FXOBinding("@lock_equipment") @DM2LcfBinding(22) @DMCXBoolean(false)
    public BooleanR2kStruct lockEquipment;
    @DM2FXOBinding("@battle_auto") @DM2LcfBinding(23) @DMCXBoolean(false)
    public BooleanR2kStruct autoBattle;
    @DM2FXOBinding("@battle_super_guard") @DM2LcfBinding(24) @DMCXBoolean(false)
    public BooleanR2kStruct superGuard;

    @DM2FXOBinding("@battle_parameters") @DM2LcfBinding(31)
    public BPB parameters;

    @DM2Optional @DM2FXOBinding("@init_level_exp") @DM2LcfBinding(41) @DMCXInteger(0)
    public IntegerR2kStruct initLevelExp;
    @DM2Optional @DM2FXOBinding("@each_level_exp_mul") @DM2LcfBinding(42) @DMCXInteger(0)
    public IntegerR2kStruct eachLevelExpP;
    @DM2FXOBinding("@each_level_exp_add") @DM2LcfBinding(43) @DMCXInteger(0)
    public IntegerR2kStruct eachLevelExpModC;

    // 1 or 0...? Different in each.
    @DM2FXOBinding("@battler_anim_2k3") @DM2LcfBinding(62)
    public IntegerR2kStruct battlerAnimation;

    @DM2FXOBinding("@learn_skills") @DM2LcfBinding(63)
    public DM2SparseArrayA<Learning> learnSkills;

    @DM2FXOBinding("@state_ranks") @DM2LcfSizeBinding(71) @DM2LcfBinding(72)
    public DM2ArraySet<ByteR2kStruct> stateRanks;
    @DM2FXOBinding("@attr_ranks") @DM2LcfSizeBinding(73) @DM2LcfBinding(74)
    public DM2ArraySet<ByteR2kStruct> attrRanks;

    @DM2FXOBinding("@battle_commands_2k3") @DM2LcfBinding(80)
    public DM2Array<Int32R2kStruct> battleCommands;

    private final int battlerAnimationDefault;

    public ActorClassBase(DMContext ctx, String sym, int bad1) {
        super(ctx, sym);
        battlerAnimationDefault = bad1;
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@battle_parameters"))
            return parameters = new BPB(dm2Ctx);
        if (sym.equals("@learn_skills"))
            return learnSkills = new DM2SparseArrayA<Learning>(dm2Ctx, () -> new Learning(dm2Ctx));
        if (sym.equals("@state_ranks"))
            return stateRanks = byteSet();
        if (sym.equals("@attr_ranks"))
            return attrRanks = byteSet();
        if (sym.equals("@battle_commands_2k3"))
            return battleCommands = new DM2Array<Int32R2kStruct>(dm2Ctx) {
                @Override
                public Int32R2kStruct newValue() {
                    return new Int32R2kStruct(dm2Ctx, 0);
                }
            };
        if (sym.equals("@battler_anim_2k3"))
            return battlerAnimation = new IntegerR2kStruct(dm2Ctx, battlerAnimationDefault);
        return super.dm2AddIVar(sym);
    }

    private DM2ArraySet<ByteR2kStruct> byteSet() {
        return new DM2ArraySet<ByteR2kStruct>(dm2Ctx) {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(dm2Ctx, 2);
            }
        };
    }
}
