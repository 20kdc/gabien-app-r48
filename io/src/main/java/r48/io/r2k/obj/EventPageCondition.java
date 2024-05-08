/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.r2k.chunks.BitfieldR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 02/06/17.
 */
public class EventPageCondition extends DM2R2kObject {
    @DM2FXOBinding("@valid") @DM2LcfBinding(1)
    public BitfieldR2kStruct bitfield;
    @DM2FXOBinding("@switch_a") @DM2LcfBinding(2) @DMCXInteger(1)
    public IntegerR2kStruct switchAId;
    @DM2FXOBinding("@switch_b") @DM2LcfBinding(3) @DMCXInteger(1)
    public IntegerR2kStruct switchBId;
    @DM2FXOBinding("@var_id") @DM2LcfBinding(4) @DMCXInteger(1)
    public IntegerR2kStruct variableId;
    @DM2FXOBinding("@var_value") @DM2LcfBinding(5) @DMCXInteger(0)
    public IntegerR2kStruct variableVal;
    @DM2FXOBinding("@item_id") @DM2LcfBinding(6) @DMCXInteger(1)
    public IntegerR2kStruct itemId;
    @DM2FXOBinding("@actor_id") @DM2LcfBinding(7) @DMCXInteger(1)
    public IntegerR2kStruct actorId;
    @DM2FXOBinding("@timer_1_secs") @DM2LcfBinding(8) @DMCXInteger(0)
    public IntegerR2kStruct timer1Sec;

    @DM2FXOBinding("@timer_2_secs_2k3") @DM2LcfBinding(9) @DMCXInteger(0)
    public IntegerR2kStruct timer2Sec;
    @DM2FXOBinding("@var_compare_op_2k3") @DM2LcfBinding(10) @DMCXInteger(1)
    public IntegerR2kStruct compareOp;

    public EventPageCondition(DMContext ctx) {
        super(ctx, "RPG::EventPage::Condition");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@valid"))
            return bitfield = new BitfieldR2kStruct(dm2Ctx, new String[] {
                    "@switch_a",
                    "@switch_b",
                    "@var_>=_or_2k3op",
                    "@item",
                    "@actor",
                    "@timer_1",
                    "@timer_2_2k3",
            }, 0);
        return super.dm2AddIVar(sym);
    }
}
