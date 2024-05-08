/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.EventCommand;

/**
 * aka 'SaveEventData'
 */
public class Interpreter extends DM2R2kObject {
    @DMFXOBinding("@stack") @DM2LcfBinding(0x01) @DMCXSupplier(InterpreterStackLevel.class)
    public DM2SparseArrayA<InterpreterStackLevel> commands;
    @DMFXOBinding("@shown_message") @DM2LcfBinding(0x04) @DMCXBoolean(false)
    public BooleanR2kStruct shownMessage;
    @DMFXOBinding("@keyii_wait") @DM2LcfBinding(0x15) @DMCXBoolean(false)
    public BooleanR2kStruct kiiWait;
    @DMFXOBinding("@waiting_for_nmovement") @DM2LcfBinding(0x0D) @DMCXBoolean(false)
    public BooleanR2kStruct waitingMovementEnd;
    // Yes, really. Don't trust this
    @DMFXOBinding("@keyii_variable_OLD") @DM2LcfBinding(0x16) @DMCXObject
    public ByteR2kStruct kiiVariable;
    @DMFXOBinding("@keyii_timed") @DM2LcfBinding(0x29) @DMCXBoolean(false)
    public BooleanR2kStruct kiiTimed;
    @DMFXOBinding("@keyii_time_variable") @DM2LcfBinding(0x20) @DMCXInteger(-1)
    public IntegerR2kStruct kiiTimeVariable;
    @DMFXOBinding("@keyii_filter_arrowkeys") @DM2LcfBinding(0x17) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilterArrowkeys;
    @DMFXOBinding("@keyii_filter_decision") @DM2LcfBinding(0x18) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilterDecision;
    @DMFXOBinding("@waiting_for_sleep") @DM2LcfBinding(0x1F) @DMCXInteger(0)
    public IntegerR2kStruct waitingSlp1;
    @DMFXOBinding("@keyii_filter_23") @DM2LcfBinding(0x23) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter23;
    @DMFXOBinding("@keyii_filter_24") @DM2LcfBinding(0x24) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter24;
    @DMFXOBinding("@keyii_filter_25") @DM2LcfBinding(0x25) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter25;
    @DMFXOBinding("@keyii_filter_26") @DM2LcfBinding(0x26) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter26;
    @DMFXOBinding("@keyii_filter_cancel") @DM2LcfBinding(0x19) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilterCancel;
    @DMFXOBinding("@keyii_filter_1A") @DM2LcfBinding(0x1A) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter1A;
    @DMFXOBinding("@keyii_filter_1B") @DM2LcfBinding(0x1B) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter1B;
    @DMFXOBinding("@keyii_filter_1C") @DM2LcfBinding(0x1C) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter1C;
    @DMFXOBinding("@keyii_filter_1D") @DM2LcfBinding(0x1D) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter1D;
    @DMFXOBinding("@keyii_filter_1E") @DM2LcfBinding(0x1E) @DMCXBoolean(false)
    public BooleanR2kStruct kiiFilter1E;
    @DMFXOBinding("@waiting_for_sleep_alt") @DM2LcfBinding(0x2A) @DMCXInteger(0)
    public IntegerR2kStruct waitingSlp2;

    public Interpreter(DMContext ctx) {
        super(ctx, "RPG::Interpreter");
    }

    public static class InterpreterStackLevel extends DM2R2kObject {
        @DMFXOBinding("@list") @DM2LcfSizeBinding(0x1) @DM2LcfBinding(0x02) @DMCXBoolean(false)
        public DM2Array<EventCommand> list;
        @DMFXOBinding("@index") @DM2LcfBinding(0x0B) @DMCXInteger(0)
        public IntegerR2kStruct index;
        @DMFXOBinding("@event_id") @DM2LcfBinding(0x0C) @DMCXInteger(0)
        public IntegerR2kStruct eventId;
        @DMFXOBinding("@actioned") @DM2LcfBinding(0x0D) @DMCXBoolean(false)
        public BooleanR2kStruct actioned;
        @DMFXOBinding("@branches") @DM2LcfSizeBinding(0x15) @DM2LcfBinding(0x16)
        public DM2Array<ByteR2kStruct> branches;

        public InterpreterStackLevel(DMContext ctx) {
            super(ctx, "RPG::InterpreterStackLevel");
        }

        @Override
        protected IRIO dm2AddIVar(String sym) {
            if (sym.equals("@list"))
                return list = new DM2Array<EventCommand>(context) {
                    @Override
                    public EventCommand newValue() {
                        return new EventCommand(context);
                    }
                };
            if (sym.equals("@branches"))
                return branches = new DM2Array<ByteR2kStruct>(context) {
                    @Override
                    public ByteR2kStruct newValue() {
                        return new ByteR2kStruct(context);
                    }
                };
            return super.dm2AddIVar(sym);
        }
    }
}
