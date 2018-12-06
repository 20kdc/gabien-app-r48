/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.BitfieldR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2LcfInteger;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 02/06/17.
 */
public class EventPageCondition extends DM2R2kObject {
    @DM2FXOBinding(optional = false, iVar = "@valid") @DM2LcfBinding(index = 1)
    public BitfieldR2kStruct bitfield;
    @DM2FXOBinding(optional = false, iVar = "@switch_a") @DM2LcfBinding(index = 2) @DM2LcfInteger(1)
    public IntegerR2kStruct switchAId;
    @DM2FXOBinding(optional = false, iVar = "@switch_b") @DM2LcfBinding(index = 3) @DM2LcfInteger(1)
    public IntegerR2kStruct switchBId;
    @DM2FXOBinding(optional = false, iVar = "@var_id") @DM2LcfBinding(index = 4) @DM2LcfInteger(1)
    public IntegerR2kStruct variableId;
    @DM2FXOBinding(optional = false, iVar = "@var_value") @DM2LcfBinding(index = 5) @DM2LcfInteger(0)
    public IntegerR2kStruct variableVal;
    @DM2FXOBinding(optional = false, iVar = "@item_id") @DM2LcfBinding(index = 6) @DM2LcfInteger(1)
    public IntegerR2kStruct itemId;
    @DM2FXOBinding(optional = false, iVar = "@actor_id") @DM2LcfBinding(index = 7) @DM2LcfInteger(1)
    public IntegerR2kStruct actorId;
    @DM2FXOBinding(optional = false, iVar = "@timer_1_secs") @DM2LcfBinding(index = 8) @DM2LcfInteger(0)
    public IntegerR2kStruct timer1Sec;

    @DM2FXOBinding(optional = true, iVar = "@timer_2_secs_2k3") @DM2LcfBinding(index = 9) @DM2LcfInteger(0)
    public IntegerR2kStruct timer2Sec;
    @DM2FXOBinding(optional = true, iVar = "@var_compare_op_2k3") @DM2LcfBinding(index = 10) @DM2LcfInteger(0)
    public IntegerR2kStruct compareOp;

    public EventPageCondition() {
        super("RPG::EventPage::Condition");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@valid"))
            return bitfield = new BitfieldR2kStruct(new String[] {
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
