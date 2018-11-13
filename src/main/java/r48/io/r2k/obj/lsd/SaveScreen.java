/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.DoubleR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;

/**
 * Created on 14th December 2017.
 */
public class SaveScreen extends R2kObject {
    public IntegerR2kStruct tintEndR = new IntegerR2kStruct(100);
    public IntegerR2kStruct tintEndG = new IntegerR2kStruct(100);
    public IntegerR2kStruct tintEndB = new IntegerR2kStruct(100);
    public IntegerR2kStruct tintEndS = new IntegerR2kStruct(100);

    public DoubleR2kStruct tintR = new DoubleR2kStruct(100);
    public DoubleR2kStruct tintG = new DoubleR2kStruct(100);
    public DoubleR2kStruct tintB = new DoubleR2kStruct(100);
    public DoubleR2kStruct tintS = new DoubleR2kStruct(100);
    public IntegerR2kStruct tintFL = new IntegerR2kStruct(0);

    public BooleanR2kStruct flashContinuous = new BooleanR2kStruct(false);
    public IntegerR2kStruct flashR = new IntegerR2kStruct(0);
    public IntegerR2kStruct flashG = new IntegerR2kStruct(0);
    public IntegerR2kStruct flashB = new IntegerR2kStruct(0);
    public DoubleR2kStruct flashPosition = new DoubleR2kStruct(0);
    public IntegerR2kStruct flashFramesLeft = new IntegerR2kStruct(0);

    public BooleanR2kStruct shakeContinuous = new BooleanR2kStruct(false);
    public IntegerR2kStruct shakeStrength = new IntegerR2kStruct(0);
    public IntegerR2kStruct shakeSpeed = new IntegerR2kStruct(0);
    public IntegerR2kStruct shakeX = new IntegerR2kStruct(0);
    public IntegerR2kStruct shakeY = new IntegerR2kStruct(0);
    public IntegerR2kStruct shakeFramesLeft = new IntegerR2kStruct(0);

    public IntegerR2kStruct panX = new IntegerR2kStruct(0);
    public IntegerR2kStruct panY = new IntegerR2kStruct(0);
    public IntegerR2kStruct battleanimId = new IntegerR2kStruct(0);
    public IntegerR2kStruct battleanimTarget = new IntegerR2kStruct(0);
    public IntegerR2kStruct battleanimFrame = new IntegerR2kStruct(0);
    public BooleanR2kStruct battleanim2E = new BooleanR2kStruct(false);
    public BooleanR2kStruct battleanimGlobal = new BooleanR2kStruct(false);

    public IntegerR2kStruct weather = new IntegerR2kStruct(0);
    public IntegerR2kStruct weatherStrength = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, tintEndR, "@tint_end_r"),
                new Index(0x02, tintEndG, "@tint_end_g"),
                new Index(0x03, tintEndB, "@tint_end_b"),
                new Index(0x04, tintEndS, "@tint_end_s"),
                new Index(0x0B, tintR, "@tint_r"),
                new Index(0x0C, tintG, "@tint_g"),
                new Index(0x0D, tintB, "@tint_b"),
                new Index(0x0E, tintS, "@tint_s"),
                new Index(0x0F, tintFL, "@tint_frames_left"),

                new Index(0x14, flashContinuous, "@flash_continuous"),
                new Index(0x15, flashR, "@flash_r"),
                new Index(0x16, flashG, "@flash_g"),
                new Index(0x17, flashB, "@flash_b"),
                new Index(0x18, flashPosition, "@flash_position"),
                new Index(0x19, flashFramesLeft, "@flash_frames_left"),

                new Index(0x1E, shakeContinuous, "@shake_continuous"),
                new Index(0x1F, shakeStrength, "@shake_strength"),
                new Index(0x20, shakeSpeed, "@shake_speed"),
                new Index(0x21, shakeX, "@shake_x"),
                new Index(0x22, shakeY, "@shake_y"),
                new Index(0x23, shakeFramesLeft, "@shake_frames_left"),

                new Index(0x29, panX, "@pan_x"),
                new Index(0x2A, panY, "@pan_y"),
                new Index(0x2B, battleanimId, "@battleanim_id"),
                new Index(0x2C, battleanimTarget, "@battleanim_target"),
                new Index(0x2D, battleanimFrame, "@battleanim_frame"),
                new Index(0x2E, battleanim2E, "@battleanim_active"),
                new Index(0x2F, battleanimGlobal, "@battleanim_global"),

                new Index(0x30, weather, "@weather"),
                new Index(0x31, weatherStrength, "@weather_strength"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SaveScreen", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
