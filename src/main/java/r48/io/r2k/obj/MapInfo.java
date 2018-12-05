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
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2LcfInteger;
import r48.io.r2k.dm2chk.DM2R2kObject;
import r48.io.r2k.dm2chk.DM2SparseArrayA;
import r48.io.r2k.struct.TRect;

/**
 * Created on 31/05/17.
 */
public class MapInfo extends DM2R2kObject {
    @DM2FXOBinding(optional = false, iVar = "@name")
    @DM2LcfBinding(index = 1)
    public StringR2kStruct name;
    @DM2FXOBinding(optional = false, iVar = "@parent_id")
    @DM2LcfBinding(index = 2)
    @DM2LcfInteger(0)
    public IntegerR2kStruct parent;
    @DM2FXOBinding(optional = false, iVar = "@indent")
    @DM2LcfBinding(index = 3)
    @DM2LcfInteger(0)
    public IntegerR2kStruct indent;
    @DM2FXOBinding(optional = false, iVar = "@type")
    @DM2LcfBinding(index = 4)
    @DM2LcfInteger(-1)
    public IntegerR2kStruct type;
    @DM2FXOBinding(optional = false, iVar = "@OFED_edit_pos_x")
    @DM2LcfBinding(index = 5)
    @DM2LcfInteger(0)
    public IntegerR2kStruct editPosX;
    @DM2FXOBinding(optional = false, iVar = "@OFED_edit_pos_y")
    @DM2LcfBinding(index = 6)
    @DM2LcfInteger(0)
    public IntegerR2kStruct editPosY;
    @DM2FXOBinding(optional = false, iVar = "@OFED_expanded")
    @DM2LcfBinding(index = 7)
    public BooleanR2kStruct expanded;
    @DM2FXOBinding(optional = false, iVar = "@music_type")
    @DM2LcfBinding(index = 11)
    @DM2LcfInteger(0)
    public IntegerR2kStruct musicType;
    @DM2FXOBinding(optional = false, iVar = "@music")
    @DM2LcfBinding(index = 12)
    public Music music;
    @DM2FXOBinding(optional = false, iVar = "@background_type")
    @DM2LcfBinding(index = 21)
    @DM2LcfInteger(0)
    public IntegerR2kStruct backgroundType;
    @DM2FXOBinding(optional = false, iVar = "@background_name")
    @DM2LcfBinding(index = 22)
    public StringR2kStruct backgroundName;
    @DM2FXOBinding(optional = false, iVar = "@teleport_state")
    @DM2LcfBinding(index = 31)
    @DM2LcfInteger(0)
    public IntegerR2kStruct teleportState;
    @DM2FXOBinding(optional = false, iVar = "@escape_state")
    @DM2LcfBinding(index = 32)
    @DM2LcfInteger(0)
    public IntegerR2kStruct escapeState;
    @DM2FXOBinding(optional = false, iVar = "@save_state")
    @DM2LcfBinding(index = 33)
    @DM2LcfInteger(0)
    public IntegerR2kStruct saveState;
    @DM2FXOBinding(optional = false, iVar = "@encounters")
    @DM2LcfBinding(index = 41)
    public DM2SparseArrayA<Encounter> encounters;
    @DM2FXOBinding(optional = false, iVar = "@encounter_steps")
    @DM2LcfBinding(index = 44)
    @DM2LcfInteger(25)
    public IntegerR2kStruct encounterSteps;
    @DM2FXOBinding(optional = false, iVar = "@area_rect")
    @DM2LcfBinding(index = 51)
    public TRect areaRect;

    public MapInfo() {
        super("RPG::MapInfo");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@name"))
            return name = new StringR2kStruct();
        if (sym.equals("@OFED_expanded"))
            return expanded = new BooleanR2kStruct(false);
        if (sym.equals("@music"))
            return music = new Music();
        if (sym.equals("@background_name"))
            return backgroundName = new StringR2kStruct();
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
