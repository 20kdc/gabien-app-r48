/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
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

    @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@dual_wield") @DM2LcfBinding(21) @DMCXBoolean(false)
    public BooleanR2kStruct dualWield;
    @DMFXOBinding("@lock_equipment") @DM2LcfBinding(22) @DMCXBoolean(false)
    public BooleanR2kStruct lockEquipment;
    @DMFXOBinding("@battle_auto") @DM2LcfBinding(23) @DMCXBoolean(false)
    public BooleanR2kStruct autoBattle;
    @DMFXOBinding("@battle_super_guard") @DM2LcfBinding(24) @DMCXBoolean(false)
    public BooleanR2kStruct superGuard;

    @DMFXOBinding("@battle_parameters") @DM2LcfBinding(31)
    public BPB parameters;

    @DMOptional @DMFXOBinding("@init_level_exp") @DM2LcfBinding(41) @DMCXInteger(0)
    public IntegerR2kStruct initLevelExp;
    @DMOptional @DMFXOBinding("@each_level_exp_mul") @DM2LcfBinding(42) @DMCXInteger(0)
    public IntegerR2kStruct eachLevelExpP;
    @DMFXOBinding("@each_level_exp_add") @DM2LcfBinding(43) @DMCXInteger(0)
    public IntegerR2kStruct eachLevelExpModC;

    // 1 or 0...? Different in each.
    @DMFXOBinding("@battler_anim_2k3") @DM2LcfBinding(62)
    public IntegerR2kStruct battlerAnimation;

    @DMFXOBinding("@learn_skills") @DM2LcfBinding(63)
    public DM2SparseArrayA<Learning> learnSkills;

    @DMFXOBinding("@state_ranks") @DM2LcfSizeBinding(71) @DM2LcfBinding(72)
    public DM2ArraySet<ByteR2kStruct> stateRanks;
    @DMFXOBinding("@attr_ranks") @DM2LcfSizeBinding(73) @DM2LcfBinding(74)
    public DM2ArraySet<ByteR2kStruct> attrRanks;

    @DMFXOBinding("@battle_commands_2k3") @DM2LcfBinding(80)
    public DM2Array<Int32R2kStruct> battleCommands;

    private final int battlerAnimationDefault;

    public ActorClassBase(DMContext ctx, String sym, int bad1) {
        super(ctx, sym);
        battlerAnimationDefault = bad1;
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@battle_parameters"))
            return parameters = new BPB(context);
        if (sym.equals("@learn_skills"))
            return learnSkills = new DM2SparseArrayA<Learning>(context, () -> new Learning(context));
        if (sym.equals("@state_ranks"))
            return stateRanks = byteSet();
        if (sym.equals("@attr_ranks"))
            return attrRanks = byteSet();
        if (sym.equals("@battle_commands_2k3"))
            return battleCommands = new DM2Array<Int32R2kStruct>(context) {
                @Override
                public Int32R2kStruct newValue() {
                    return new Int32R2kStruct(context, 0);
                }
            };
        if (sym.equals("@battler_anim_2k3"))
            return battlerAnimation = new IntegerR2kStruct(context, battlerAnimationDefault);
        return super.dm2AddIVar(sym);
    }

    private DM2ArraySet<ByteR2kStruct> byteSet() {
        return new DM2ArraySet<ByteR2kStruct>(context) {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(context, 2);
            }
        };
    }
}
