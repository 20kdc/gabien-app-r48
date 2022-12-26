/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.data.DM2Optional;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.ShortR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * Created on 05/06/17.
 */
public class Actor extends ActorClassBase {
    @DM2FXOBinding("@title") @DM2LcfBinding(2) @DM2LcfObject
    public StringR2kStruct title;
    @DM2FXOBinding("@character_name") @DM2LcfBinding(3) @DM2LcfObject
    public StringR2kStruct charName;
    @DM2FXOBinding("@character_index") @DM2LcfBinding(4) @DM2LcfInteger(0)
    public IntegerR2kStruct charIdx;
    @DM2FXOBinding("@character_blend_mode") @DM2LcfBinding(5) @DM2LcfBoolean(false)
    public BooleanR2kStruct transparent;
    @DM2FXOBinding("@init_level") @DM2LcfBinding(7) @DM2LcfInteger(1)
    public IntegerR2kStruct initLevel;
    // Marked as "50|99" in liblcf docs - version differences.
    // OptionalR2kStruct can at least translate this into something usable.
    @DM2Optional @DM2FXOBinding("@final_level") @DM2LcfBinding(8) @DM2LcfInteger(99)
    public IntegerR2kStruct finalLevel;
    @DM2FXOBinding("@can_crit") @DM2LcfBinding(9) @DM2LcfBoolean(true)
    public BooleanR2kStruct canCrit;
    @DM2FXOBinding("@crit_percent") @DM2LcfBinding(10) @DM2LcfInteger(30)
    public IntegerR2kStruct critPercent;
    @DM2FXOBinding("@face_name") @DM2LcfBinding(15) @DM2LcfObject
    public StringR2kStruct faceName;
    @DM2FXOBinding("@face_index") @DM2LcfBinding(16) @DM2LcfInteger(0)
    public IntegerR2kStruct faceIdx;
    @DM2FXOBinding("@equipment") @DM2LcfBinding(51)
    public DM2Array<ShortR2kStruct> equipment;
    @DM2FXOBinding("@no_weapon_attack_anim") @DM2LcfBinding(40) @DM2LcfInteger(1)
    public IntegerR2kStruct noWeaponAttackAnim;
    @DM2FXOBinding("@class_2k3") @DM2LcfBinding(57) @DM2LcfInteger(0)
    public IntegerR2kStruct aClass;
    @DM2FXOBinding("@battle_posx_2k3") @DM2LcfBinding(59) @DM2LcfInteger(220)
    public IntegerR2kStruct batPosX;
    @DM2FXOBinding("@battle_posy_2k3") @DM2LcfBinding(60) @DM2LcfInteger(120)
    public IntegerR2kStruct batPosY;
    @DM2FXOBinding("@editor_use_skillspanel_name") @DM2LcfBinding(66) @DM2LcfBoolean(false)
    public BooleanR2kStruct canRename;
    @DM2FXOBinding("@battle_skillspanel_name") @DM2LcfBinding(67) @DM2LcfObject
    public StringR2kStruct renameResult;

    public Actor() {
        super("RPG::Actor", 1);
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@equipment"))
            return equipment = new DM2Array<ShortR2kStruct>() {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(0);
                }
            };
        return super.dm2AddIVar(sym);
    }
}
