/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.TRect;

/**
 * Created on 31/05/17.
 */
public class MapInfo extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DM2LcfString()
    public StringR2kStruct name;
    @DM2FXOBinding("@parent_id") @DM2LcfBinding(2) @DM2LcfInteger(0)
    public IntegerR2kStruct parent;
    @DM2FXOBinding("@indent") @DM2LcfBinding(3) @DM2LcfInteger(0)
    public IntegerR2kStruct indent;
    @DM2FXOBinding("@type") @DM2LcfBinding(4) @DM2LcfInteger(-1)
    public IntegerR2kStruct type;
    @DM2FXOBinding("@OFED_edit_pos_x") @DM2LcfBinding(5) @DM2LcfInteger(0)
    public IntegerR2kStruct editPosX;
    @DM2FXOBinding("@OFED_edit_pos_y") @DM2LcfBinding(6) @DM2LcfInteger(0)
    public IntegerR2kStruct editPosY;
    @DM2FXOBinding("@OFED_expanded") @DM2LcfBinding(7) @DM2LcfBoolean(false)
    public BooleanR2kStruct expanded;
    @DM2FXOBinding("@music_type") @DM2LcfBinding(11) @DM2LcfInteger(0)
    public IntegerR2kStruct musicType;
    @DM2FXOBinding("@music") @DM2LcfBinding(12)
    public Music music;
    @DM2FXOBinding("@background_type") @DM2LcfBinding(21) @DM2LcfInteger(0)
    public IntegerR2kStruct backgroundType;
    @DM2FXOBinding("@background_name") @DM2LcfBinding(22) @DM2LcfString()
    public StringR2kStruct backgroundName;
    @DM2FXOBinding("@teleport_state") @DM2LcfBinding(31) @DM2LcfInteger(0)
    public IntegerR2kStruct teleportState;
    @DM2FXOBinding("@escape_state") @DM2LcfBinding(32) @DM2LcfInteger(0)
    public IntegerR2kStruct escapeState;
    @DM2FXOBinding("@save_state") @DM2LcfBinding(33) @DM2LcfInteger(0)
    public IntegerR2kStruct saveState;
    @DM2FXOBinding("@encounters") @DM2LcfBinding(41)
    public DM2SparseArrayA<Encounter> encounters;
    @DM2FXOBinding("@encounter_steps") @DM2LcfBinding(44) @DM2LcfInteger(25)
    public IntegerR2kStruct encounterSteps;
    @DM2FXOBinding("@area_rect") @DM2LcfBinding(51)
    public TRect areaRect;

    public MapInfo() {
        super("RPG::MapInfo");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@music"))
            return music = new Music();
        if (sym.equals("@encounters"))
            return encounters = new DM2SparseArrayA<Encounter>(new ISupplier<Encounter>() {
                @Override
                public Encounter get() {
                    return new Encounter();
                }
            });
        if (sym.equals("@area_rect"))
            return areaRect = new TRect();
        return super.dm2AddIVar(sym);
    }
}
