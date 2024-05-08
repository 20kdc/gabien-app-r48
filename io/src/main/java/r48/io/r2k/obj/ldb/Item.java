/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
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

/**
 * Created on 05/06/17.
 */
public class Item extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(0x01) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@description") @DM2LcfBinding(0x02) @DMCXObject
    public StringR2kStruct description;
    @DMFXOBinding("@type") @DM2LcfBinding(0x03) @DMCXInteger(0)
    public IntegerR2kStruct type;
    @DMFXOBinding("@price") @DM2LcfBinding(0x05) @DMCXInteger(0)
    public IntegerR2kStruct price;
    @DMFXOBinding("@use_count") @DM2LcfBinding(0x06) @DMCXInteger(1)
    public IntegerR2kStruct uses;
    @DMFXOBinding("@equipbuff_atk") @DM2LcfBinding(0x0B) @DMCXInteger(0)
    public IntegerR2kStruct atkPoints1;
    @DMFXOBinding("@equipbuff_def") @DM2LcfBinding(0x0C) @DMCXInteger(0)
    public IntegerR2kStruct defPoints1;
    @DMFXOBinding("@equipbuff_spi") @DM2LcfBinding(0x0D) @DMCXInteger(0)
    public IntegerR2kStruct spiPoints1;
    @DMFXOBinding("@equipbuff_agi") @DM2LcfBinding(0x0E) @DMCXInteger(0)
    public IntegerR2kStruct agiPoints1;
    @DMFXOBinding("@two_handed") @DM2LcfBinding(0x0F) @DMCXBoolean(false)
    public BooleanR2kStruct twoHanded;
    @DMFXOBinding("@use_sp_cost") @DM2LcfBinding(0x10) @DMCXInteger(0)
    public IntegerR2kStruct spCost;
    @DMFXOBinding("@hit_chance") @DM2LcfBinding(0x11) @DMCXInteger(90)
    public IntegerR2kStruct hit;
    @DMFXOBinding("@crit_chance") @DM2LcfBinding(0x12) @DMCXInteger(0)
    public IntegerR2kStruct crit;
    @DMFXOBinding("@animation") @DM2LcfBinding(0x14) @DMCXInteger(1)
    public IntegerR2kStruct animation;
    @DMFXOBinding("@attack_preemptive") @DM2LcfBinding(0x15) @DMCXBoolean(false)
    public BooleanR2kStruct preemptive;
    @DMFXOBinding("@dual_attack") @DM2LcfBinding(0x16) @DMCXBoolean(false)
    public BooleanR2kStruct dualAttack;
    @DMFXOBinding("@attack_all") @DM2LcfBinding(0x17) @DMCXBoolean(false)
    public BooleanR2kStruct attackAll;
    @DMFXOBinding("@ignore_evasion") @DM2LcfBinding(0x18) @DMCXBoolean(false)
    public BooleanR2kStruct ignoreEvade;
    @DMFXOBinding("@prevent_crit") @DM2LcfBinding(0x19) @DMCXBoolean(false)
    public BooleanR2kStruct preventCrit;
    @DMFXOBinding("@raise_evasion") @DM2LcfBinding(0x1A) @DMCXBoolean(false)
    public BooleanR2kStruct raiseEvasion;
    @DMFXOBinding("@half_sp_cost") @DM2LcfBinding(0x1B) @DMCXBoolean(false)
    public BooleanR2kStruct halfSpCost;
    @DMFXOBinding("@no_terrain_damage") @DM2LcfBinding(0x1C) @DMCXBoolean(false)
    public BooleanR2kStruct noTerrainDamage;
    @DMFXOBinding("@cursed_2k3") @DM2LcfBinding(0x1D) @DMCXBoolean(false)
    public BooleanR2kStruct cursed;
    @DMFXOBinding("@entire_party") @DM2LcfBinding(0x1F) @DMCXBoolean(false)
    public BooleanR2kStruct entireParty;
    @DMFXOBinding("@recover_hp_rate") @DM2LcfBinding(0x20) @DMCXInteger(0)
    public IntegerR2kStruct recoverHpRate;
    @DMFXOBinding("@recover_hp") @DM2LcfBinding(0x21) @DMCXInteger(0)
    public IntegerR2kStruct recoverHp;
    @DMFXOBinding("@recover_sp_rate") @DM2LcfBinding(0x22) @DMCXInteger(0)
    public IntegerR2kStruct recoverSpRate;
    @DMFXOBinding("@recover_sp") @DM2LcfBinding(0x23) @DMCXInteger(0)
    public IntegerR2kStruct recoverSp;
    @DMFXOBinding("@medicine_only_on_map") @DM2LcfBinding(0x25) @DMCXBoolean(false)
    public BooleanR2kStruct situationMap1;
    @DMFXOBinding("@dead_only") @DM2LcfBinding(0x26) @DMCXBoolean(false)
    public BooleanR2kStruct koOnly;
    @DMFXOBinding("@usebuff_maxhp") @DM2LcfBinding(0x29) @DMCXInteger(0)
    public IntegerR2kStruct maxHpPoints;
    @DMFXOBinding("@usebuff_maxsp") @DM2LcfBinding(0x2A) @DMCXInteger(0)
    public IntegerR2kStruct maxSpPoints;
    @DMFXOBinding("@usebuff_atk") @DM2LcfBinding(0x2B) @DMCXInteger(0)
    public IntegerR2kStruct atkPoints2;
    @DMFXOBinding("@usebuff_def") @DM2LcfBinding(0x2C) @DMCXInteger(0)
    public IntegerR2kStruct defPoints2;
    @DMFXOBinding("@usebuff_spi") @DM2LcfBinding(0x2D) @DMCXInteger(0)
    public IntegerR2kStruct spiPoints2;
    @DMFXOBinding("@usebuff_agi") @DM2LcfBinding(0x2E) @DMCXInteger(0)
    public IntegerR2kStruct agiPoints2;
    @DMFXOBinding("@skill_use_item_msg_2k3") @DM2LcfBinding(0x33) @DMCXInteger(0)
    public IntegerR2kStruct useMessage;
    @DMFXOBinding("@skill_id") @DM2LcfBinding(0x35) @DMCXInteger(1)
    public IntegerR2kStruct skillId;
    @DMFXOBinding("@switch_id") @DM2LcfBinding(0x37) @DMCXInteger(1)
    public IntegerR2kStruct switchId;
    @DMFXOBinding("@switch_usable_on_map") @DM2LcfBinding(0x39) @DMCXBoolean(true)
    public BooleanR2kStruct situationMap2;
    @DMFXOBinding("@switch_usable_in_battle") @DM2LcfBinding(0x3A) @DMCXBoolean(false)
    public BooleanR2kStruct situationBattle;
    @DMOptional @DMFXOBinding("@easyrpg_using_message") @DM2LcfBinding(0xC9) @DMCXObject
    public StringR2kStruct easyrpgUsingMessage;

    // And now the arrays start
    @DMFXOBinding("@actor_set") @DM2LcfSizeBinding(0x3D) @DM2LcfBinding(0x3E)
    public DM2ArraySet<BooleanR2kStruct> aEfx;

    @DMFXOBinding("@state_set") @DM2LcfSizeBinding(0x3F) @DM2LcfBinding(0x40)
    public DM2ArraySet<BooleanR2kStruct> sEfx;

    @DMFXOBinding("@attr_set") @DM2LcfSizeBinding(0x41) @DM2LcfBinding(0x42)
    public DM2ArraySet<BooleanR2kStruct> atEfx;

    // --


    @DMFXOBinding("@state_chance") @DM2LcfBinding(0x43) @DMCXInteger(0)
    public IntegerR2kStruct stateChance;
    @DMFXOBinding("@state_effect") @DM2LcfBinding(0x44) @DMCXBoolean(false)
    public BooleanR2kStruct stateEffect;

    @DMFXOBinding("@weapon_anim_def_2k3") @DM2LcfBinding(0x45) @DMCXInteger(1)
    public IntegerR2kStruct weaponAnimation;
    @DMFXOBinding("@weapon_anim_data_2k3") @DM2LcfBinding(0x46) @DMCXSupplier(ItemAnimation.class)
    public DM2SparseArrayH<ItemAnimation> weaponAnimationData;

    @DMFXOBinding("@use_skill_2k3") @DM2LcfBinding(0x47) @DMCXBoolean(false)
    public BooleanR2kStruct useSkill;

    @DMFXOBinding("@ranged_return") @DM2LcfBinding(0x4B) @DMCXInteger(0)
    public IntegerR2kStruct rangedPath;
    @DMFXOBinding("@ranged_target") @DM2LcfBinding(0x4C) @DMCXInteger(0)
    public IntegerR2kStruct itemTarget;

    // --

    @DMFXOBinding("@class_set_2k3") @DM2LcfSizeBinding(0x48) @DM2LcfBinding(0x49)
    public DM2ArraySet<BooleanR2kStruct> cEfx;

    public Item(DMContext ctx) {
        super(ctx, "RPG::Item");
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
        return new DM2ArraySet<BooleanR2kStruct>(context) {
            @Override
            public BooleanR2kStruct newValue() {
                return new BooleanR2kStruct(context, b);
            }
        };
    }
}
