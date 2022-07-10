/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.data.DM2Optional;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * Created on 05/06/17.
 */
public class Item extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(0x01) @DM2LcfObject
    public StringR2kStruct name;
    @DM2FXOBinding("@description") @DM2LcfBinding(0x02) @DM2LcfObject
    public StringR2kStruct description;
    @DM2FXOBinding("@type") @DM2LcfBinding(0x03) @DM2LcfInteger(0)
    public IntegerR2kStruct type;
    @DM2FXOBinding("@price") @DM2LcfBinding(0x05) @DM2LcfInteger(0)
    public IntegerR2kStruct price;
    @DM2FXOBinding("@use_count") @DM2LcfBinding(0x06) @DM2LcfInteger(1)
    public IntegerR2kStruct uses;
    @DM2FXOBinding("@equipbuff_atk") @DM2LcfBinding(0x0B) @DM2LcfInteger(0)
    public IntegerR2kStruct atkPoints1;
    @DM2FXOBinding("@equipbuff_def") @DM2LcfBinding(0x0C) @DM2LcfInteger(0)
    public IntegerR2kStruct defPoints1;
    @DM2FXOBinding("@equipbuff_spi") @DM2LcfBinding(0x0D) @DM2LcfInteger(0)
    public IntegerR2kStruct spiPoints1;
    @DM2FXOBinding("@equipbuff_agi") @DM2LcfBinding(0x0E) @DM2LcfInteger(0)
    public IntegerR2kStruct agiPoints1;
    @DM2FXOBinding("@two_handed") @DM2LcfBinding(0x0F) @DM2LcfBoolean(false)
    public BooleanR2kStruct twoHanded;
    @DM2FXOBinding("@use_sp_cost") @DM2LcfBinding(0x10) @DM2LcfInteger(0)
    public IntegerR2kStruct spCost;
    @DM2FXOBinding("@hit_chance") @DM2LcfBinding(0x11) @DM2LcfInteger(90)
    public IntegerR2kStruct hit;
    @DM2FXOBinding("@crit_chance") @DM2LcfBinding(0x12) @DM2LcfInteger(0)
    public IntegerR2kStruct crit;
    @DM2FXOBinding("@animation") @DM2LcfBinding(0x14) @DM2LcfInteger(1)
    public IntegerR2kStruct animation;
    @DM2FXOBinding("@attack_preemptive") @DM2LcfBinding(0x15) @DM2LcfBoolean(false)
    public BooleanR2kStruct preemptive;
    @DM2FXOBinding("@dual_attack") @DM2LcfBinding(0x16) @DM2LcfBoolean(false)
    public BooleanR2kStruct dualAttack;
    @DM2FXOBinding("@attack_all") @DM2LcfBinding(0x17) @DM2LcfBoolean(false)
    public BooleanR2kStruct attackAll;
    @DM2FXOBinding("@ignore_evasion") @DM2LcfBinding(0x18) @DM2LcfBoolean(false)
    public BooleanR2kStruct ignoreEvade;
    @DM2FXOBinding("@prevent_crit") @DM2LcfBinding(0x19) @DM2LcfBoolean(false)
    public BooleanR2kStruct preventCrit;
    @DM2FXOBinding("@raise_evasion") @DM2LcfBinding(0x1A) @DM2LcfBoolean(false)
    public BooleanR2kStruct raiseEvasion;
    @DM2FXOBinding("@half_sp_cost") @DM2LcfBinding(0x1B) @DM2LcfBoolean(false)
    public BooleanR2kStruct halfSpCost;
    @DM2FXOBinding("@no_terrain_damage") @DM2LcfBinding(0x1C) @DM2LcfBoolean(false)
    public BooleanR2kStruct noTerrainDamage;
    @DM2FXOBinding("@cursed_2k3") @DM2LcfBinding(0x1D) @DM2LcfBoolean(false)
    public BooleanR2kStruct cursed;
    @DM2FXOBinding("@entire_party") @DM2LcfBinding(0x1F) @DM2LcfBoolean(false)
    public BooleanR2kStruct entireParty;
    @DM2FXOBinding("@recover_hp_rate") @DM2LcfBinding(0x20) @DM2LcfInteger(0)
    public IntegerR2kStruct recoverHpRate;
    @DM2FXOBinding("@recover_hp") @DM2LcfBinding(0x21) @DM2LcfInteger(0)
    public IntegerR2kStruct recoverHp;
    @DM2FXOBinding("@recover_sp_rate") @DM2LcfBinding(0x22) @DM2LcfInteger(0)
    public IntegerR2kStruct recoverSpRate;
    @DM2FXOBinding("@recover_sp") @DM2LcfBinding(0x23) @DM2LcfInteger(0)
    public IntegerR2kStruct recoverSp;
    @DM2FXOBinding("@medicine_only_on_map") @DM2LcfBinding(0x25) @DM2LcfBoolean(false)
    public BooleanR2kStruct situationMap1;
    @DM2FXOBinding("@dead_only") @DM2LcfBinding(0x26) @DM2LcfBoolean(false)
    public BooleanR2kStruct koOnly;
    @DM2FXOBinding("@usebuff_maxhp") @DM2LcfBinding(0x29) @DM2LcfInteger(0)
    public IntegerR2kStruct maxHpPoints;
    @DM2FXOBinding("@usebuff_maxsp") @DM2LcfBinding(0x2A) @DM2LcfInteger(0)
    public IntegerR2kStruct maxSpPoints;
    @DM2FXOBinding("@usebuff_atk") @DM2LcfBinding(0x2B) @DM2LcfInteger(0)
    public IntegerR2kStruct atkPoints2;
    @DM2FXOBinding("@usebuff_def") @DM2LcfBinding(0x2C) @DM2LcfInteger(0)
    public IntegerR2kStruct defPoints2;
    @DM2FXOBinding("@usebuff_spi") @DM2LcfBinding(0x2D) @DM2LcfInteger(0)
    public IntegerR2kStruct spiPoints2;
    @DM2FXOBinding("@usebuff_agi") @DM2LcfBinding(0x2E) @DM2LcfInteger(0)
    public IntegerR2kStruct agiPoints2;
    @DM2FXOBinding("@skill_use_item_msg_2k3") @DM2LcfBinding(0x33) @DM2LcfInteger(0)
    public IntegerR2kStruct useMessage;
    @DM2FXOBinding("@skill_id") @DM2LcfBinding(0x35) @DM2LcfInteger(1)
    public IntegerR2kStruct skillId;
    @DM2FXOBinding("@switch_id") @DM2LcfBinding(0x37) @DM2LcfInteger(1)
    public IntegerR2kStruct switchId;
    @DM2FXOBinding("@switch_usable_on_map") @DM2LcfBinding(0x39) @DM2LcfBoolean(true)
    public BooleanR2kStruct situationMap2;
    @DM2FXOBinding("@switch_usable_in_battle") @DM2LcfBinding(0x3A) @DM2LcfBoolean(false)
    public BooleanR2kStruct situationBattle;
    @DM2Optional @DM2FXOBinding("@easyrpg_using_message") @DM2LcfBinding(0xC9) @DM2LcfObject
    public StringR2kStruct easyrpgUsingMessage;

    // And now the arrays start
    @DM2FXOBinding("@actor_set") @DM2LcfSizeBinding(0x3D) @DM2LcfBinding(0x3E)
    public DM2ArraySet<BooleanR2kStruct> aEfx;

    @DM2FXOBinding("@state_set") @DM2LcfSizeBinding(0x3F) @DM2LcfBinding(0x40)
    public DM2ArraySet<BooleanR2kStruct> sEfx;

    @DM2FXOBinding("@attr_set") @DM2LcfSizeBinding(0x41) @DM2LcfBinding(0x42)
    public DM2ArraySet<BooleanR2kStruct> atEfx;

    // --


    @DM2FXOBinding("@state_chance") @DM2LcfBinding(0x43) @DM2LcfInteger(0)
    public IntegerR2kStruct stateChance;
    @DM2FXOBinding("@state_effect") @DM2LcfBinding(0x44) @DM2LcfBoolean(false)
    public BooleanR2kStruct stateEffect;

    @DM2FXOBinding("@weapon_anim_def_2k3") @DM2LcfBinding(0x45) @DM2LcfInteger(1)
    public IntegerR2kStruct weaponAnimation;
    @DM2FXOBinding("@weapon_anim_data_2k3") @DM2LcfBinding(0x46) @DM2LcfSparseArray(ItemAnimation.class)
    public DM2SparseArrayH<ItemAnimation> weaponAnimationData;

    @DM2FXOBinding("@use_skill_2k3") @DM2LcfBinding(0x47) @DM2LcfBoolean(false)
    public BooleanR2kStruct useSkill;

    @DM2FXOBinding("@ranged_return") @DM2LcfBinding(0x4B) @DM2LcfInteger(0)
    public IntegerR2kStruct rangedPath;
    @DM2FXOBinding("@ranged_target") @DM2LcfBinding(0x4C) @DM2LcfInteger(0)
    public IntegerR2kStruct itemTarget;

    // --

    @DM2FXOBinding("@class_set_2k3") @DM2LcfSizeBinding(0x48) @DM2LcfBinding(0x49)
    public DM2ArraySet<BooleanR2kStruct> cEfx;

    public Item() {
        super("RPG::Item");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@actor_set"))
            return aEfx = newFlagSet(false);
        if (sym.equals("@state_set"))
            return sEfx = newFlagSet(false);
        if (sym.equals("@attr_set"))
            return atEfx = newFlagSet(true);
        if (sym.equals("@class_set_2k3"))
            return cEfx = newFlagSet(true);
        return super.dm2AddIVar(sym);
    }

    private DM2ArraySet<BooleanR2kStruct> newFlagSet(final boolean b) {
        return new DM2ArraySet<BooleanR2kStruct>() {
            @Override
            public BooleanR2kStruct newValue() {
                return new BooleanR2kStruct(b);
            }
        };
    }
}
