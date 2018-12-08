/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.*;
import r48.io.r2k.dm2chk.*;

/**
 * Created on 3rd December 2017.
 */
public class SavePicture extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(0x01) @DM2LcfObject
    public StringR2kStruct name;
    @DM2FXOBinding("@start_x") @DM2LcfBinding(0x02) @DM2LcfInteger(0)
    public DoubleR2kStruct startX;
    @DM2FXOBinding("@start_y") @DM2LcfBinding(0x03) @DM2LcfInteger(0)
    public DoubleR2kStruct startY;
    @DM2FXOBinding("@x") @DM2LcfBinding(0x04) @DM2LcfInteger(0)
    public DoubleR2kStruct x;
    @DM2FXOBinding("@y") @DM2LcfBinding(0x05) @DM2LcfInteger(0)
    public DoubleR2kStruct y;
    @DM2FXOBinding("@fixed") @DM2LcfBinding(0x06) @DM2LcfBoolean(false)
    public BooleanR2kStruct fixed;
    // EasyRPG says this should have a default of -1 ; I'm not convinced,
    //  but that said does a case where this is default exist in the wild?
    // Test with -1, 100, and removed via optional if you want to find out.
    @DM2FXOBinding("@magnify") @DM2LcfBinding(0x07) @DM2LcfInteger(100)
    public DoubleR2kStruct magnify;
    @DM2FXOBinding("@transparency_top") @DM2LcfBinding(0x08) @DM2LcfInteger(0)
    public DoubleR2kStruct topTransparency;
    @DM2FXOBinding("@transparency") @DM2LcfBinding(0x09) @DM2LcfBoolean(false)
    public BooleanR2kStruct transparency;
    @DM2FXOBinding("@r") @DM2LcfBinding(0x0B) @DM2LcfInteger(100)
    public DoubleR2kStruct r;
    @DM2FXOBinding("@g") @DM2LcfBinding(0x0C) @DM2LcfInteger(100)
    public DoubleR2kStruct g;
    @DM2FXOBinding("@b") @DM2LcfBinding(0x0D) @DM2LcfInteger(100)
    public DoubleR2kStruct b;
    @DM2FXOBinding("@s") @DM2LcfBinding(0x0E) @DM2LcfInteger(100)
    public DoubleR2kStruct s;
    @DM2FXOBinding("@fx") @DM2LcfBinding(0x0F) @DM2LcfInteger(0)
    public IntegerR2kStruct fx;
    @DM2FXOBinding("@fxstrength") @DM2LcfBinding(0x10) @DM2LcfInteger(0)
    public DoubleR2kStruct fxstrength;
    @DM2FXOBinding("@transparency_bottom") @DM2LcfBinding(0x12) @DM2LcfInteger(0)
    public DoubleR2kStruct bottomTransparency;
    @DM2FXOBinding("@spritesheet_cols_112") @DM2LcfBinding(0x13) @DM2LcfInteger(1)
    public IntegerR2kStruct spritesheetC112;
    @DM2FXOBinding("@spritesheet_rows_112") @DM2LcfBinding(0x14) @DM2LcfInteger(1)
    public IntegerR2kStruct spritesheetR112;
    @DM2FXOBinding("@spritesheet_frame_112") @DM2LcfBinding(0x15) @DM2LcfInteger(0)
    public IntegerR2kStruct spritesheetF112;
    @DM2FXOBinding("@spritesheet_speed_112") @DM2LcfBinding(0x16) @DM2LcfInteger(0)
    public IntegerR2kStruct spritesheetS112;
    @DM2FXOBinding("@lifetime") @DM2LcfBinding(0x17) @DM2LcfInteger(0)
    public IntegerR2kStruct lifetime;
    @DM2FXOBinding("@spritesheet_oneshot_112") @DM2LcfBinding(0x18) @DM2LcfBoolean(false)
    public BooleanR2kStruct spritesheetOS112;
    @DM2FXOBinding("@layer_map_112") @DM2LcfBinding(0x19) @DM2LcfInteger(7)
    public IntegerR2kStruct mapLayer112;
    @DM2FXOBinding("@layer_battle_112") @DM2LcfBinding(0x1A) @DM2LcfInteger(0)
    public IntegerR2kStruct battleLayer112;
    @DM2FXOBinding("@flags_112") @DM2LcfBinding(0x1B)
    public BitfieldR2kStruct flags112;
    @DM2FXOBinding("@target_x") @DM2LcfBinding(0x1F) @DM2LcfInteger(0)
    public DoubleR2kStruct targetX;
    @DM2FXOBinding("@target_y") @DM2LcfBinding(0x20) @DM2LcfInteger(0)
    public DoubleR2kStruct targetY;
    @DM2FXOBinding("@target_magnify") @DM2LcfBinding(0x21) @DM2LcfInteger(100)
    public IntegerR2kStruct targetMagnify;
    @DM2FXOBinding("@target_transp_top") @DM2LcfBinding(0x22) @DM2LcfInteger(0)
    public IntegerR2kStruct targetTopTransparency;
    @DM2FXOBinding("@target_transp_bottom") @DM2LcfBinding(0x23) @DM2LcfInteger(0)
    public IntegerR2kStruct targetBottomTransparency;
    @DM2FXOBinding("@target_r") @DM2LcfBinding(0x29) @DM2LcfInteger(100)
    public IntegerR2kStruct targetR;
    @DM2FXOBinding("@target_g") @DM2LcfBinding(0x2A) @DM2LcfInteger(100)
    public IntegerR2kStruct targetG;
    @DM2FXOBinding("@target_b") @DM2LcfBinding(0x2B) @DM2LcfInteger(100)
    public IntegerR2kStruct targetB;
    @DM2FXOBinding("@target_s") @DM2LcfBinding(0x2C) @DM2LcfInteger(100)
    public IntegerR2kStruct targetS;
    @DM2FXOBinding("@target_fxstrength") @DM2LcfBinding(0x2E) @DM2LcfInteger(0)
    public IntegerR2kStruct targetFxStrength;
    @DM2FXOBinding("@move_remain_time") @DM2LcfBinding(0x33) @DM2LcfInteger(0)
    public IntegerR2kStruct moveRemainTime;
    @DM2FXOBinding("@rotation") @DM2LcfBinding(0x34) @DM2LcfInteger(0)
    public DoubleR2kStruct rotation;
    @DM2FXOBinding("@waver") @DM2LcfBinding(0x35) @DM2LcfInteger(0)
    public IntegerR2kStruct waver;

    public SavePicture() {
        super("RPG::SavePicture");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@flags_112"))
            return flags112 = new BitfieldR2kStruct(new String[] {
                    "@erase_on_mapchange",
                    "@erase_on_battleend",
                    "@unused_1",
                    "@unused_2",
                    "@mod_tint",
                    "@mod_flash",
                    "@mod_shake",
            }, 0x61); // 0b01100001
        return super.dm2AddIVar(sym);
    }
}
