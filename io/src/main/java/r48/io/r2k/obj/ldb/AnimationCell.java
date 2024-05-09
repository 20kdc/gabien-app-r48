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
 * Created on 07/06/17.
 */
public class AnimationCell extends DM2R2kObject {
    @DMFXOBinding("@visible") @DM2LcfBinding(1) @DMCXBoolean(true)
    public BooleanR2kStruct visible;
    @DMFXOBinding("@cell_id") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct cellId;
    @DMFXOBinding("@x") @DM2LcfBinding(3) @DMCXInteger(0)
    public IntegerR2kStruct x;
    @DMFXOBinding("@y") @DM2LcfBinding(4) @DMCXInteger(0)
    public IntegerR2kStruct y;
    @DMFXOBinding("@scale") @DM2LcfBinding(5) @DMCXInteger(100)
    public IntegerR2kStruct scale;
    @DMFXOBinding("@tone_r") @DM2LcfBinding(6) @DMCXInteger(100)
    public IntegerR2kStruct toneR;
    @DMFXOBinding("@tone_g") @DM2LcfBinding(7) @DMCXInteger(100)
    public IntegerR2kStruct toneG;
    @DMFXOBinding("@tone_b") @DM2LcfBinding(8) @DMCXInteger(100)
    public IntegerR2kStruct toneB;
    @DMFXOBinding("@tone_grey") @DM2LcfBinding(9) @DMCXInteger(100)
    public IntegerR2kStruct toneG2;
    @DMFXOBinding("@transparency") @DM2LcfBinding(10) @DMCXInteger(0)
    public IntegerR2kStruct transparency;

    public AnimationCell(DMContext ctx) {
        super(ctx, "RPG::Animation::Cell");
    }
}
