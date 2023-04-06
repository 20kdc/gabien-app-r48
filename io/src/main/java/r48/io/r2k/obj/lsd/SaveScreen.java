/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.DoubleR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 14th December 2017.
 */
public class SaveScreen extends DM2R2kObject {
    @DM2FXOBinding("@tint_end_r") @DM2LcfBinding(0x01) @DMCXInteger(100)
    public IntegerR2kStruct tintEndR;
    @DM2FXOBinding("@tint_end_g") @DM2LcfBinding(0x02) @DMCXInteger(100)
    public IntegerR2kStruct tintEndG;
    @DM2FXOBinding("@tint_end_b") @DM2LcfBinding(0x03) @DMCXInteger(100)
    public IntegerR2kStruct tintEndB;
    @DM2FXOBinding("@tint_end_s") @DM2LcfBinding(0x04) @DMCXInteger(100)
    public IntegerR2kStruct tintEndS;

    @DM2FXOBinding("@tint_r") @DM2LcfBinding(0x0B) @DMCXInteger(100)
    public DoubleR2kStruct tintR;
    @DM2FXOBinding("@tint_g") @DM2LcfBinding(0x0C) @DMCXInteger(100)
    public DoubleR2kStruct tintG;
    @DM2FXOBinding("@tint_b") @DM2LcfBinding(0x0D) @DMCXInteger(100)
    public DoubleR2kStruct tintB;
    @DM2FXOBinding("@tint_s") @DM2LcfBinding(0x0E) @DMCXInteger(100)
    public DoubleR2kStruct tintS;
    @DM2FXOBinding("@tint_frames_left") @DM2LcfBinding(0x0F) @DMCXInteger(0)
    public IntegerR2kStruct tintFL;

    @DM2FXOBinding("@flash_continuous") @DM2LcfBinding(0x14) @DMCXBoolean(false)
    public BooleanR2kStruct flashContinuous;
    @DM2FXOBinding("@flash_r") @DM2LcfBinding(0x15) @DMCXInteger(0)
    public IntegerR2kStruct flashR;
    @DM2FXOBinding("@flash_g") @DM2LcfBinding(0x16) @DMCXInteger(0)
    public IntegerR2kStruct flashG;
    @DM2FXOBinding("@flash_b") @DM2LcfBinding(0x17) @DMCXInteger(0)
    public IntegerR2kStruct flashB;
    @DM2FXOBinding("@flash_position") @DM2LcfBinding(0x18) @DMCXInteger(0)
    public DoubleR2kStruct flashPosition;
    @DM2FXOBinding("@flash_frames_left") @DM2LcfBinding(0x19) @DMCXInteger(0)
    public IntegerR2kStruct flashFramesLeft;

    @DM2FXOBinding("@shake_continuous") @DM2LcfBinding(0x1E) @DMCXBoolean(false)
    public BooleanR2kStruct shakeContinuous;
    @DM2FXOBinding("@shake_strength") @DM2LcfBinding(0x1F) @DMCXInteger(0)
    public IntegerR2kStruct shakeStrength;
    @DM2FXOBinding("@shake_speed") @DM2LcfBinding(0x20) @DMCXInteger(0)
    public IntegerR2kStruct shakeSpeed;
    @DM2FXOBinding("@shake_x") @DM2LcfBinding(0x21) @DMCXInteger(0)
    public IntegerR2kStruct shakeX;
    @DM2FXOBinding("@shake_y") @DM2LcfBinding(0x22) @DMCXInteger(0)
    public IntegerR2kStruct shakeY;
    @DM2FXOBinding("@shake_frames_left") @DM2LcfBinding(0x23) @DMCXInteger(0)
    public IntegerR2kStruct shakeFramesLeft;

    @DM2FXOBinding("@pan_x") @DM2LcfBinding(0x29) @DMCXInteger(0)
    public IntegerR2kStruct panX;
    @DM2FXOBinding("@pan_y") @DM2LcfBinding(0x2A) @DMCXInteger(0)
    public IntegerR2kStruct panY;
    @DM2FXOBinding("@battleanim_id") @DM2LcfBinding(0x2B) @DMCXInteger(0)
    public IntegerR2kStruct battleanimId;
    @DM2FXOBinding("@battleanim_target") @DM2LcfBinding(0x2C) @DMCXInteger(0)
    public IntegerR2kStruct battleanimTarget;
    @DM2FXOBinding("@battleanim_frame") @DM2LcfBinding(0x2D) @DMCXInteger(0)
    public IntegerR2kStruct battleanimFrame;
    @DM2FXOBinding("@battleanim_active") @DM2LcfBinding(0x2E) @DMCXBoolean(false)
    public BooleanR2kStruct battleanim2E;
    @DM2FXOBinding("@battleanim_global") @DM2LcfBinding(0x2F) @DMCXBoolean(false)
    public BooleanR2kStruct battleanimGlobal;

    @DM2FXOBinding("@weather") @DM2LcfBinding(0x30) @DMCXInteger(0)
    public IntegerR2kStruct weather;
    @DM2FXOBinding("@weather_strength") @DM2LcfBinding(0x31) @DMCXInteger(0)
    public IntegerR2kStruct weatherStrength;

    public SaveScreen(DM2Context ctx) {
        super(ctx, "RPG::SaveScreen");
    }
}
