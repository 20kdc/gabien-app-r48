/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DM2Context;
import r48.io.data.DM2FXOBinding;
import r48.io.data.DMCXObject;
import r48.io.data.DM2Optional;
import r48.io.data.DMCXInteger;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.obj.lsd.*;

/**
 * Savefile
 */
public class Save extends DM2R2kObject {
    @DM2FXOBinding("@title") @DM2LcfBinding(100) @DMCXObject
    public SaveTitle title;
    @DM2FXOBinding("@system") @DM2LcfBinding(101) @DMCXObject
    public SaveSystem system;
    @DM2FXOBinding("@screen") @DM2LcfBinding(102) @DMCXObject
    public SaveScreen screen;
    @DM2FXOBinding("@pictures") @DM2LcfBinding(103) @DM2CXSupplier(SavePicture.class)
    public DM2SparseArrayH<SavePicture> pictures;
    @DM2FXOBinding("@party_pos") @DM2LcfBinding(104) @DMCXObject
    public SavePartyLocation partyPos;
    @DM2FXOBinding("@boat_pos") @DM2LcfBinding(105) @DMCXObject
    public SaveVehicleLocation boatPos;
    @DM2FXOBinding("@ship_pos") @DM2LcfBinding(106) @DMCXObject
    public SaveVehicleLocation shipPos;
    @DM2FXOBinding("@airship_pos") @DM2LcfBinding(107) @DMCXObject
    public SaveVehicleLocation airshipPos;
    @DM2FXOBinding("@actors") @DM2LcfBinding(108) @DM2CXSupplier(SaveActor.class)
    public DM2SparseArrayH<SaveActor> actors;
    @DM2FXOBinding("@party") @DM2LcfBinding(109) @DMCXObject
    public SaveParty partyItems;
    @DM2FXOBinding("@targets") @DM2LcfBinding(110) @DM2CXSupplier(SaveTarget.class)
    public DM2SparseArrayA<SaveTarget> targets;
    @DM2FXOBinding("@map_info") @DM2LcfBinding(111) @DMCXObject
    public SaveMapInfo mapInfo;
    @DM2Optional @DM2FXOBinding("@unused_panorama") @DM2LcfBinding(112)
    public DM2Array<ByteR2kStruct> unusedPanorama;
    @DM2FXOBinding("@main_interpreter") @DM2LcfBinding(113) @DMCXObject
    public Interpreter mainInterpreter;
    @DM2FXOBinding("@common_events") @DM2LcfBinding(114) @DM2CXSupplier(SaveCommonEvent.class)
    public DM2SparseArrayH<SaveCommonEvent> commonEvents;

    // EasyRPG Player version tag.
    @DM2FXOBinding("@easyrpg_player_version_EPL") @DM2LcfBinding(200) @DMCXInteger(600) @DM2Optional
    public IntegerR2kStruct easyrpgPlayerVersion;

    public Save(DM2Context ctx) {
        super(ctx, "RPG::Save");
    }

    private DM2Array<ByteR2kStruct> newDM2A() {
        return new DM2Array<ByteR2kStruct>() {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(0);
            }
        };
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@unused_panorama"))
            return unusedPanorama = newDM2A();
        return super.dm2AddIVar(sym);
    }

    @Override
    public boolean terminatable() {
        return true;
    }

    public static class SaveCommonEvent extends DM2R2kObject {
        @DM2FXOBinding("@i") @DM2LcfBinding(1) @DMCXObject
        public Interpreter interp;

        public SaveCommonEvent(DM2Context ctx) {
            super(ctx, "RPG::SaveCommonEvent");
        }
    }
}
