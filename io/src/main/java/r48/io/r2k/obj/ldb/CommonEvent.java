/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.EventCommand;

/**
 * COPY jun6-2017
 * fixed up later that day along with part of Item and all of Skill.
 */
public class CommonEvent extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@trigger") @DM2LcfBinding(11) @DMCXInteger(0)
    public IntegerR2kStruct trigger;
    @DMFXOBinding("@condition_switch") @DM2LcfBinding(12) @DMCXBoolean(false)
    public BooleanR2kStruct conditionSwitch;
    @DMFXOBinding("@condition_switch_id") @DM2LcfBinding(13) @DMCXInteger(1)
    public IntegerR2kStruct switchId;
    @DMFXOBinding("@list") @DM2LcfSizeBinding(21) @DM2LcfBinding(22)
    public DM2Array<EventCommand> list;

    public CommonEvent(DMContext ctx) {
        super(ctx, "RPG::CommonEvent");
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
        return super.dm2AddIVar(sym);
    }
}
