/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class State extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(0x01) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@persists") @DM2LcfBinding(0x02) @DMCXInteger(0)
    public IntegerR2kStruct type;
    @DMFXOBinding("@colour") @DM2LcfBinding(0x03) @DMCXInteger(6)
    public IntegerR2kStruct colour;
    @DMFXOBinding("@priority") @DM2LcfBinding(0x04) @DMCXInteger(50)
    public IntegerR2kStruct priority;
    @DMFXOBinding("@restriction") @DM2LcfBinding(0x05) @DMCXInteger(0)
    public IntegerR2kStruct restriction;
    @DMFXOBinding("@a_rate") @DM2LcfBinding(0x0B) @DMCXInteger(100)
    public IntegerR2kStruct aRate;
    @DMFXOBinding("@b_rate") @DM2LcfBinding(0x0C) @DMCXInteger(80)
    public IntegerR2kStruct bRate;
    @DMFXOBinding("@c_rate") @DM2LcfBinding(0x0D) @DMCXInteger(60)
    public IntegerR2kStruct cRate;
    @DMFXOBinding("@d_rate") @DM2LcfBinding(0x0E) @DMCXInteger(30)
    public IntegerR2kStruct dRate;
    @DMFXOBinding("@e_rate") @DM2LcfBinding(0x0F) @DMCXInteger(0)
    public IntegerR2kStruct eRate;
    @DMFXOBinding("@hold_turn") @DM2LcfBinding(0x15) @DMCXInteger(0)
    public IntegerR2kStruct holdTurn;
    @DMFXOBinding("@auto_release_prob") @DM2LcfBinding(0x16) @DMCXInteger(0)
    public IntegerR2kStruct autoReleaseProb;
    @DMFXOBinding("@release_by_damage") @DM2LcfBinding(0x17) @DMCXInteger(0)
    public IntegerR2kStruct releaseByDamage;
    @DMFXOBinding("@affect_type_2k3") @DM2LcfBinding(0x1E) @DMCXInteger(0)
    public IntegerR2kStruct affectType;
    @DMFXOBinding("@affect_atk") @DM2LcfBinding(0x1F) @DMCXBoolean(false)
    public BooleanR2kStruct affectAtk;
    @DMFXOBinding("@affect_def") @DM2LcfBinding(0x20) @DMCXBoolean(false)
    public BooleanR2kStruct affectDef;
    @DMFXOBinding("@affect_spi") @DM2LcfBinding(0x21) @DMCXBoolean(false)
    public BooleanR2kStruct affectSpi;
    @DMFXOBinding("@affect_agi") @DM2LcfBinding(0x22) @DMCXBoolean(false)
    public BooleanR2kStruct affectAgi;
    @DMFXOBinding("@reduce_hit_ratio") @DM2LcfBinding(0x23) @DMCXInteger(100)
    public IntegerR2kStruct reduceHitRatio;
    @DMFXOBinding("@avoid_attacks_2k3") @DM2LcfBinding(0x24) @DMCXBoolean(false)
    public BooleanR2kStruct avoidAttacks;
    @DMFXOBinding("@reflect_magic_2k3") @DM2LcfBinding(0x25) @DMCXBoolean(false)
    public BooleanR2kStruct reflectMagic;
    @DMFXOBinding("@cursed_2k3") @DM2LcfBinding(0x26) @DMCXBoolean(false)
    public BooleanR2kStruct cursed;
    @DMFXOBinding("@battler_pose_2k3") @DM2LcfBinding(0x27) @DMCXInteger(100)
    public IntegerR2kStruct battlerAnimationId;
    @DMFXOBinding("@restrict_skill") @DM2LcfBinding(0x29) @DMCXBoolean(false)
    public BooleanR2kStruct restrictSkill;
    @DMFXOBinding("@restrict_skill_level") @DM2LcfBinding(0x2A) @DMCXInteger(100)
    public IntegerR2kStruct restrictSkillLevel;
    @DMFXOBinding("@restrict_magic") @DM2LcfBinding(0x2B) @DMCXBoolean(false)
    public BooleanR2kStruct restrictMagic;
    @DMFXOBinding("@restrict_magic_level") @DM2LcfBinding(0x2C) @DMCXInteger(0)
    public IntegerR2kStruct restrictMagicLevel;
    @DMFXOBinding("@hp_change_type") @DM2LcfBinding(0x2D) @DMCXInteger(0)
    public IntegerR2kStruct hpChangeType;
    @DMFXOBinding("@sp_change_type") @DM2LcfBinding(0x2E) @DMCXInteger(0)
    public IntegerR2kStruct spChangeType;
    @DMFXOBinding("@msg_actor") @DM2LcfBinding(0x33) @DMCXObject
    public StringR2kStruct msgActor;
    @DMFXOBinding("@msg_enemy") @DM2LcfBinding(0x34) @DMCXObject
    public StringR2kStruct msgEnemy;
    @DMFXOBinding("@msg_already") @DM2LcfBinding(0x35) @DMCXObject
    public StringR2kStruct msgAlready;
    @DMFXOBinding("@msg_affected") @DM2LcfBinding(0x36) @DMCXObject
    public StringR2kStruct msgAffected;
    @DMFXOBinding("@msg_recovery") @DM2LcfBinding(0x37) @DMCXObject
    public StringR2kStruct msgRecovery;

    @DMFXOBinding("@hp_change_max") @DM2LcfBinding(0x3D) @DMCXInteger(0)
    public IntegerR2kStruct hpChangeMax;
    @DMFXOBinding("@hp_change_val") @DM2LcfBinding(0x3E) @DMCXInteger(0)
    public IntegerR2kStruct hpChangeVal;
    @DMFXOBinding("@hp_change_map_steps") @DM2LcfBinding(0x3F) @DMCXInteger(0)
    public IntegerR2kStruct hpChangeMapSteps;
    @DMFXOBinding("@hp_change_map_val") @DM2LcfBinding(0x40) @DMCXInteger(0)
    public IntegerR2kStruct hpChangeMapVal;

    @DMFXOBinding("@sp_change_max") @DM2LcfBinding(0x41) @DMCXInteger(0)
    public IntegerR2kStruct spChangeMax;
    @DMFXOBinding("@sp_change_val") @DM2LcfBinding(0x42) @DMCXInteger(0)
    public IntegerR2kStruct spChangeVal;
    @DMFXOBinding("@sp_change_map_steps") @DM2LcfBinding(0x43) @DMCXInteger(0)
    public IntegerR2kStruct spChangeMapSteps;
    @DMFXOBinding("@sp_change_map_val") @DM2LcfBinding(0x44) @DMCXInteger(0)
    public IntegerR2kStruct spChangeMapVal;

    public State(DMContext ctx) {
        super(ctx, "RPG::State");
    }
}
