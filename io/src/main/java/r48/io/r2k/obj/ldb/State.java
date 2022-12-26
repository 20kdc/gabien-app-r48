/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class State extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(0x01) @DM2LcfObject
    public StringR2kStruct name;
    @DM2FXOBinding("@persists") @DM2LcfBinding(0x02) @DM2LcfInteger(0)
    public IntegerR2kStruct type;
    @DM2FXOBinding("@colour") @DM2LcfBinding(0x03) @DM2LcfInteger(6)
    public IntegerR2kStruct colour;
    @DM2FXOBinding("@priority") @DM2LcfBinding(0x04) @DM2LcfInteger(50)
    public IntegerR2kStruct priority;
    @DM2FXOBinding("@restriction") @DM2LcfBinding(0x05) @DM2LcfInteger(0)
    public IntegerR2kStruct restriction;
    @DM2FXOBinding("@a_rate") @DM2LcfBinding(0x0B) @DM2LcfInteger(100)
    public IntegerR2kStruct aRate;
    @DM2FXOBinding("@b_rate") @DM2LcfBinding(0x0C) @DM2LcfInteger(80)
    public IntegerR2kStruct bRate;
    @DM2FXOBinding("@c_rate") @DM2LcfBinding(0x0D) @DM2LcfInteger(60)
    public IntegerR2kStruct cRate;
    @DM2FXOBinding("@d_rate") @DM2LcfBinding(0x0E) @DM2LcfInteger(30)
    public IntegerR2kStruct dRate;
    @DM2FXOBinding("@e_rate") @DM2LcfBinding(0x0F) @DM2LcfInteger(0)
    public IntegerR2kStruct eRate;
    @DM2FXOBinding("@hold_turn") @DM2LcfBinding(0x15) @DM2LcfInteger(0)
    public IntegerR2kStruct holdTurn;
    @DM2FXOBinding("@auto_release_prob") @DM2LcfBinding(0x16) @DM2LcfInteger(0)
    public IntegerR2kStruct autoReleaseProb;
    @DM2FXOBinding("@release_by_damage") @DM2LcfBinding(0x17) @DM2LcfInteger(0)
    public IntegerR2kStruct releaseByDamage;
    @DM2FXOBinding("@affect_type_2k3") @DM2LcfBinding(0x1E) @DM2LcfInteger(0)
    public IntegerR2kStruct affectType;
    @DM2FXOBinding("@affect_atk") @DM2LcfBinding(0x1F) @DM2LcfBoolean(false)
    public BooleanR2kStruct affectAtk;
    @DM2FXOBinding("@affect_def") @DM2LcfBinding(0x20) @DM2LcfBoolean(false)
    public BooleanR2kStruct affectDef;
    @DM2FXOBinding("@affect_spi") @DM2LcfBinding(0x21) @DM2LcfBoolean(false)
    public BooleanR2kStruct affectSpi;
    @DM2FXOBinding("@affect_agi") @DM2LcfBinding(0x22) @DM2LcfBoolean(false)
    public BooleanR2kStruct affectAgi;
    @DM2FXOBinding("@reduce_hit_ratio") @DM2LcfBinding(0x23) @DM2LcfInteger(100)
    public IntegerR2kStruct reduceHitRatio;
    @DM2FXOBinding("@avoid_attacks_2k3") @DM2LcfBinding(0x24) @DM2LcfBoolean(false)
    public BooleanR2kStruct avoidAttacks;
    @DM2FXOBinding("@reflect_magic_2k3") @DM2LcfBinding(0x25) @DM2LcfBoolean(false)
    public BooleanR2kStruct reflectMagic;
    @DM2FXOBinding("@cursed_2k3") @DM2LcfBinding(0x26) @DM2LcfBoolean(false)
    public BooleanR2kStruct cursed;
    @DM2FXOBinding("@battler_pose_2k3") @DM2LcfBinding(0x27) @DM2LcfInteger(100)
    public IntegerR2kStruct battlerAnimationId;
    @DM2FXOBinding("@restrict_skill") @DM2LcfBinding(0x29) @DM2LcfBoolean(false)
    public BooleanR2kStruct restrictSkill;
    @DM2FXOBinding("@restrict_skill_level") @DM2LcfBinding(0x2A) @DM2LcfInteger(100)
    public IntegerR2kStruct restrictSkillLevel;
    @DM2FXOBinding("@restrict_magic") @DM2LcfBinding(0x2B) @DM2LcfBoolean(false)
    public BooleanR2kStruct restrictMagic;
    @DM2FXOBinding("@restrict_magic_level") @DM2LcfBinding(0x2C) @DM2LcfInteger(0)
    public IntegerR2kStruct restrictMagicLevel;
    @DM2FXOBinding("@hp_change_type") @DM2LcfBinding(0x2D) @DM2LcfInteger(0)
    public IntegerR2kStruct hpChangeType;
    @DM2FXOBinding("@sp_change_type") @DM2LcfBinding(0x2E) @DM2LcfInteger(0)
    public IntegerR2kStruct spChangeType;
    @DM2FXOBinding("@msg_actor") @DM2LcfBinding(0x33) @DM2LcfObject
    public StringR2kStruct msgActor;
    @DM2FXOBinding("@msg_enemy") @DM2LcfBinding(0x34) @DM2LcfObject
    public StringR2kStruct msgEnemy;
    @DM2FXOBinding("@msg_already") @DM2LcfBinding(0x35) @DM2LcfObject
    public StringR2kStruct msgAlready;
    @DM2FXOBinding("@msg_affected") @DM2LcfBinding(0x36) @DM2LcfObject
    public StringR2kStruct msgAffected;
    @DM2FXOBinding("@msg_recovery") @DM2LcfBinding(0x37) @DM2LcfObject
    public StringR2kStruct msgRecovery;

    @DM2FXOBinding("@hp_change_max") @DM2LcfBinding(0x3D) @DM2LcfInteger(0)
    public IntegerR2kStruct hpChangeMax;
    @DM2FXOBinding("@hp_change_val") @DM2LcfBinding(0x3E) @DM2LcfInteger(0)
    public IntegerR2kStruct hpChangeVal;
    @DM2FXOBinding("@hp_change_map_steps") @DM2LcfBinding(0x3F) @DM2LcfInteger(0)
    public IntegerR2kStruct hpChangeMapSteps;
    @DM2FXOBinding("@hp_change_map_val") @DM2LcfBinding(0x40) @DM2LcfInteger(0)
    public IntegerR2kStruct hpChangeMapVal;

    @DM2FXOBinding("@sp_change_max") @DM2LcfBinding(0x41) @DM2LcfInteger(0)
    public IntegerR2kStruct spChangeMax;
    @DM2FXOBinding("@sp_change_val") @DM2LcfBinding(0x42) @DM2LcfInteger(0)
    public IntegerR2kStruct spChangeVal;
    @DM2FXOBinding("@sp_change_map_steps") @DM2LcfBinding(0x43) @DM2LcfInteger(0)
    public IntegerR2kStruct spChangeMapSteps;
    @DM2FXOBinding("@sp_change_map_val") @DM2LcfBinding(0x44) @DM2LcfInteger(0)
    public IntegerR2kStruct spChangeMapVal;

    public State() {
        super("RPG::State");
    }
}
