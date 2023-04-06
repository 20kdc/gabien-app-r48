/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.io.data.IRIO;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.EventCommand;

/**
 * Created on 31/05/17.
 */
public class EventPage extends DM2R2kObject {
    // NOTE TO SELF. YOU HAVE CHECKED THIS AT LEAST THRICE ALREADY. STOP CHECKING THE DEF.VALS.
    @DM2FXOBinding("@condition") @DM2LcfBinding(2) @DMCXObject
    public EventPageCondition condition;
    @DM2FXOBinding("@character_name") @DM2LcfBinding(21) @DMCXObject
    public StringR2kStruct graphicCName;
    @DM2FXOBinding("@character_index") @DM2LcfBinding(22) @DMCXInteger(0)
    public IntegerR2kStruct graphicCIndex;
    @DM2FXOBinding("@character_direction") @DM2LcfBinding(23) @DMCXInteger(2)
    public IntegerR2kStruct graphicCDirection;
    @DM2FXOBinding("@character_pattern") @DM2LcfBinding(24) @DMCXInteger(1)
    public IntegerR2kStruct graphicCPattern;
    @DM2FXOBinding("@character_blend_mode") @DM2LcfBinding(25) @DMCXBoolean(false)
    public BooleanR2kStruct graphicCBlendMode;

    @DM2FXOBinding("@move_type") @DM2LcfBinding(31) @DMCXInteger(1)
    public IntegerR2kStruct moveType;
    @DM2FXOBinding("@move_freq") @DM2LcfBinding(32) @DMCXInteger(3)
    public IntegerR2kStruct moveFreq;
    @DM2FXOBinding("@trigger") @DM2LcfBinding(33) @DMCXInteger(0)
    public IntegerR2kStruct trigger;
    @DM2FXOBinding("@layer") @DM2LcfBinding(34) @DMCXInteger(0)
    public IntegerR2kStruct layer;
    @DM2FXOBinding("@block_other_events") @DM2LcfBinding(35) @DMCXBoolean(false)
    public BooleanR2kStruct blocking;
    @DM2FXOBinding("@anim_type") @DM2LcfBinding(36) @DMCXInteger(0)
    public IntegerR2kStruct animType;
    @DM2FXOBinding("@move_speed") @DM2LcfBinding(37) @DMCXInteger(3)
    public IntegerR2kStruct moveSpeed;
    @DM2FXOBinding("@move_route") @DM2LcfBinding(41) @DMCXObject
    public MoveRoute moveRoute;

    @DM2FXOBinding("@list") @DM2LcfSizeBinding(51) @DM2LcfBinding(52)
    public DM2Array<EventCommand> list;

    public EventPage(DM2Context ctx) {
        super(ctx, "RPG::EventPage");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@list"))
            return list = new DM2Array<EventCommand>() {
                @Override
                public EventCommand newValue() {
                    return new EventCommand(context);
                }
            };
        return super.dm2AddIVar(sym);
    }
}
