/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DM2FXOBinding;
import r48.io.data.DM2Optional;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.obj.lsd.*;

/**
 * Savefile
 */
public class Save extends DM2R2kObject {
    @DM2FXOBinding("@title") @DM2LcfBinding(100) @DM2LcfObject
    public SaveTitle title;
    @DM2FXOBinding("@system") @DM2LcfBinding(101) @DM2LcfObject
    public SaveSystem system;
    @DM2FXOBinding("@screen") @DM2LcfBinding(102) @DM2LcfObject
    public SaveScreen screen;
    @DM2FXOBinding("@pictures") @DM2LcfBinding(103) @DM2LcfSparseArray(SavePicture.class)
    public DM2SparseArrayH<SavePicture> pictures;
    @DM2FXOBinding("@party_pos") @DM2LcfBinding(104) @DM2LcfObject
    public SavePartyLocation partyPos;
    @DM2FXOBinding("@boat_pos") @DM2LcfBinding(105) @DM2LcfObject
    public SaveVehicleLocation boatPos;
    @DM2FXOBinding("@ship_pos") @DM2LcfBinding(106) @DM2LcfObject
    public SaveVehicleLocation shipPos;
    @DM2FXOBinding("@airship_pos") @DM2LcfBinding(107) @DM2LcfObject
    public SaveVehicleLocation airshipPos;
    @DM2FXOBinding("@actors") @DM2LcfBinding(108) @DM2LcfSparseArray(SaveActor.class)
    public DM2SparseArrayH<SaveActor> actors;
    @DM2FXOBinding("@party") @DM2LcfBinding(109) @DM2LcfObject
    public SaveParty partyItems;
    @DM2FXOBinding("@targets") @DM2LcfBinding(110) @DM2LcfSparseArray(SaveTarget.class)
    public DM2SparseArrayA<SaveTarget> targets;
    @DM2FXOBinding("@map_info") @DM2LcfBinding(111) @DM2LcfObject
    public SaveMapInfo mapInfo;
    @DM2Optional @DM2FXOBinding("@unused_panorama") @DM2LcfBinding(112)
    public DM2Array<ByteR2kStruct> unusedPanorama;
    @DM2FXOBinding("@main_interpreter") @DM2LcfBinding(113) @DM2LcfObject
    public Interpreter mainInterpreter;
    @DM2FXOBinding("@common_events") @DM2LcfBinding(114) @DM2LcfSparseArray(SaveCommonEvent.class)
    public DM2SparseArrayH<SaveCommonEvent> commonEvents;

    // EasyRPG Player version tag.
    @DM2FXOBinding("@easyrpg_player_version_EPL") @DM2LcfBinding(200) @DM2LcfInteger(600) @DM2Optional
    public DM2LcfInteger easyrpgPlayerVersion;

    public Save() {
        super("RPG::Save");
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
            unusedPanorama = newDM2A();
        return super.dm2AddIVar(sym);
    }

    @Override
    public boolean terminatable() {
        return true;
    }

    public static class SaveCommonEvent extends DM2R2kObject {
        @DM2FXOBinding("@i") @DM2LcfBinding(1) @DM2LcfObject
        public Interpreter interp;

        public SaveCommonEvent() {
            super("RPG::SaveCommonEvent");
        }
    }
}
