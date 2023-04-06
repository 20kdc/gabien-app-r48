/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;
import r48.io.r2k.obj.Sound;

/**
 * Created on 07/06/17.
 */
public class AnimationTiming extends DM2R2kObject {
    @DM2FXOBinding("@frame") @DM2LcfBinding(1) @DMCXInteger(0)
    public IntegerR2kStruct frame;
    @DM2FXOBinding("@sound") @DM2LcfBinding(2) @DMCXObject
    public Sound sound;
    @DM2FXOBinding("@flash_scope") @DM2LcfBinding(3) @DMCXInteger(0)
    public IntegerR2kStruct flashScope;
    @DM2FXOBinding("@flash_red") @DM2LcfBinding(4) @DMCXInteger(31)
    public IntegerR2kStruct flashRed;
    @DM2FXOBinding("@flash_green") @DM2LcfBinding(5) @DMCXInteger(31)
    public IntegerR2kStruct flashGreen;
    @DM2FXOBinding("@flash_blue") @DM2LcfBinding(6) @DMCXInteger(31)
    public IntegerR2kStruct flashBlue;
    @DM2FXOBinding("@flash_power") @DM2LcfBinding(7) @DMCXInteger(31)
    public IntegerR2kStruct flashPower;
    @DM2FXOBinding("@screen_shake") @DM2LcfBinding(8) @DMCXInteger(0)
    public IntegerR2kStruct screenShake;

    public AnimationTiming(DM2Context ctx) {
        super(ctx, "RPG::Animation::Timing");
    }
}
