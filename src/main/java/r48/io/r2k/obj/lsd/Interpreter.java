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
import r48.io.r2k.struct.EventCommand;

public class Interpreter extends R2kObject {
    public SparseArrayAR2kStruct<InterpreterStackLevel> commands = new SparseArrayAR2kStruct<InterpreterStackLevel>(new ISupplier<InterpreterStackLevel>() {
        @Override
        public InterpreterStackLevel get() {
            return new InterpreterStackLevel();
        }
    });
    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, commands, "@stack")
        };
    }


    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Interpreter", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }

    private class InterpreterStackLevel extends R2kObject {
        public IntegerR2kStruct listSize = new IntegerR2kStruct(0);
        /*
        public ArrayR2kStruct<EventCommand> list = new ArrayR2kStruct<EventCommand>(null, new ISupplier<EventCommand>() {
            @Override
            public EventCommand get() {
                return new EventCommand();
            }
        });*/
        public BlobR2kStruct list = new BlobR2kStruct(new ISupplier<byte[]>() {
            @Override
            public byte[] get() {
                return new byte[0];
            }
        });
        public IntegerR2kStruct index = new IntegerR2kStruct(0);
        public IntegerR2kStruct eventId = new IntegerR2kStruct(0);
        public BooleanR2kStruct actioned = new BooleanR2kStruct(false);


        @Override
        public Index[] getIndices() {
            return new Index[] {
                    new Index(0x01, listSize, "@list_size"),
                    new Index(0x02, list, "@list"),
                    new Index(0x0B, index, "@index"),
                    new Index(0x0C, eventId, "@event_id"),
                    new Index(0x0D, actioned, "@actioned"),
            };
        }

        @Override
        public RubyIO asRIO() {
            RubyIO rio = new RubyIO().setSymlike("RPG::InterpreterStackLevel", true);
            asRIOISF(rio);
            return rio;
        }

        @Override
        public void fromRIO(RubyIO src) {
            fromRIOISF(src);
        }
    }
}
