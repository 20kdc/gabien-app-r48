/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
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
    @DM2FXOBinding(optional = false, iVar = "@condition") @DM2LcfBinding(index = 2)
    public EventPageCondition condition;
    @DM2FXOBinding(optional = false, iVar = "@character_name") @DM2LcfBinding(index = 21) @DM2LcfString()
    public StringR2kStruct graphicCName;
    @DM2FXOBinding(optional = false, iVar = "@character_index") @DM2LcfBinding(index = 22) @DM2LcfInteger(0)
    public IntegerR2kStruct graphicCIndex;
    @DM2FXOBinding(optional = false, iVar = "@character_direction") @DM2LcfBinding(index = 23) @DM2LcfInteger(2)
    public IntegerR2kStruct graphicCDirection;
    @DM2FXOBinding(optional = false, iVar = "@character_pattern") @DM2LcfBinding(index = 24) @DM2LcfInteger(1)
    public IntegerR2kStruct graphicCPattern;
    @DM2FXOBinding(optional = false, iVar = "@character_blend_mode") @DM2LcfBinding(index = 25) @DM2LcfBoolean(false)
    public BooleanR2kStruct graphicCBlendMode;

    @DM2FXOBinding(optional = false, iVar = "@move_type") @DM2LcfBinding(index = 31) @DM2LcfInteger(1)
    public IntegerR2kStruct moveType;
    @DM2FXOBinding(optional = false, iVar = "@move_freq") @DM2LcfBinding(index = 32) @DM2LcfInteger(3)
    public IntegerR2kStruct moveFreq;
    @DM2FXOBinding(optional = false, iVar = "@trigger") @DM2LcfBinding(index = 33) @DM2LcfInteger(0)
    public IntegerR2kStruct trigger;
    @DM2FXOBinding(optional = false, iVar = "@layer") @DM2LcfBinding(index = 34) @DM2LcfInteger(0)
    public IntegerR2kStruct layer;
    @DM2FXOBinding(optional = false, iVar = "@block_other_events") @DM2LcfBinding(index = 35) @DM2LcfBoolean(false)
    public BooleanR2kStruct blocking;
    @DM2FXOBinding(optional = false, iVar = "@anim_type") @DM2LcfBinding(index = 36) @DM2LcfInteger(0)
    public IntegerR2kStruct animType;
    @DM2FXOBinding(optional = false, iVar = "@move_speed") @DM2LcfBinding(index = 37) @DM2LcfInteger(3)
    public IntegerR2kStruct moveSpeed;
    @DM2FXOBinding(optional = false, iVar = "@move_route") @DM2LcfBinding(index = 41)
    public MoveRoute moveRoute;

    @DM2FXOBinding(optional = false, iVar = "@list") @DM2LcfSizeBinding(51) @DM2LcfBinding(index = 52)
    public DM2Array<EventCommand> list;

    public EventPage() {
        super("RPG::EventPage");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@condition"))
            return condition = new EventPageCondition();
        if (sym.equals("@move_route"))
            return moveRoute = new MoveRoute();
        if (sym.equals("@list"))
            return list = new DM2Array<EventCommand>(0, false, true) {
                @Override
                public EventCommand newValue() {
                    return new EventCommand();
                }
            };
        return super.dm2AddIVar(sym);
    }
}
