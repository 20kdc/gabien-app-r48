/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * COPY jun6-2017
 */
public class State extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct type = new IntegerR2kStruct(0);
    public IntegerR2kStruct colour = new IntegerR2kStruct(6);
    public IntegerR2kStruct priority = new IntegerR2kStruct(50);
    public IntegerR2kStruct restriction = new IntegerR2kStruct(0);
    public IntegerR2kStruct aRate = new IntegerR2kStruct(100);
    public IntegerR2kStruct bRate = new IntegerR2kStruct(80);
    public IntegerR2kStruct cRate = new IntegerR2kStruct(60);
    public IntegerR2kStruct dRate = new IntegerR2kStruct(30);
    public IntegerR2kStruct eRate = new IntegerR2kStruct(0);
    public IntegerR2kStruct holdTurn = new IntegerR2kStruct(0);
    public IntegerR2kStruct autoReleaseProb = new IntegerR2kStruct(0);
    public IntegerR2kStruct releaseByDamage = new IntegerR2kStruct(0);
    public IntegerR2kStruct affectType = new IntegerR2kStruct(0);
    public BooleanR2kStruct affectAtk = new BooleanR2kStruct(false);
    public BooleanR2kStruct affectDef = new BooleanR2kStruct(false);
    public BooleanR2kStruct affectSpi = new BooleanR2kStruct(false);
    public BooleanR2kStruct affectAgi = new BooleanR2kStruct(false);
    public IntegerR2kStruct reduceHitRatio = new IntegerR2kStruct(100);
    public BooleanR2kStruct avoidAttacks = new BooleanR2kStruct(false);
    public BooleanR2kStruct reflectMagic = new BooleanR2kStruct(false);
    public BooleanR2kStruct cursed = new BooleanR2kStruct(false);
    public IntegerR2kStruct battlerAnimationId = new IntegerR2kStruct(100);
    public BooleanR2kStruct restrictSkill = new BooleanR2kStruct(false);
    public IntegerR2kStruct restrictSkillLevel = new IntegerR2kStruct(0);
    public BooleanR2kStruct restrictMagic = new BooleanR2kStruct(false);
    public IntegerR2kStruct restrictMagicLevel = new IntegerR2kStruct(0);
    public IntegerR2kStruct hpChangeType = new IntegerR2kStruct(0);
    public IntegerR2kStruct spChangeType = new IntegerR2kStruct(0);
    public StringR2kStruct msgActor = new StringR2kStruct();
    public StringR2kStruct msgEnemy = new StringR2kStruct();
    public StringR2kStruct msgAlready = new StringR2kStruct();
    public StringR2kStruct msgAffected = new StringR2kStruct();
    public StringR2kStruct msgRecovery = new StringR2kStruct();
    public IntegerR2kStruct hpChangeMax = new IntegerR2kStruct(0);
    public IntegerR2kStruct hpChangeVal = new IntegerR2kStruct(0);
    public IntegerR2kStruct hpChangeMapVal = new IntegerR2kStruct(0);
    public IntegerR2kStruct hpChangeMapSteps = new IntegerR2kStruct(0);
    public IntegerR2kStruct spChangeMax = new IntegerR2kStruct(0);
    public IntegerR2kStruct spChangeVal = new IntegerR2kStruct(0);
    public IntegerR2kStruct spChangeMapVal = new IntegerR2kStruct(0);
    public IntegerR2kStruct spChangeMapSteps = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, type, "@persists"),
                new Index(0x03, colour, "@colour"),
                new Index(0x04, priority, "@priority"),
                new Index(0x05, restriction, "@restriction"),
                new Index(0x0B, aRate, "@a_rate"),
                new Index(0x0C, bRate, "@b_rate"),
                new Index(0x0D, cRate, "@c_rate"),
                new Index(0x0E, dRate, "@d_rate"),
                new Index(0x0F, eRate, "@e_rate"),
                new Index(0x15, holdTurn, "@hold_turn"),
                new Index(0x16, autoReleaseProb, "@auto_release_prob"),
                new Index(0x17, releaseByDamage, "@release_by_damage"),
                new Index(0x1E, affectType, "@affect_type_2k3"),
                new Index(0x1F, affectAtk, "@affect_atk"),
                new Index(0x20, affectDef, "@affect_def"),
                new Index(0x21, affectSpi, "@affect_spi"),
                new Index(0x22, affectAgi, "@affect_agi"),
                new Index(0x23, reduceHitRatio, "@reduce_hit_ratio"),
                new Index(0x24, avoidAttacks, "@avoid_attacks_2k3"),
                new Index(0x25, reflectMagic, "@reflect_magic_2k3"),
                new Index(0x26, cursed, "@cursed_2k3"),
                new Index(0x27, battlerAnimationId, "@battler_pose_2k3"),
                new Index(0x29, restrictSkill, "@restrict_skill"),
                new Index(0x2A, restrictSkillLevel, "@restrict_skill_level"),
                new Index(0x2B, restrictMagic, "@restrict_magic"),
                new Index(0x2C, restrictMagicLevel, "@restrict_magic_level"),
                new Index(0x2D, hpChangeType, "@hp_change_type"),
                new Index(0x2E, spChangeType, "@sp_change_type"),
                new Index(0x33, msgActor, "@msg_actor"),
                new Index(0x34, msgEnemy, "@msg_enemy"),
                new Index(0x35, msgAlready, "@msg_already"),
                new Index(0x36, msgAffected, "@msg_affected"),
                new Index(0x37, msgRecovery, "@msg_recovery"),

                new Index(0x3D, hpChangeMax, "@hp_change_max"),
                new Index(0x3E, hpChangeVal, "@hp_change_val"),
                new Index(0x3F, hpChangeMapVal, "@hp_change_map_val"),
                new Index(0x40, hpChangeMapSteps, "@hp_change_map_steps"),

                new Index(0x41, spChangeMax, "@sp_change_max"),
                new Index(0x42, spChangeVal, "@sp_change_val"),
                new Index(0x43, spChangeMapVal, "@sp_change_map_val"),
                new Index(0x44, spChangeMapSteps, "@sp_change_map_steps")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::State", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
