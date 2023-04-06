/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.IRIO;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BitfieldR2kStruct;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.obj.Sound;

/**
 * COPY jun6-2017
 */
public class Terrain extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(0x01) @DMCXObject
    public StringR2kStruct name;
    @DM2FXOBinding("@damage") @DM2LcfBinding(0x02) @DMCXInteger(0)
    public IntegerR2kStruct damage;
    @DM2FXOBinding("@encounter%_mod") @DM2LcfBinding(0x03) @DMCXInteger(100)
    public IntegerR2kStruct encounterMod;
    @DM2FXOBinding("@background_name") @DM2LcfBinding(0x04) @DMCXObject
    public StringR2kStruct backgroundName;
    @DM2FXOBinding("@boat_pass") @DM2LcfBinding(0x05) @DMCXBoolean(false)
    public BooleanR2kStruct boatPass;
    @DM2FXOBinding("@ship_pass") @DM2LcfBinding(0x06) @DMCXBoolean(false)
    public BooleanR2kStruct shipPass;
    @DM2FXOBinding("@airship_pass") @DM2LcfBinding(0x07) @DMCXBoolean(true)
    public BooleanR2kStruct airshipPass;
    @DM2FXOBinding("@airship_land") @DM2LcfBinding(0x09) @DMCXBoolean(true)
    public BooleanR2kStruct airshipLand;
    @DM2FXOBinding("@bush_depth") @DM2LcfBinding(0x0B) @DMCXInteger(0)
    public IntegerR2kStruct bushDepth;
    @DM2FXOBinding("@footstep_sound_2k3") @DM2LcfBinding(0x0F) @DMCXObject
    public Sound footstep;
    @DM2FXOBinding("@footstep_for_damage_2k3") @DM2LcfBinding(0x10) @DMCXBoolean(false)
    public BooleanR2kStruct damageSe;
    @DM2FXOBinding("@back_as_frame_2k3") @DM2LcfBinding(0x11) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundType;
    @DM2FXOBinding("@background_a_name_2k3") @DM2LcfBinding(0x15) @DMCXObject
    public StringR2kStruct backgroundAName;
    @DM2FXOBinding("@background_a_scrh_2k3") @DM2LcfBinding(0x16) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundAScrH;

    @DM2FXOBinding("@background_a_scrv_2k3") @DM2LcfBinding(0x17) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundAScrV;
    @DM2FXOBinding("@background_a_scrh_speed_2k3") @DM2LcfBinding(0x18) @DMCXInteger(0)
    public IntegerR2kStruct backgroundAScrHSpeed;
    @DM2FXOBinding("@background_a_scrv_speed_2k3") @DM2LcfBinding(0x19) @DMCXInteger(0)
    public IntegerR2kStruct backgroundAScrVSpeed;

    @DM2FXOBinding("@background_b_exists_2k3") @DM2LcfBinding(0x1E) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundB;
    @DM2FXOBinding("@background_b_name_2k3") @DM2LcfBinding(0x1F) @DMCXObject
    public StringR2kStruct backgroundBName;
    @DM2FXOBinding("@background_b_scrh_2k3") @DM2LcfBinding(0x20) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundBScrH;
    @DM2FXOBinding("@background_b_scrv_2k3") @DM2LcfBinding(0x21) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundBScrV;
    @DM2FXOBinding("@background_b_scrh_speed_2k3") @DM2LcfBinding(0x22) @DMCXInteger(0)
    public IntegerR2kStruct backgroundBScrHSpeed;
    @DM2FXOBinding("@background_b_scrv_speed_2k3") @DM2LcfBinding(0x23) @DMCXInteger(0)
    public IntegerR2kStruct backgroundBScrVSpeed;

    @DM2FXOBinding("@special_flags_2k3") @DM2LcfBinding(0x28)
    public BitfieldR2kStruct specialFlags;

    @DM2FXOBinding("@special_back_party_2k3") @DM2LcfBinding(0x29) @DMCXInteger(15)
    public IntegerR2kStruct specialBackParty;
    @DM2FXOBinding("@special_back_enemies_2k3") @DM2LcfBinding(0x2A) @DMCXInteger(10)
    public IntegerR2kStruct specialBackEnemies;
    @DM2FXOBinding("@special_lat_party_2k3") @DM2LcfBinding(0x2B) @DMCXInteger(10)
    public IntegerR2kStruct specialLatParty;
    @DM2FXOBinding("@special_lat_enemies_2k3") @DM2LcfBinding(0x2C) @DMCXInteger(5)
    public IntegerR2kStruct specialLatEnemies;

    @DM2FXOBinding("@grid_loc_2k3") @DM2LcfBinding(0x2D) @DMCXInteger(0)
    public IntegerR2kStruct gridLoc;
    @DM2FXOBinding("@grid_a_2k3") @DM2LcfBinding(0x2E) @DMCXInteger(120)
    public IntegerR2kStruct gridA;
    @DM2FXOBinding("@grid_b_2k3") @DM2LcfBinding(0x2F) @DMCXInteger(392)
    public IntegerR2kStruct gridB;
    @DM2FXOBinding("@grid_c_2k3") @DM2LcfBinding(0x30) @DMCXInteger(16000)
    public IntegerR2kStruct gridC;

    public Terrain(DM2Context ctx) {
        super(ctx, "RPG::Terrain");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@special_flags_2k3"))
            return specialFlags = new BitfieldR2kStruct(new String[] {
                    "@back_party",
                    "@back_enemies",
                    "@lat_party",
                    "@lat_enemies",
            }, 0); // Default left unspecified, assumed 0.
        return super.dm2AddIVar(sym);
    }
}
