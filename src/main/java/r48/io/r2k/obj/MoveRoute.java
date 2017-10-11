/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.ArrayR2kStruct;
import r48.io.r2k.chunks.ArraySizeR2kInterpretable;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.struct.MoveCommand;

/**
 * You know, I have tons of classes in here just for SERIALIZING LCF STUFF.
 * seriously, it's getting ridiculous.
 * Created on 02/06/17.
 */
public class MoveRoute extends R2kObject {
    public ArraySizeR2kInterpretable<MoveCommand> listSize = new ArraySizeR2kInterpretable<MoveCommand>();

    public ArrayR2kStruct<MoveCommand> list = new ArrayR2kStruct<MoveCommand>(listSize, new ISupplier<MoveCommand>() {
        @Override
        public MoveCommand get() {
            return new MoveCommand();
        }
    }, false);

    public BooleanR2kStruct repeat = new BooleanR2kStruct(true);
    public BooleanR2kStruct skippable = new BooleanR2kStruct(false);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x0B, listSize),
                new Index(0x0C, list, "@list"),
                new Index(0x15, repeat, "@repeat"),
                new Index(0x16, skippable, "@skippable"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::MoveRoute", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
