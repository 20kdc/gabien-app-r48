/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BitfieldR2kStruct;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;

/**
 * Created on 02/06/17.
 */
public class EventPageCondition extends R2kObject {
    public BitfieldR2kStruct bitfield = new BitfieldR2kStruct(new String[] {
            "@switch_a",
            "@switch_b",
            "@var_>=_or_2k3op",
            "@item",
            "@actor",
            "@timer_1",
            "@timer_2",
    });
    public IntegerR2kStruct switchAId = new IntegerR2kStruct(1);
    public IntegerR2kStruct switchBId = new IntegerR2kStruct(1);
    public IntegerR2kStruct variableId = new IntegerR2kStruct(1);
    public IntegerR2kStruct variableVal = new IntegerR2kStruct(0);
    public IntegerR2kStruct itemId = new IntegerR2kStruct(1);
    public IntegerR2kStruct actorId = new IntegerR2kStruct(1);
    public IntegerR2kStruct timer1Sec = new IntegerR2kStruct(0);
    public IntegerR2kStruct timer2Sec = new IntegerR2kStruct(0);
    public IntegerR2kStruct compareOp = new IntegerR2kStruct(0);
    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, bitfield, "@valid"),
                new Index(0x02, switchAId, "@switch_a"),
                new Index(0x03, switchBId, "@switch_b"),
                new Index(0x04, variableId, "@var_id"),
                new Index(0x05, variableVal, "@var_value"),
                new Index(0x06, itemId, "@item_id"),
                new Index(0x07, actorId, "@actor_id"),
                new Index(0x08, timer1Sec, "@timer_1_secs"),
                new Index(0x09, timer2Sec, "@timer_2_secs_2k3"),
                new Index(0x0A, compareOp, "@var_compare_op_2k3"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO b = new RubyIO().setSymlike("RPG::EventPage::Condition", true);
        asRIOISF(b);
        return b;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}