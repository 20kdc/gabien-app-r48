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

/**
 * aka 'SaveEventData'
 */
public class Interpreter extends R2kObject {
    public SparseArrayAR2kStruct<InterpreterStackLevel> commands = new SparseArrayAR2kStruct<InterpreterStackLevel>(new ISupplier<InterpreterStackLevel>() {
        @Override
        public InterpreterStackLevel get() {
            return new InterpreterStackLevel();
        }
    });
    public BooleanR2kStruct shownMessage = new BooleanR2kStruct(false);
    public BooleanR2kStruct waitingMovementEnd = new BooleanR2kStruct(false);
    public IntegerR2kStruct waitingSlp1 = new IntegerR2kStruct(0);
    public IntegerR2kStruct waitingSlp2 = new IntegerR2kStruct(0);
    public BooleanR2kStruct kiiWait = new BooleanR2kStruct(false);
    // Yes, really. Don't trust this
    public ByteR2kStruct kiiVariable = new ByteR2kStruct(0);
    public BooleanR2kStruct kiiTimed  = new BooleanR2kStruct(false);
    public IntegerR2kStruct kiiTimeVariable = new IntegerR2kStruct(0);
    public BooleanR2kStruct kiiFilterArrowkeys = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilterDecision  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilterCancel  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter1A  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter1B  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter1C  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter1D  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter1E  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter23  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter24  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter25  = new BooleanR2kStruct(false);
    public BooleanR2kStruct kiiFilter26  = new BooleanR2kStruct(false);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, commands, "@stack"),
                new Index(0x04, shownMessage, "@shown_message"),
                new Index(0x0D, waitingMovementEnd, "@waiting_for_nmovement"),
                new Index(0x15, kiiWait, "@keyii_wait"),
                new Index(0x16, kiiVariable, "@keyii_variable_OLD"),
                new Index(0x17, kiiFilterArrowkeys, "@keyii_filter_arrowkeys"),
                new Index(0x18, kiiFilterDecision, "@keyii_filter_decision"),
                new Index(0x19, kiiFilterCancel, "@keyii_filter_cancel"),
                new Index(0x1A, kiiFilter1A, "@keyii_filter_1A"),
                new Index(0x1B, kiiFilter1B, "@keyii_filter_1B"),
                new Index(0x1C, kiiFilter1C, "@keyii_filter_1C"),
                new Index(0x1D, kiiFilter1D, "@keyii_filter_1D"),
                new Index(0x1E, kiiFilter1E, "@keyii_filter_1E"),
                new Index(0x1F, waitingSlp1, "@waiting_for_sleep"),
                new Index(0x20, kiiTimeVariable, "@keyii_time_variable"),
                new Index(0x23, kiiFilter23, "@keyii_filter_23"),
                new Index(0x24, kiiFilter24, "@keyii_filter_24"),
                new Index(0x25, kiiFilter25, "@keyii_filter_25"),
                new Index(0x26, kiiFilter26, "@keyii_filter_26"),
                new Index(0x29, kiiTimed, "@keyii_timed"),
                new Index(0x2A, waitingSlp2, "@waiting_for_sleep_alt"),
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
        public ArraySizeR2kInterpretable<EventCommand> listSize = new ArraySizeR2kInterpretable<EventCommand>();
        public ArrayR2kStruct<EventCommand> list = new ArrayR2kStruct<EventCommand>(listSize, new ISupplier<EventCommand>() {
            @Override
            public EventCommand get() {
                return new EventCommand();
            }
        });
        public IntegerR2kStruct index = new IntegerR2kStruct(0);
        public IntegerR2kStruct eventId = new IntegerR2kStruct(0);
        public BooleanR2kStruct actioned = new BooleanR2kStruct(false);
        public ArraySizeR2kInterpretable<ByteR2kStruct> branchesSize = new ArraySizeR2kInterpretable<ByteR2kStruct>();
        public ArrayR2kStruct<ByteR2kStruct> branches = new ArrayR2kStruct<ByteR2kStruct>(branchesSize, new ISupplier<ByteR2kStruct>() {
            @Override
            public ByteR2kStruct get() {
                return new ByteR2kStruct(0);
            }
        });

        @Override
        public Index[] getIndices() {
            return new Index[] {
                    new Index(0x01, listSize),
                    new Index(0x02, list, "@list"),
                    new Index(0x0B, index, "@index"),
                    new Index(0x0C, eventId, "@event_id"),
                    new Index(0x0D, actioned, "@actioned"),
                    new Index(0x15, branchesSize),
                    new Index(0x16, branches, "@branches"),
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
