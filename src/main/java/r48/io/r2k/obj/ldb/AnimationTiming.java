/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.obj.Sound;

/**
 * Created on 07/06/17.
 */
public class AnimationTiming extends R2kObject {
    public IntegerR2kStruct frame = new IntegerR2kStruct(0);
    public Sound sound = new Sound();
    public IntegerR2kStruct flashScope = new IntegerR2kStruct(0);
    public IntegerR2kStruct flashRed = new IntegerR2kStruct(31);
    public IntegerR2kStruct flashGreen = new IntegerR2kStruct(31);
    public IntegerR2kStruct flashBlue = new IntegerR2kStruct(31);
    public IntegerR2kStruct flashPower = new IntegerR2kStruct(31);
    public IntegerR2kStruct screenShake = new IntegerR2kStruct(0);
    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, frame, "@frame"),
                new Index(0x02, sound, "@sound"),
                new Index(0x03, flashScope, "@flash_scope"),
                new Index(0x04, flashRed, "@flash_red"),
                new Index(0x05, flashGreen, "@flash_green"),
                new Index(0x06, flashBlue, "@flash_blue"),
                new Index(0x07, flashPower, "@flash_power"),
                new Index(0x08, screenShake, "@screen_shake")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Animation::Timing", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
