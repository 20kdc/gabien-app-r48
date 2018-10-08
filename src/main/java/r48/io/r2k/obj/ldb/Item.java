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

/**
 * Created on 05/06/17.
 */
public class Item extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct description = new StringR2kStruct();
    public IntegerR2kStruct type = new IntegerR2kStruct(0);
    public IntegerR2kStruct price = new IntegerR2kStruct(0);
    public IntegerR2kStruct uses = new IntegerR2kStruct(1);
    public IntegerR2kStruct atkPoints1 = new IntegerR2kStruct(0);
    public IntegerR2kStruct defPoints1 = new IntegerR2kStruct(0);
    public IntegerR2kStruct spiPoints1 = new IntegerR2kStruct(0);
    public IntegerR2kStruct agiPoints1 = new IntegerR2kStruct(0);
    public BooleanR2kStruct twoHanded = new BooleanR2kStruct(false);
    public IntegerR2kStruct spCost = new IntegerR2kStruct(0);
    public IntegerR2kStruct hit = new IntegerR2kStruct(90);
    public IntegerR2kStruct crit = new IntegerR2kStruct(0);
    public IntegerR2kStruct animation = new IntegerR2kStruct(1);
    public BooleanR2kStruct preemptive = new BooleanR2kStruct(false);
    public BooleanR2kStruct dualAttack = new BooleanR2kStruct(false);
    public BooleanR2kStruct attackAll = new BooleanR2kStruct(false);
    public BooleanR2kStruct ignoreEvade = new BooleanR2kStruct(false);
    public BooleanR2kStruct preventCrit = new BooleanR2kStruct(false);
    public BooleanR2kStruct raiseEvasion = new BooleanR2kStruct(false);
    public BooleanR2kStruct halfSpCost = new BooleanR2kStruct(false);
    public BooleanR2kStruct noTerrainDamage = new BooleanR2kStruct(false);
    public BooleanR2kStruct cursed = new BooleanR2kStruct(false);
    public BooleanR2kStruct entireParty = new BooleanR2kStruct(false);
    public IntegerR2kStruct recoverHpRate = new IntegerR2kStruct(0);
    public IntegerR2kStruct recoverHp = new IntegerR2kStruct(0);
    public IntegerR2kStruct recoverSpRate = new IntegerR2kStruct(0);
    public IntegerR2kStruct recoverSp = new IntegerR2kStruct(0);
    public BooleanR2kStruct situationMap1 = new BooleanR2kStruct(false);
    public BooleanR2kStruct koOnly = new BooleanR2kStruct(false);
    public IntegerR2kStruct maxHpPoints = new IntegerR2kStruct(0);
    public IntegerR2kStruct maxSpPoints = new IntegerR2kStruct(0);
    public IntegerR2kStruct atkPoints2 = new IntegerR2kStruct(0);
    public IntegerR2kStruct defPoints2 = new IntegerR2kStruct(0);
    public IntegerR2kStruct spiPoints2 = new IntegerR2kStruct(0);
    public IntegerR2kStruct agiPoints2 = new IntegerR2kStruct(0);
    public IntegerR2kStruct useMessage = new IntegerR2kStruct(0);
    public IntegerR2kStruct skillId = new IntegerR2kStruct(1);
    public IntegerR2kStruct switchId = new IntegerR2kStruct(1);
    public BooleanR2kStruct situationMap2 = new BooleanR2kStruct(false);
    public BooleanR2kStruct situationBattle = new BooleanR2kStruct(false);

    // And now the arrays start

    public ArraySizeR2kInterpretable<BooleanR2kStruct> basAE = new ArraySizeR2kInterpretable<BooleanR2kStruct>();
    public ArraySetR2kStruct<BooleanR2kStruct> aEfx = new ArraySetR2kStruct<BooleanR2kStruct>(basAE, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(false);
        }
    }, true);

    public ArraySizeR2kInterpretable<BooleanR2kStruct> basSE = new ArraySizeR2kInterpretable<BooleanR2kStruct>();
    public ArraySetR2kStruct<BooleanR2kStruct> sEfx = new ArraySetR2kStruct<BooleanR2kStruct>(basSE, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(false);
        }
    }, true);

    public ArraySizeR2kInterpretable<BooleanR2kStruct> basATE = new ArraySizeR2kInterpretable<BooleanR2kStruct>();
    public ArraySetR2kStruct<BooleanR2kStruct> atEfx = new ArraySetR2kStruct<BooleanR2kStruct>(basATE, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(true);
        }
    }, true);

    // --

    public IntegerR2kStruct stateChance = new IntegerR2kStruct(0);
    public BooleanR2kStruct stateEffect = new BooleanR2kStruct(false);

    public IntegerR2kStruct weaponAnimation = new IntegerR2kStruct(1);
    public SparseArrayHR2kStruct<ItemAnimation> weaponAnimationData = new SparseArrayHR2kStruct<ItemAnimation>(new ISupplier<ItemAnimation>() {
        @Override
        public ItemAnimation get() {
            return new ItemAnimation();
        }
    });

    public BooleanR2kStruct useSkill = new BooleanR2kStruct(false);

    public IntegerR2kStruct rangedPath = new IntegerR2kStruct(0);
    public IntegerR2kStruct itemTarget = new IntegerR2kStruct(0);

    // --

    public ArraySizeR2kInterpretable<BooleanR2kStruct> basCE = new ArraySizeR2kInterpretable<BooleanR2kStruct>();
    public ArraySetR2kStruct<BooleanR2kStruct> cEfx = new ArraySetR2kStruct<BooleanR2kStruct>(basCE, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(true);
        }
    }, true);


    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, description, "@description"),
                new Index(0x03, type, "@type"),
                new Index(0x05, price, "@price"),
                new Index(0x06, uses, "@use_count"),
                new Index(0x0B, atkPoints1, "@equipbuff_atk"),
                new Index(0x0C, defPoints1, "@equipbuff_def"),
                new Index(0x0D, spiPoints1, "@equipbuff_spi"),
                new Index(0x0E, agiPoints1, "@equipbuff_agi"),
                new Index(0x0F, twoHanded, "@two_handed"),
                new Index(0x10, spCost, "@use_sp_cost"),
                new Index(0x11, hit, "@hit_chance"),
                new Index(0x12, crit, "@crit_chance"),
                new Index(0x14, animation, "@animation"),
                new Index(0x15, preemptive, "@attack_preemptive"),
                new Index(0x16, dualAttack, "@dual_attack"),
                new Index(0x17, attackAll, "@attack_all"),
                new Index(0x18, ignoreEvade, "@ignore_evasion"),
                new Index(0x19, preventCrit, "@prevent_crit"),
                new Index(0x1A, raiseEvasion, "@raise_evasion"),
                new Index(0x1B, halfSpCost, "@half_sp_cost"),
                new Index(0x1C, noTerrainDamage, "@no_terrain_damage"),
                new Index(0x1D, cursed, "@cursed_2k3"),
                new Index(0x1F, entireParty, "@entire_party"),
                new Index(0x20, recoverHpRate, "@recover_hp_rate"),
                new Index(0x21, recoverHp, "@recover_hp"),
                new Index(0x22, recoverSpRate, "@recover_sp_rate"),
                new Index(0x23, recoverSp, "@recover_sp"),
                new Index(0x25, situationMap1, "@medicine_only_on_map"),
                new Index(0x26, koOnly, "@dead_only"),
                new Index(0x29, maxHpPoints, "@usebuff_maxhp"),
                new Index(0x2A, maxSpPoints, "@usebuff_maxsp"),
                new Index(0x2B, atkPoints2, "@usebuff_atk"),
                new Index(0x2C, defPoints2, "@usebuff_def"),
                new Index(0x2D, spiPoints2, "@usebuff_spi"),
                new Index(0x2E, agiPoints2, "@usebuff_agi"),
                new Index(0x33, useMessage, "@skill_use_item_msg_2k3"),
                new Index(0x35, skillId, "@skill_id"),
                new Index(0x37, switchId, "@switch_id"),
                new Index(0x39, situationMap2, "@switch_usable_on_map"),
                new Index(0x3A, situationBattle, "@switch_usable_in_battle"),

                new Index(0x3D, basAE),
                new Index(0x3E, aEfx, "@actor_set"),

                new Index(0x3F, basSE),
                new Index(0x40, sEfx, "@state_set"),

                new Index(0x41, basATE),
                new Index(0x42, atEfx, "@attr_set"),

                new Index(0x43, stateChance, "@state_chance"),
                new Index(0x44, stateEffect, "@state_effect"),

                new Index(0x45, weaponAnimation, "@weapon_anim_def_2k3"),
                new Index(0x46, weaponAnimationData, "@weapon_anim_data_2k3"),

                new Index(0x47, useSkill, "@use_skill_2k3"),

                new Index(0x48, basCE),
                new Index(0x49, cEfx, "@class_set_2k3"),

                new Index(0x4B, rangedPath, "@ranged_return"),
                new Index(0x4C, itemTarget, "@ranged_target"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Item", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
