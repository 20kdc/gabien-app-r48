/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 06/06/17.
 */
public class ItemAnimation extends DM2R2kObject {
    @DM2FXOBinding("@type") @DM2LcfBinding(0x03) @DMCXInteger(0)
    public IntegerR2kStruct type;
    @DM2FXOBinding("@weapon_batanim_idx") @DM2LcfBinding(0x04) @DMCXInteger(0)
    public IntegerR2kStruct weaponAnim;
    @DM2FXOBinding("@movement") @DM2LcfBinding(0x05) @DMCXInteger(0)
    public IntegerR2kStruct movement;
    @DM2FXOBinding("@has_afterimage") @DM2LcfBinding(0x06) @DMCXInteger(0)
    public IntegerR2kStruct afterImage;
    @DM2FXOBinding("@loop_count") @DM2LcfBinding(0x07) @DMCXInteger(0)
    public IntegerR2kStruct attacks;
    @DM2FXOBinding("@ranged") @DM2LcfBinding(0x08) @DMCXBoolean(false)
    public BooleanR2kStruct ranged;
    @DM2FXOBinding("@ranged_batanim_idx") @DM2LcfBinding(0x09) @DMCXInteger(0)
    public IntegerR2kStruct rangedAnim;
    @DM2FXOBinding("@ranged_speed") @DM2LcfBinding(0x0C) @DMCXInteger(0)
    public IntegerR2kStruct rangedSpeed;
    @DM2FXOBinding("@battle_anim") @DM2LcfBinding(0x0D) @DMCXInteger(0)
    public IntegerR2kStruct battleAnim;

    public ItemAnimation(DM2Context ctx) {
        super(ctx, "RPG::ItemAnimation");
    }
}
