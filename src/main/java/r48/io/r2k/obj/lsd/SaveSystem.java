/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;

public class SaveSystem extends R2kObject {
    public IntegerR2kStruct screen = new IntegerR2kStruct(1);
    public IntegerR2kStruct frameCount = new IntegerR2kStruct(0);
    public StringR2kStruct systemName = new StringR2kStruct();
    // This is different from the documented default - test this out?
    public IntegerR2kStruct messageStretch = new IntegerR2kStruct(0);
    public IntegerR2kStruct fontId = new IntegerR2kStruct(0);
    public ArraySizeR2kInterpretable<BooleanR2kStruct> switchesSize = new ArraySizeR2kInterpretable<BooleanR2kStruct>(true);
    public ArrayR2kStruct<BooleanR2kStruct> switches = new ArrayR2kStruct<BooleanR2kStruct>(switchesSize, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(false);
        }
    });
    public ArraySizeR2kInterpretable<Int32R2kStruct> variablesSize = new ArraySizeR2kInterpretable<Int32R2kStruct>(true);
    public ArrayR2kStruct<Int32R2kStruct> variables = new ArrayR2kStruct<Int32R2kStruct>(variablesSize, new ISupplier<Int32R2kStruct>() {
        @Override
        public Int32R2kStruct get() {
            return new Int32R2kStruct(0);
        }
    });
    public IntegerR2kStruct messageTransparent = new IntegerR2kStruct(0);
    public IntegerR2kStruct messagePosition = new IntegerR2kStruct(2);
    public IntegerR2kStruct messagePreventOverlap = new IntegerR2kStruct(1);
    public IntegerR2kStruct messageContinueEvents = new IntegerR2kStruct(0);

    public StringR2kStruct faceName = new StringR2kStruct();
    public IntegerR2kStruct faceIdx = new IntegerR2kStruct(0);
    public BooleanR2kStruct faceRight = new BooleanR2kStruct(false);
    public BooleanR2kStruct faceFlip = new BooleanR2kStruct(false);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, screen, "@screen"),
                new Index(0x0B, frameCount, "@frame_count"),
                new Index(0x15, systemName, "@system_name"),
                new Index(0x16, messageStretch, "@message_tiling"),
                new Index(0x17, fontId, "@font_id"),
                new Index(0x1F, switchesSize),
                new Index(0x20, switches, "@switches"),
                new Index(0x21, variablesSize),
                new Index(0x22, variables, "@variables"),
                new Index(0x29, messageTransparent, "@message_transparent"),
                new Index(0x2A, messagePosition, "@message_position"),
                new Index(0x2B, messagePreventOverlap, "@message_prevent_overlap"),
                new Index(0x2C, messageContinueEvents, "@message_continue_events"),
                new Index(0x33, faceName, "@face_name"),
                new Index(0x34, faceIdx, "@face_index"),
                new Index(0x35, faceRight, "@face_right"),
                new Index(0x36, faceFlip, "@face_flip"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SaveSystem", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
