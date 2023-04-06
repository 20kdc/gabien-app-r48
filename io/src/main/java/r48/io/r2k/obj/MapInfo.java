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
import r48.io.r2k.struct.TRect;

/**
 * Created on 31/05/17.
 */
public class MapInfo extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DM2FXOBinding("@parent_id") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct parent;
    @DM2FXOBinding("@indent") @DM2LcfBinding(3) @DMCXInteger(0)
    public IntegerR2kStruct indent;
    @DM2FXOBinding("@type") @DM2LcfBinding(4) @DMCXInteger(-1)
    public IntegerR2kStruct type;
    @DM2FXOBinding("@OFED_edit_pos_x") @DM2LcfBinding(5) @DMCXInteger(0)
    public IntegerR2kStruct editPosX;
    @DM2FXOBinding("@OFED_edit_pos_y") @DM2LcfBinding(6) @DMCXInteger(0)
    public IntegerR2kStruct editPosY;
    @DM2FXOBinding("@OFED_expanded") @DM2LcfBinding(7) @DMCXBoolean(false)
    public BooleanR2kStruct expanded;
    @DM2FXOBinding("@music_type") @DM2LcfBinding(11) @DMCXInteger(0)
    public IntegerR2kStruct musicType;
    @DM2FXOBinding("@music") @DM2LcfBinding(12) @DMCXObject
    public Music music;
    @DM2FXOBinding("@background_type") @DM2LcfBinding(21) @DMCXInteger(0)
    public IntegerR2kStruct backgroundType;
    @DM2FXOBinding("@background_name") @DM2LcfBinding(22) @DMCXObject
    public StringR2kStruct backgroundName;
    @DM2FXOBinding("@teleport_state") @DM2LcfBinding(31) @DMCXInteger(0)
    public IntegerR2kStruct teleportState;
    @DM2FXOBinding("@escape_state") @DM2LcfBinding(32) @DMCXInteger(0)
    public IntegerR2kStruct escapeState;
    @DM2FXOBinding("@save_state") @DM2LcfBinding(33) @DMCXInteger(0)
    public IntegerR2kStruct saveState;
    @DM2FXOBinding("@encounters") @DM2LcfBinding(41)
    public DM2SparseArrayA<Encounter> encounters;
    @DM2FXOBinding("@encounter_steps") @DM2LcfBinding(44) @DMCXInteger(25)
    public IntegerR2kStruct encounterSteps;
    @DM2FXOBinding("@area_rect") @DM2LcfBinding(51) @DMCXObject
    public TRect areaRect;

    public MapInfo(DM2Context ctx) {
        super(ctx, "RPG::MapInfo");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@encounters"))
            return encounters = new DM2SparseArrayA<Encounter>(() -> new Encounter(context));
        return super.dm2AddIVar(sym);
    }
}
