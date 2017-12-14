/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;

/**
 * Created on 3rd December 2017.
 */
public class SavePicture extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public DoubleR2kStruct startX = new DoubleR2kStruct();
    public DoubleR2kStruct startY = new DoubleR2kStruct();
    public DoubleR2kStruct x = new DoubleR2kStruct();
    public DoubleR2kStruct y = new DoubleR2kStruct();
    public BooleanR2kStruct fixed = new BooleanR2kStruct(false);
    public DoubleR2kStruct magnify = new DoubleR2kStruct(100);
    public DoubleR2kStruct topTransparency = new DoubleR2kStruct();
    public BooleanR2kStruct transparency = new BooleanR2kStruct(false);
    public DoubleR2kStruct r = new DoubleR2kStruct(100);
    public DoubleR2kStruct g = new DoubleR2kStruct(100);
    public DoubleR2kStruct b = new DoubleR2kStruct(100);
    public DoubleR2kStruct s = new DoubleR2kStruct(100);
    public IntegerR2kStruct fx = new IntegerR2kStruct(0);
    public DoubleR2kStruct fxstrength = new DoubleR2kStruct();
    public DoubleR2kStruct bottomTransparency = new DoubleR2kStruct();
    public IntegerR2kStruct spritesheetC112 = new IntegerR2kStruct(1);
    public IntegerR2kStruct spritesheetR112 = new IntegerR2kStruct(1);
    public IntegerR2kStruct spritesheetF112 = new IntegerR2kStruct(0);
    public IntegerR2kStruct spritesheetS112 = new IntegerR2kStruct(0);
    public IntegerR2kStruct lifetime = new IntegerR2kStruct(0);
    public BooleanR2kStruct spritesheetOS112 = new BooleanR2kStruct(false);
    public IntegerR2kStruct mapLayer112 = new IntegerR2kStruct(7);
    public IntegerR2kStruct battleLayer112 = new IntegerR2kStruct(0);
    public BitfieldR2kStruct flags112 = new BitfieldR2kStruct(new String[] {
            "@erase_on_mapchange",
            "@erase_on_battleend",
            "@unused_1",
            "@unused_2",
            "@mod_tint",
            "@mod_flash",
            "@mod_shake",
    });
    public DoubleR2kStruct targetX = new DoubleR2kStruct();
    public DoubleR2kStruct targetY = new DoubleR2kStruct();
    public IntegerR2kStruct targetMagnify = new IntegerR2kStruct(100);
    public IntegerR2kStruct targetTopTransparency = new IntegerR2kStruct(0);
    public IntegerR2kStruct targetBottomTransparency = new IntegerR2kStruct(0);
    public IntegerR2kStruct targetR = new IntegerR2kStruct(100);
    public IntegerR2kStruct targetG = new IntegerR2kStruct(100);
    public IntegerR2kStruct targetB = new IntegerR2kStruct(100);
    public IntegerR2kStruct targetS = new IntegerR2kStruct(100);
    public IntegerR2kStruct targetFxStrength = new IntegerR2kStruct(0);
    public IntegerR2kStruct moveRemainTime = new IntegerR2kStruct(0);
    public DoubleR2kStruct rotation = new DoubleR2kStruct(0);
    public IntegerR2kStruct waver = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, startX, "@start_x"),
                new Index(0x03, startY, "@start_y"),
                new Index(0x04, x, "@x"),
                new Index(0x05, y, "@y"),
                new Index(0x06, fixed, "@fixed"),
                new Index(0x07, magnify, "@magnify"),
                new Index(0x08, topTransparency, "@transparency_top"),
                new Index(0x09, transparency, "@transparency"),
                new Index(0x0B, r, "@r"),
                new Index(0x0C, g, "@g"),
                new Index(0x0D, b, "@b"),
                new Index(0x0E, s, "@s"),
                new Index(0x0F, fx, "@fx"),
                new Index(0x10, fxstrength, "@fxstrength"),
                new Index(0x12, bottomTransparency, "@transparency_bottom"),
                new Index(0x13, spritesheetC112, "@spritesheet_cols_112"),
                new Index(0x14, spritesheetR112, "@spritesheet_rows_112"),
                new Index(0x15, spritesheetF112, "@spritesheet_frame_112"),
                new Index(0x16, spritesheetS112, "@spritesheet_speed_112"),
                new Index(0x17, lifetime, "@lifetime"),
                new Index(0x18, spritesheetOS112, "@spritesheet_oneshot_112"),
                new Index(0x19, mapLayer112, "@layer_map_112"),
                new Index(0x1A, battleLayer112, "@layer_battle_112"),
                new Index(0x1B, flags112, "@flags_112"),
                new Index(0x1F, targetX, "@target_x"),
                new Index(0x20, targetY, "@target_y"),
                new Index(0x21, targetMagnify, "@target_magnify"),
                new Index(0x22, targetTopTransparency, "@target_transp_top"),
                new Index(0x23, targetBottomTransparency, "@target_transp_bottom"),
                new Index(0x29, targetR, "@target_r"),
                new Index(0x2A, targetG, "@target_g"),
                new Index(0x2B, targetB, "@target_b"),
                new Index(0x2C, targetS, "@target_s"),
                new Index(0x2E, targetFxStrength, "@target_fxstrength"),
                new Index(0x33, moveRemainTime, "@move_remain_time"),
                new Index(0x34, rotation, "@rotation"),
                new Index(0x35, waver, "@waver"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SavePicture", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
