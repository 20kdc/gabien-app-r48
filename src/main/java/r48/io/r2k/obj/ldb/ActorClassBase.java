/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.BPB;

/**
 * Common things between Actor and ActorClass
 * Created on December 06, 2018.
 */
public class ActorClassBase extends DM2R2kObject {

    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DM2LcfObject
    public StringR2kStruct name = new StringR2kStruct();
    @DM2FXOBinding("@dual_wield") @DM2LcfBinding(21) @DM2LcfBoolean(false)
    public BooleanR2kStruct dualWield;
    @DM2FXOBinding("@lock_equipment") @DM2LcfBinding(22) @DM2LcfBoolean(false)
    public BooleanR2kStruct lockEquipment;
    @DM2FXOBinding("@battle_auto") @DM2LcfBinding(23) @DM2LcfBoolean(false)
    public BooleanR2kStruct autoBattle;
    @DM2FXOBinding("@battle_super_guard") @DM2LcfBinding(24) @DM2LcfBoolean(false)
    public BooleanR2kStruct superGuard;

    @DM2FXOBinding("@battle_parameters") @DM2LcfBinding(31)
    public BPB parameters;

    @DM2FXOBinding("@battler_animation") @DM2LcfBinding(62) @DM2LcfInteger(0)
    public IntegerR2kStruct battlerAnimation;
    @DM2FXOBinding("@learn_skills") @DM2LcfBinding(63)
    public DM2SparseArrayA<Learning> learnSkills;


//                new Index(0x47, stateRanksSz),
//                new Index(0x48, stateRanks, "@state_ranks"),
//                new Index(0x49, attrRanksSz),
//                new Index(0x4A, attrRanks, "@element_ranks"),
//                new Index(0x50, battleCommands, "@battle_commands_2k3")

    public ActorClassBase(String sym) {
        super(sym);
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@battle_parameters"))
            return parameters = new BPB();
        if (sym.equals("@learn_skills"))
            return learnSkills = new DM2SparseArrayA<Learning>(new ISupplier<Learning>() {
                @Override
                public Learning get() {
                    return new Learning();
                }
            });
        return super.dm2AddIVar(sym);
    }
}
