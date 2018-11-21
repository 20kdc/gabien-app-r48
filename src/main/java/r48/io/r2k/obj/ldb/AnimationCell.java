/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;

/**
 * Created on 07/06/17.
 */
public class AnimationCell extends R2kObject {
    public BooleanR2kStruct visible = new BooleanR2kStruct(true);
    public IntegerR2kStruct cellId = new IntegerR2kStruct(0);
    public IntegerR2kStruct x = new IntegerR2kStruct(0);
    public IntegerR2kStruct y = new IntegerR2kStruct(0);
    public IntegerR2kStruct scale = new IntegerR2kStruct(100);
    public IntegerR2kStruct toneR = new IntegerR2kStruct(100);
    public IntegerR2kStruct toneG = new IntegerR2kStruct(100);
    public IntegerR2kStruct toneB = new IntegerR2kStruct(100);
    public IntegerR2kStruct toneG2 = new IntegerR2kStruct(100);
    public IntegerR2kStruct transparency = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, visible, "@visible"),
                new Index(0x02, cellId, "@cell_id"),
                new Index(0x03, x, "@x"),
                new Index(0x04, y, "@y"),
                new Index(0x05, scale, "@scale"),
                new Index(0x06, toneR, "@tone_r"),
                new Index(0x07, toneG, "@tone_g"),
                new Index(0x08, toneB, "@tone_b"),
                new Index(0x09, toneG2, "@tone_grey"),
                new Index(0x0A, transparency, "@transparency"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Animation::Cell", true);
        asRIOISF(rio);
        return rio;
    }
}
