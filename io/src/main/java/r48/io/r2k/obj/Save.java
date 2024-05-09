/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import java.util.function.Consumer;

import r48.io.data.DMContext;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.obj.lsd.*;

/**
 * Savefile
 */
public class Save extends DM2R2kObject {
    @DMFXOBinding("@title") @DM2LcfBinding(100) @DMCXObject
    public SaveTitle title;
    @DMFXOBinding("@system") @DM2LcfBinding(101) @DMCXObject
    public SaveSystem system;
    @DMFXOBinding("@screen") @DM2LcfBinding(102) @DMCXObject
    public SaveScreen screen;
    @DMFXOBinding("@pictures") @DM2LcfBinding(103) @DMCXSupplier(SavePicture.class)
    public DM2SparseArrayH<SavePicture> pictures;
    @DMFXOBinding("@party_pos") @DM2LcfBinding(104) @DMCXObject
    public SavePartyLocation partyPos;
    @DMFXOBinding("@boat_pos") @DM2LcfBinding(105) @DMCXObject
    public SaveVehicleLocation boatPos;
    @DMFXOBinding("@ship_pos") @DM2LcfBinding(106) @DMCXObject
    public SaveVehicleLocation shipPos;
    @DMFXOBinding("@airship_pos") @DM2LcfBinding(107) @DMCXObject
    public SaveVehicleLocation airshipPos;
    @DMFXOBinding("@actors") @DM2LcfBinding(108) @DMCXSupplier(SaveActor.class)
    public DM2SparseArrayH<SaveActor> actors;
    @DMFXOBinding("@party") @DM2LcfBinding(109) @DMCXObject
    public SaveParty partyItems;
    @DMFXOBinding("@targets") @DM2LcfBinding(110) @DMCXSupplier(SaveTarget.class)
    public DM2SparseArrayA<SaveTarget> targets;
    @DMFXOBinding("@map_info") @DM2LcfBinding(111) @DMCXObject
    public SaveMapInfo mapInfo;
    @DMOptional @DMFXOBinding("@unused_panorama") @DM2LcfBinding(112)
    public DM2Array<ByteR2kStruct> unusedPanorama;
    public static Consumer<Save> unusedPanorama_add = (v) -> v.unusedPanorama = v.newDM2A();
    @DMFXOBinding("@main_interpreter") @DM2LcfBinding(113) @DMCXObject
    public Interpreter mainInterpreter;
    @DMFXOBinding("@common_events") @DM2LcfBinding(114) @DMCXSupplier(SaveCommonEvent.class)
    public DM2SparseArrayH<SaveCommonEvent> commonEvents;

    // EasyRPG Player version tag.
    @DMFXOBinding("@easyrpg_player_version_EPL") @DM2LcfBinding(200) @DMCXInteger(600) @DMOptional
    public IntegerR2kStruct easyrpgPlayerVersion;

    public Save(DMContext ctx) {
        super(ctx, "RPG::Save");
    }

    private DM2Array<ByteR2kStruct> newDM2A() {
        return new DM2Array<ByteR2kStruct>(context) {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(context, 0);
            }
        };
    }

    @Override
    public boolean terminatable() {
        return true;
    }

    public static class SaveCommonEvent extends DM2R2kObject {
        @DMFXOBinding("@i") @DM2LcfBinding(1) @DMCXObject
        public Interpreter interp;

        public SaveCommonEvent(DMContext ctx) {
            super(ctx, "RPG::SaveCommonEvent");
        }
    }
}
