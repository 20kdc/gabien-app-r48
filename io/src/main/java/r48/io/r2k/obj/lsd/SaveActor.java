/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.*;
import r48.io.r2k.dm2chk.*;

/**
 * They're pieces on the board that are waiting to be moved...
 */
public class SaveActor extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(0x01) @DM2LcfObject
    public StringR2kStruct name;
    @DM2FXOBinding("@title") @DM2LcfBinding(0x02) @DM2LcfObject
    public StringR2kStruct title;
    @DM2FXOBinding("@character_name") @DM2LcfBinding(0x0B) @DM2LcfObject
    public StringR2kStruct charName;
    @DM2FXOBinding("@character_index") @DM2LcfBinding(0x0C) @DM2LcfInteger(0)
    public IntegerR2kStruct charIdx;
    @DM2FXOBinding("@character_flags") @DM2LcfBinding(0x0D) @DM2LcfInteger(0)
    public IntegerR2kStruct charFlags;
    @DM2FXOBinding("@face_name") @DM2LcfBinding(0x15) @DM2LcfObject
    public StringR2kStruct faceName;
    @DM2FXOBinding("@face_index") @DM2LcfBinding(0x16) @DM2LcfInteger(0)
    public IntegerR2kStruct faceIdx;
    @DM2FXOBinding("@level") @DM2LcfBinding(0x1F) @DM2LcfInteger(-1)
    public IntegerR2kStruct level;
    @DM2FXOBinding("@exp") @DM2LcfBinding(0x20) @DM2LcfInteger(-1)
    public IntegerR2kStruct exp;
    @DM2FXOBinding("@hp_mod") @DM2LcfBinding(0x21) @DM2LcfInteger(-1)
    public IntegerR2kStruct hpMod;
    @DM2FXOBinding("@sp_mod") @DM2LcfBinding(0x22) @DM2LcfInteger(-1)
    public IntegerR2kStruct spMod;
    @DM2FXOBinding("@atk_mod") @DM2LcfBinding(0x29) @DM2LcfInteger(0)
    public IntegerR2kStruct attackMod;
    @DM2FXOBinding("@def_mod") @DM2LcfBinding(0x2A) @DM2LcfInteger(0)
    public IntegerR2kStruct defenseMod;
    @DM2FXOBinding("@spi_mod") @DM2LcfBinding(0x2B) @DM2LcfInteger(0)
    public IntegerR2kStruct spiritMod;
    @DM2FXOBinding("@agi_mod") @DM2LcfBinding(0x2C) @DM2LcfInteger(0)
    public IntegerR2kStruct agilityMod;

    @DM2FXOBinding("@skills") @DM2LcfSizeBinding(0x33) @DM2LcfBinding(0x34)
    public DM2Array<ShortR2kStruct> skills;

    @DM2FXOBinding("@equipment") @DM2LcfBinding(0x3D)
    public DM2Array<ShortR2kStruct> equipment;

    @DM2FXOBinding("@current_hp") @DM2LcfBinding(0x47) @DM2LcfInteger(-1)
    public IntegerR2kStruct currentHp;
    @DM2FXOBinding("@current_sp") @DM2LcfBinding(0x48) @DM2LcfInteger(-1)
    public IntegerR2kStruct currentSp;

    @DM2FXOBinding("@battle_commands_2k3") @DM2LcfBinding(0x50)
    public DM2Array<Int32R2kStruct> battleCommands;

    @DM2FXOBinding("@states") @DM2LcfSizeBinding(0x51) @DM2LcfBinding(0x52)
    public DM2Array<ShortR2kStruct> states;

    @DM2FXOBinding("@changed_battle_commands_2k3") @DM2LcfBinding(0x53) @DM2LcfBoolean(false)
    public BooleanR2kStruct changedBattleCommands;
    @DM2FXOBinding("@class_id_2k3") @DM2LcfBinding(0x5A) @DM2LcfInteger(-1)
    public IntegerR2kStruct classId;
    @DM2FXOBinding("@row") @DM2LcfBinding(0x5B) @DM2LcfInteger(0)
    public IntegerR2kStruct row;
    @DM2FXOBinding("@two_weapon") @DM2LcfBinding(0x5C) @DM2LcfBoolean(false)
    public BooleanR2kStruct twoWeapon;
    @DM2FXOBinding("@lock_equipment") @DM2LcfBinding(0x5D) @DM2LcfBoolean(false)
    public BooleanR2kStruct lockEquipment;
    @DM2FXOBinding("@auto_battle") @DM2LcfBinding(0x5E)
    public BooleanR2kStruct autoBattle;
    @DM2FXOBinding("@super_guard") @DM2LcfBinding(0x5F) @DM2LcfBoolean(false)
    public BooleanR2kStruct superGuard;
    @DM2FXOBinding("@battler_animation_2k3") @DM2LcfBinding(0x60) @DM2LcfInteger(0)
    public IntegerR2kStruct battlerAnimation;

    public SaveActor() {
        super("RPG::SaveActor");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@skills"))
            return skills = new DM2Array<ShortR2kStruct>(0, true, true) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(0);
                }
            };
        if (sym.equals("@equipment"))
            return equipment = new DM2Array<ShortR2kStruct>(0, true, true, 5) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(0);
                }
            };
        if (sym.equals("@battle_commands_2k3"))
            return battleCommands = new DM2Array<Int32R2kStruct>(0, true, false, 7) {
                @Override
                public Int32R2kStruct newValue() {
                    return new Int32R2kStruct(-1);
                }
            };
        if (sym.equals("@states"))
            return states = new DM2Array<ShortR2kStruct>(0, true, true) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(0);
                }
            };
        return super.dm2AddIVar(sym);
    }
}
