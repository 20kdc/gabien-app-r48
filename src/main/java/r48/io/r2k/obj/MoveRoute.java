/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
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
