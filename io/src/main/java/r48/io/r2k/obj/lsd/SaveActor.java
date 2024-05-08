/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.*;
import r48.io.r2k.dm2chk.*;

/**
 * They're pieces on the board that are waiting to be moved...
 */
public class SaveActor extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(0x01) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@title") @DM2LcfBinding(0x02) @DMCXObject
    public StringR2kStruct title;
    @DMFXOBinding("@character_name") @DM2LcfBinding(0x0B) @DMCXObject
    public StringR2kStruct charName;
    @DMFXOBinding("@character_index") @DM2LcfBinding(0x0C) @DMCXInteger(0)
    public IntegerR2kStruct charIdx;
    @DMFXOBinding("@character_flags") @DM2LcfBinding(0x0D) @DMCXInteger(0)
    public IntegerR2kStruct charFlags;
    @DMFXOBinding("@face_name") @DM2LcfBinding(0x15) @DMCXObject
    public StringR2kStruct faceName;
    @DMFXOBinding("@face_index") @DM2LcfBinding(0x16) @DMCXInteger(0)
    public IntegerR2kStruct faceIdx;
    @DMFXOBinding("@level") @DM2LcfBinding(0x1F) @DMCXInteger(-1)
    public IntegerR2kStruct level;
    @DMFXOBinding("@exp") @DM2LcfBinding(0x20) @DMCXInteger(-1)
    public IntegerR2kStruct exp;
    @DMFXOBinding("@hp_mod") @DM2LcfBinding(0x21) @DMCXInteger(-1)
    public IntegerR2kStruct hpMod;
    @DMFXOBinding("@sp_mod") @DM2LcfBinding(0x22) @DMCXInteger(-1)
    public IntegerR2kStruct spMod;
    @DMFXOBinding("@atk_mod") @DM2LcfBinding(0x29) @DMCXInteger(0)
    public IntegerR2kStruct attackMod;
    @DMFXOBinding("@def_mod") @DM2LcfBinding(0x2A) @DMCXInteger(0)
    public IntegerR2kStruct defenseMod;
    @DMFXOBinding("@spi_mod") @DM2LcfBinding(0x2B) @DMCXInteger(0)
    public IntegerR2kStruct spiritMod;
    @DMFXOBinding("@agi_mod") @DM2LcfBinding(0x2C) @DMCXInteger(0)
    public IntegerR2kStruct agilityMod;

    @DMFXOBinding("@skills") @DM2LcfSizeBinding(0x33) @DM2LcfBinding(0x34)
    public DM2Array<ShortR2kStruct> skills;

    @DMFXOBinding("@equipment") @DM2LcfBinding(0x3D)
    public DM2Array<ShortR2kStruct> equipment;

    @DMFXOBinding("@current_hp") @DM2LcfBinding(0x47) @DMCXInteger(-1)
    public IntegerR2kStruct currentHp;
    @DMFXOBinding("@current_sp") @DM2LcfBinding(0x48) @DMCXInteger(-1)
    public IntegerR2kStruct currentSp;

    @DMFXOBinding("@battle_commands_2k3") @DM2LcfBinding(0x50)
    public DM2Array<Int32R2kStruct> battleCommands;

    @DMFXOBinding("@states") @DM2LcfSizeBinding(0x51) @DM2LcfBinding(0x52)
    public DM2Array<ShortR2kStruct> states;

    @DMFXOBinding("@changed_battle_commands_2k3") @DM2LcfBinding(0x53) @DMCXBoolean(false)
    public BooleanR2kStruct changedBattleCommands;
    @DMFXOBinding("@class_id_2k3") @DM2LcfBinding(0x5A) @DMCXInteger(-1)
    public IntegerR2kStruct classId;
    @DMFXOBinding("@row") @DM2LcfBinding(0x5B) @DMCXInteger(0)
    public IntegerR2kStruct row;
    @DMFXOBinding("@two_weapon") @DM2LcfBinding(0x5C) @DMCXBoolean(false)
    public BooleanR2kStruct twoWeapon;
    @DMFXOBinding("@lock_equipment") @DM2LcfBinding(0x5D) @DMCXBoolean(false)
    public BooleanR2kStruct lockEquipment;
    @DMFXOBinding("@auto_battle") @DM2LcfBinding(0x5E)
    public BooleanR2kStruct autoBattle;
    @DMFXOBinding("@super_guard") @DM2LcfBinding(0x5F) @DMCXBoolean(false)
    public BooleanR2kStruct superGuard;
    @DMFXOBinding("@battler_animation_2k3") @DM2LcfBinding(0x60) @DMCXInteger(0)
    public IntegerR2kStruct battlerAnimation;

    public SaveActor(DMContext ctx) {
        super(ctx, "RPG::SaveActor");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@skills"))
            return skills = new DM2Array<ShortR2kStruct>(context, 0, true, true) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(context, 0);
                }
            };
        if (sym.equals("@equipment"))
            return equipment = new DM2Array<ShortR2kStruct>(context, 0, true, true, 5) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(context, 0);
                }
            };
        if (sym.equals("@battle_commands_2k3"))
            return battleCommands = new DM2Array<Int32R2kStruct>(context, 0, true, false, 7) {
                @Override
                public Int32R2kStruct newValue() {
                    return new Int32R2kStruct(context, -1);
                }
            };
        if (sym.equals("@states"))
            return states = new DM2Array<ShortR2kStruct>(context, 0, true, true) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(context, 0);
                }
            };
        return super.dm2AddIVar(sym);
    }
}
