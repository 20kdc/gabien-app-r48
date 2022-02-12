/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2LcfBoolean;
import r48.io.r2k.dm2chk.DM2LcfInteger;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 06/06/17.
 */
public class ItemAnimation extends DM2R2kObject {
    @DM2FXOBinding("@type") @DM2LcfBinding(0x03) @DM2LcfInteger(0)
    public IntegerR2kStruct type;
    @DM2FXOBinding("@weapon_batanim_idx") @DM2LcfBinding(0x04) @DM2LcfInteger(0)
    public IntegerR2kStruct weaponAnim;
    @DM2FXOBinding("@movement") @DM2LcfBinding(0x05) @DM2LcfInteger(0)
    public IntegerR2kStruct movement;
    @DM2FXOBinding("@has_afterimage") @DM2LcfBinding(0x06) @DM2LcfInteger(0)
    public IntegerR2kStruct afterImage;
    @DM2FXOBinding("@loop_count") @DM2LcfBinding(0x07) @DM2LcfInteger(0)
    public IntegerR2kStruct attacks;
    @DM2FXOBinding("@ranged") @DM2LcfBinding(0x08) @DM2LcfBoolean(false)
    public BooleanR2kStruct ranged;
    @DM2FXOBinding("@ranged_batanim_idx") @DM2LcfBinding(0x09) @DM2LcfInteger(0)
    public IntegerR2kStruct rangedAnim;
    @DM2FXOBinding("@ranged_speed") @DM2LcfBinding(0x0C) @DM2LcfInteger(0)
    public IntegerR2kStruct rangedSpeed;
    @DM2FXOBinding("@battle_anim") @DM2LcfBinding(0x0D) @DM2LcfInteger(0)
    public IntegerR2kStruct battleAnim;

    public ItemAnimation() {
        super("RPG::ItemAnimation");
    }
}
