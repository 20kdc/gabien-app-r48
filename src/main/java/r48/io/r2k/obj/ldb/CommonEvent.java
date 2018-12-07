/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
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
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DM2LcfObject
    public StringR2kStruct name;
    @DM2FXOBinding("@trigger") @DM2LcfBinding(11) @DM2LcfInteger(0)
    public IntegerR2kStruct trigger;
    @DM2FXOBinding("@condition_switch") @DM2LcfBinding(12) @DM2LcfBoolean(false)
    public BooleanR2kStruct conditionSwitch;
    @DM2FXOBinding("@condition_switch_id") @DM2LcfBinding(13) @DM2LcfInteger(1)
    public IntegerR2kStruct switchId;
    @DM2FXOBinding("@list") @DM2LcfSizeBinding(21) @DM2LcfBinding(22)
    public DM2Array<EventCommand> list;

    public CommonEvent() {
        super("RPG::CommonEvent");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@list"))
            return list = new DM2Array<EventCommand>() {
                @Override
                public EventCommand newValue() {
                    return new EventCommand();
                }
            };
        return super.dm2AddIVar(sym);
    }
}
