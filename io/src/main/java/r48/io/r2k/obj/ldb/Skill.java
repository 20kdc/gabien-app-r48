/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import java.util.function.Consumer;

import r48.io.data.DMContext;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.obj.Sound;

/**
 * Created on 05/06/17.
 */
public class Skill extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@description") @DM2LcfBinding(2) @DMCXObject
    public StringR2kStruct description;
    @DMFXOBinding("@use_text_1_2KO") @DM2LcfBinding(3) @DMCXObject
    public StringR2kStruct um1;
    @DMFXOBinding("@use_text_2_2KO") @DM2LcfBinding(4) @DMCXObject
    public StringR2kStruct um2;
    @DMFXOBinding("@failure_message_2KO") @DM2LcfBinding(7) @DMCXInteger(0)
    public IntegerR2kStruct fm;
    @DMFXOBinding("@type") @DM2LcfBinding(8) @DMCXInteger(0)
    public IntegerR2kStruct type;
    @DMFXOBinding("@sp_cost_percent_2k3") @DM2LcfBinding(9) @DMCXBoolean(false)
    public BooleanR2kStruct sp;
    @DMFXOBinding("@sp_cost_val_percent_2k3") @DM2LcfBinding(10) @DMCXInteger(1)
    public IntegerR2kStruct spPercent;
    @DMFXOBinding("@sp_cost_val_normal") @DM2LcfBinding(11) @DMCXInteger(0)
    public IntegerR2kStruct spCost;
    @DMFXOBinding("@scope_n_healing") @DM2LcfBinding(12) @DMCXInteger(0)
    public IntegerR2kStruct scope;
    @DMFXOBinding("@switch_control_target") @DM2LcfBinding(13) @DMCXInteger(1)
    public IntegerR2kStruct switchId;
    @DMFXOBinding("@animation") @DM2LcfBinding(14) @DMCXInteger(0)
    public IntegerR2kStruct animationId;
    @DMFXOBinding("@sound") @DM2LcfBinding(16) @DMCXObject
    public Sound soundEffect;
    @DMFXOBinding("@usable_outside_battle") @DM2LcfBinding(18) @DMCXBoolean(true)
    public BooleanR2kStruct useOutBat;
    @DMFXOBinding("@usable_in_battle") @DM2LcfBinding(19) @DMCXBoolean(false)
    public BooleanR2kStruct useInBat;
    @DMFXOBinding("@add_states_2k3") @DM2LcfBinding(20) @DMCXBoolean(false)
    public BooleanR2kStruct stateAdd;
    @DMFXOBinding("@phys_dmg_frac20") @DM2LcfBinding(21) @DMCXInteger(0)
    public IntegerR2kStruct physRate;
    @DMFXOBinding("@mag_dmg_frac20") @DM2LcfBinding(22) @DMCXInteger(3)
    public IntegerR2kStruct magiRate;
    @DMFXOBinding("@variance") @DM2LcfBinding(23) @DMCXInteger(4)
    public IntegerR2kStruct variRate;
    @DMFXOBinding("@base_dmg") @DM2LcfBinding(24) @DMCXInteger(0)
    public IntegerR2kStruct power;
    @DMFXOBinding("@hit_chance") @DM2LcfBinding(25) @DMCXInteger(100)
    public IntegerR2kStruct hit;
    @DMFXOBinding("@mod_hp") @DM2LcfBinding(31) @DMCXBoolean(false)
    public BooleanR2kStruct affectHp;
    @DMFXOBinding("@mod_sp") @DM2LcfBinding(32) @DMCXBoolean(false)
    public BooleanR2kStruct affectSp;
    @DMFXOBinding("@mod_atk") @DM2LcfBinding(33) @DMCXBoolean(false)
    public BooleanR2kStruct affectAtk;
    @DMFXOBinding("@mod_def") @DM2LcfBinding(34) @DMCXBoolean(false)
    public BooleanR2kStruct affectDef;
    @DMFXOBinding("@mod_spi") @DM2LcfBinding(35) @DMCXBoolean(false)
    public BooleanR2kStruct affectSpi;
    @DMFXOBinding("@mod_agi") @DM2LcfBinding(36) @DMCXBoolean(false)
    public BooleanR2kStruct affectAgi;
    @DMFXOBinding("@steal_enemy_hp") @DM2LcfBinding(37) @DMCXBoolean(false)
    public BooleanR2kStruct absDam;
    @DMFXOBinding("@ignore_def") @DM2LcfBinding(38) @DMCXBoolean(false)
    public BooleanR2kStruct igDef;
    @DMFXOBinding("@mod_states") @DM2LcfSizeBinding(41) @DM2LcfBinding(42)
    public DM2ArraySet<BooleanR2kStruct> sEfx;
    public static Consumer<Skill> sEfx_add = (v) -> v.sEfx = v.boolSet();
    @DMFXOBinding("@mod_by_attributes") @DM2LcfSizeBinding(43) @DM2LcfBinding(44)
    public DM2ArraySet<BooleanR2kStruct> aEfx;
    public static Consumer<Skill> aEfx_add = (v) -> v.aEfx = v.boolSet();
    @DMFXOBinding("@affect_target_attr_defence") @DM2LcfBinding(45) @DMCXBoolean(false)
    public BooleanR2kStruct afAtDef;
    @DMFXOBinding("@OFED_battler_anim_display_actor") @DM2LcfBinding(49) @DMCXInteger(1)
    public IntegerR2kStruct defBattlerAnim;
    @DMFXOBinding("@battler_anim_data") @DM2LcfBinding(50) @DMCXSupplier(BAD.class)
    public DM2SparseArrayH<BAD> battlerAnimMap;
    @DMOptional @DMFXOBinding("@easyrpg_battle_message_2k3") @DM2LcfBinding(0xC9) @DMCXObject
    public StringR2kStruct easyrpgBattleMessage2k3;

    public Skill(DMContext ctx) {
        super(ctx, "RPG::Skill");
    }

    private DM2ArraySet<BooleanR2kStruct> boolSet() {
        return new DM2ArraySet<BooleanR2kStruct>(context) {
            @Override
            public BooleanR2kStruct newValue() {
                return new BooleanR2kStruct(context, false);
            }
        };
    }
}
