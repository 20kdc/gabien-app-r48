/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import java.util.function.Consumer;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.*;
import r48.io.r2k.dm2chk.*;

/**
 * Created on 3rd December 2017.
 */
public class SavePicture extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(0x01) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@start_x") @DM2LcfBinding(0x02) @DMCXInteger(0)
    public DoubleR2kStruct startX;
    @DMFXOBinding("@start_y") @DM2LcfBinding(0x03) @DMCXInteger(0)
    public DoubleR2kStruct startY;
    @DMFXOBinding("@x") @DM2LcfBinding(0x04) @DMCXInteger(0)
    public DoubleR2kStruct x;
    @DMFXOBinding("@y") @DM2LcfBinding(0x05) @DMCXInteger(0)
    public DoubleR2kStruct y;
    @DMFXOBinding("@fixed") @DM2LcfBinding(0x06) @DMCXBoolean(false)
    public BooleanR2kStruct fixed;
    // EasyRPG says this should have a default of -1 ; I'm not convinced,
    //  but that said does a case where this is default exist in the wild?
    // Test with -1, 100, and removed via optional if you want to find out.
    @DMFXOBinding("@magnify") @DM2LcfBinding(0x07) @DMCXInteger(100)
    public DoubleR2kStruct magnify;
    @DMFXOBinding("@transparency_top") @DM2LcfBinding(0x08) @DMCXInteger(0)
    public DoubleR2kStruct topTransparency;
    @DMFXOBinding("@transparency") @DM2LcfBinding(0x09) @DMCXBoolean(false)
    public BooleanR2kStruct transparency;
    @DMFXOBinding("@r") @DM2LcfBinding(0x0B) @DMCXInteger(100)
    public DoubleR2kStruct r;
    @DMFXOBinding("@g") @DM2LcfBinding(0x0C) @DMCXInteger(100)
    public DoubleR2kStruct g;
    @DMFXOBinding("@b") @DM2LcfBinding(0x0D) @DMCXInteger(100)
    public DoubleR2kStruct b;
    @DMFXOBinding("@s") @DM2LcfBinding(0x0E) @DMCXInteger(100)
    public DoubleR2kStruct s;
    @DMFXOBinding("@fx") @DM2LcfBinding(0x0F) @DMCXInteger(0)
    public IntegerR2kStruct fx;
    @DMFXOBinding("@fxstrength") @DM2LcfBinding(0x10) @DMCXInteger(0)
    public DoubleR2kStruct fxstrength;
    @DMFXOBinding("@transparency_bottom") @DM2LcfBinding(0x12) @DMCXInteger(0)
    public DoubleR2kStruct bottomTransparency;
    @DMFXOBinding("@spritesheet_cols_112") @DM2LcfBinding(0x13) @DMCXInteger(1)
    public IntegerR2kStruct spritesheetC112;
    @DMFXOBinding("@spritesheet_rows_112") @DM2LcfBinding(0x14) @DMCXInteger(1)
    public IntegerR2kStruct spritesheetR112;
    @DMFXOBinding("@spritesheet_frame_112") @DM2LcfBinding(0x15) @DMCXInteger(0)
    public IntegerR2kStruct spritesheetF112;
    @DMFXOBinding("@spritesheet_speed_112") @DM2LcfBinding(0x16) @DMCXInteger(0)
    public IntegerR2kStruct spritesheetS112;
    @DMFXOBinding("@lifetime") @DM2LcfBinding(0x17) @DMCXInteger(0)
    public IntegerR2kStruct lifetime;
    @DMFXOBinding("@spritesheet_oneshot_112") @DM2LcfBinding(0x18) @DMCXBoolean(false)
    public BooleanR2kStruct spritesheetOS112;
    @DMFXOBinding("@layer_map_112") @DM2LcfBinding(0x19) @DMCXInteger(7)
    public IntegerR2kStruct mapLayer112;
    @DMFXOBinding("@layer_battle_112") @DM2LcfBinding(0x1A) @DMCXInteger(0)
    public IntegerR2kStruct battleLayer112;
    @DMFXOBinding("@flags_112") @DM2LcfBinding(0x1B)
    public BitfieldR2kStruct flags112;
    public Consumer<SavePicture> flags112_add = (v) -> v.flags112 = new BitfieldR2kStruct(v.context, new String[] {
            "@erase_on_mapchange",
            "@erase_on_battleend",
            "@unused_1",
            "@unused_2",
            "@mod_tint",
            "@mod_flash",
            "@mod_shake",
    }, 0x61); // 0b01100001
    @DMFXOBinding("@target_x") @DM2LcfBinding(0x1F) @DMCXInteger(0)
    public DoubleR2kStruct targetX;
    @DMFXOBinding("@target_y") @DM2LcfBinding(0x20) @DMCXInteger(0)
    public DoubleR2kStruct targetY;
    @DMFXOBinding("@target_magnify") @DM2LcfBinding(0x21) @DMCXInteger(100)
    public IntegerR2kStruct targetMagnify;
    @DMFXOBinding("@target_transp_top") @DM2LcfBinding(0x22) @DMCXInteger(0)
    public IntegerR2kStruct targetTopTransparency;
    @DMFXOBinding("@target_transp_bottom") @DM2LcfBinding(0x23) @DMCXInteger(0)
    public IntegerR2kStruct targetBottomTransparency;
    @DMFXOBinding("@target_r") @DM2LcfBinding(0x29) @DMCXInteger(100)
    public IntegerR2kStruct targetR;
    @DMFXOBinding("@target_g") @DM2LcfBinding(0x2A) @DMCXInteger(100)
    public IntegerR2kStruct targetG;
    @DMFXOBinding("@target_b") @DM2LcfBinding(0x2B) @DMCXInteger(100)
    public IntegerR2kStruct targetB;
    @DMFXOBinding("@target_s") @DM2LcfBinding(0x2C) @DMCXInteger(100)
    public IntegerR2kStruct targetS;
    @DMFXOBinding("@target_fxstrength") @DM2LcfBinding(0x2E) @DMCXInteger(0)
    public IntegerR2kStruct targetFxStrength;
    @DMFXOBinding("@move_remain_time") @DM2LcfBinding(0x33) @DMCXInteger(0)
    public IntegerR2kStruct moveRemainTime;
    @DMFXOBinding("@rotation") @DM2LcfBinding(0x34) @DMCXInteger(0)
    public DoubleR2kStruct rotation;
    @DMFXOBinding("@waver") @DM2LcfBinding(0x35) @DMCXInteger(0)
    public IntegerR2kStruct waver;

    public SavePicture(DMContext ctx) {
        super(ctx, "RPG::SavePicture");
    }
}
