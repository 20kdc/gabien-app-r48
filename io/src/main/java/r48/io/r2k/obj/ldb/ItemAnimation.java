/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
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
    @DMFXOBinding("@type") @DM2LcfBinding(0x03) @DMCXInteger(0)
    public IntegerR2kStruct type;
    @DMFXOBinding("@weapon_batanim_idx") @DM2LcfBinding(0x04) @DMCXInteger(0)
    public IntegerR2kStruct weaponAnim;
    @DMFXOBinding("@movement") @DM2LcfBinding(0x05) @DMCXInteger(0)
    public IntegerR2kStruct movement;
    @DMFXOBinding("@has_afterimage") @DM2LcfBinding(0x06) @DMCXInteger(0)
    public IntegerR2kStruct afterImage;
    @DMFXOBinding("@loop_count") @DM2LcfBinding(0x07) @DMCXInteger(0)
    public IntegerR2kStruct attacks;
    @DMFXOBinding("@ranged") @DM2LcfBinding(0x08) @DMCXBoolean(false)
    public BooleanR2kStruct ranged;
    @DMFXOBinding("@ranged_batanim_idx") @DM2LcfBinding(0x09) @DMCXInteger(0)
    public IntegerR2kStruct rangedAnim;
    @DMFXOBinding("@ranged_speed") @DM2LcfBinding(0x0C) @DMCXInteger(0)
    public IntegerR2kStruct rangedSpeed;
    @DMFXOBinding("@battle_anim") @DM2LcfBinding(0x0D) @DMCXInteger(0)
    public IntegerR2kStruct battleAnim;

    public ItemAnimation(DMContext ctx) {
        super(ctx, "RPG::ItemAnimation");
    }
}
