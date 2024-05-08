/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.ShortR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * Created on 05/06/17.
 */
public class Actor extends ActorClassBase {
    @DMFXOBinding("@title") @DM2LcfBinding(2) @DMCXObject
    public StringR2kStruct title;
    @DMFXOBinding("@character_name") @DM2LcfBinding(3) @DMCXObject
    public StringR2kStruct charName;
    @DMFXOBinding("@character_index") @DM2LcfBinding(4) @DMCXInteger(0)
    public IntegerR2kStruct charIdx;
    @DMFXOBinding("@character_blend_mode") @DM2LcfBinding(5) @DMCXBoolean(false)
    public BooleanR2kStruct transparent;
    @DMFXOBinding("@init_level") @DM2LcfBinding(7) @DMCXInteger(1)
    public IntegerR2kStruct initLevel;
    // Marked as "50|99" in liblcf docs - version differences.
    // OptionalR2kStruct can at least translate this into something usable.
    @DMOptional @DMFXOBinding("@final_level") @DM2LcfBinding(8) @DMCXInteger(99)
    public IntegerR2kStruct finalLevel;
    @DMFXOBinding("@can_crit") @DM2LcfBinding(9) @DMCXBoolean(true)
    public BooleanR2kStruct canCrit;
    @DMFXOBinding("@crit_percent") @DM2LcfBinding(10) @DMCXInteger(30)
    public IntegerR2kStruct critPercent;
    @DMFXOBinding("@face_name") @DM2LcfBinding(15) @DMCXObject
    public StringR2kStruct faceName;
    @DMFXOBinding("@face_index") @DM2LcfBinding(16) @DMCXInteger(0)
    public IntegerR2kStruct faceIdx;
    @DMFXOBinding("@equipment") @DM2LcfBinding(51)
    public DM2Array<ShortR2kStruct> equipment;
    @DMFXOBinding("@no_weapon_attack_anim") @DM2LcfBinding(40) @DMCXInteger(1)
    public IntegerR2kStruct noWeaponAttackAnim;
    @DMFXOBinding("@class_2k3") @DM2LcfBinding(57) @DMCXInteger(0)
    public IntegerR2kStruct aClass;
    @DMFXOBinding("@battle_posx_2k3") @DM2LcfBinding(59) @DMCXInteger(220)
    public IntegerR2kStruct batPosX;
    @DMFXOBinding("@battle_posy_2k3") @DM2LcfBinding(60) @DMCXInteger(120)
    public IntegerR2kStruct batPosY;
    @DMFXOBinding("@editor_use_skillspanel_name") @DM2LcfBinding(66) @DMCXBoolean(false)
    public BooleanR2kStruct canRename;
    @DMFXOBinding("@battle_skillspanel_name") @DM2LcfBinding(67) @DMCXObject
    public StringR2kStruct renameResult;

    public Actor(DMContext ctx) {
        super(ctx, "RPG::Actor", 1);
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@equipment"))
            return equipment = new DM2Array<ShortR2kStruct>(context) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(context, 0);
                }
            };
        return super.dm2AddIVar(sym);
    }
}
