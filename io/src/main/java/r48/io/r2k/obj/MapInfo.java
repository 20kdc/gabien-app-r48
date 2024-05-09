/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.data.obj.DMCXSupplier;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.TRect;

/**
 * Created on 31/05/17.
 */
public class MapInfo extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@parent_id") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct parent;
    @DMFXOBinding("@indent") @DM2LcfBinding(3) @DMCXInteger(0)
    public IntegerR2kStruct indent;
    @DMFXOBinding("@type") @DM2LcfBinding(4) @DMCXInteger(-1)
    public IntegerR2kStruct type;
    @DMFXOBinding("@OFED_edit_pos_x") @DM2LcfBinding(5) @DMCXInteger(0)
    public IntegerR2kStruct editPosX;
    @DMFXOBinding("@OFED_edit_pos_y") @DM2LcfBinding(6) @DMCXInteger(0)
    public IntegerR2kStruct editPosY;
    @DMFXOBinding("@OFED_expanded") @DM2LcfBinding(7) @DMCXBoolean(false)
    public BooleanR2kStruct expanded;
    @DMFXOBinding("@music_type") @DM2LcfBinding(11) @DMCXInteger(0)
    public IntegerR2kStruct musicType;
    @DMFXOBinding("@music") @DM2LcfBinding(12) @DMCXObject
    public Music music;
    @DMFXOBinding("@background_type") @DM2LcfBinding(21) @DMCXInteger(0)
    public IntegerR2kStruct backgroundType;
    @DMFXOBinding("@background_name") @DM2LcfBinding(22) @DMCXObject
    public StringR2kStruct backgroundName;
    @DMFXOBinding("@teleport_state") @DM2LcfBinding(31) @DMCXInteger(0)
    public IntegerR2kStruct teleportState;
    @DMFXOBinding("@escape_state") @DM2LcfBinding(32) @DMCXInteger(0)
    public IntegerR2kStruct escapeState;
    @DMFXOBinding("@save_state") @DM2LcfBinding(33) @DMCXInteger(0)
    public IntegerR2kStruct saveState;
    @DMFXOBinding("@encounters") @DM2LcfBinding(41) @DMCXSupplier(Encounter.class)
    public DM2SparseArrayA<Encounter> encounters;
    @DMFXOBinding("@encounter_steps") @DM2LcfBinding(44) @DMCXInteger(25)
    public IntegerR2kStruct encounterSteps;
    @DMFXOBinding("@area_rect") @DM2LcfBinding(51) @DMCXObject
    public TRect areaRect;

    public MapInfo(DMContext ctx) {
        super(ctx, "RPG::MapInfo");
    }
}
