/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2Context;
import r48.io.data.DM2FXOBinding;
import r48.io.data.DMCXBoolean;
import r48.io.data.DMCXInteger;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 07/06/17.
 */
public class AnimationCell extends DM2R2kObject {
    @DM2FXOBinding("@visible") @DM2LcfBinding(1) @DMCXBoolean(true)
    public BooleanR2kStruct visible;
    @DM2FXOBinding("@cell_id") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct cellId;
    @DM2FXOBinding("@x") @DM2LcfBinding(3) @DMCXInteger(0)
    public IntegerR2kStruct x;
    @DM2FXOBinding("@y") @DM2LcfBinding(4) @DMCXInteger(0)
    public IntegerR2kStruct y;
    @DM2FXOBinding("@scale") @DM2LcfBinding(5) @DMCXInteger(100)
    public IntegerR2kStruct scale;
    @DM2FXOBinding("@tone_r") @DM2LcfBinding(6) @DMCXInteger(100)
    public IntegerR2kStruct toneR;
    @DM2FXOBinding("@tone_g") @DM2LcfBinding(7) @DMCXInteger(100)
    public IntegerR2kStruct toneG;
    @DM2FXOBinding("@tone_b") @DM2LcfBinding(8) @DMCXInteger(100)
    public IntegerR2kStruct toneB;
    @DM2FXOBinding("@tone_grey") @DM2LcfBinding(9) @DMCXInteger(100)
    public IntegerR2kStruct toneG2;
    @DM2FXOBinding("@transparency") @DM2LcfBinding(10) @DMCXInteger(0)
    public IntegerR2kStruct transparency;

    public AnimationCell(DM2Context ctx) {
        super(ctx, "RPG::Animation::Cell");
    }
}
