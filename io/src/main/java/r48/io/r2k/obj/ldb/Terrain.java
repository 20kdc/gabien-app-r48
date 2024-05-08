/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMFXOBinding;
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
    @DMFXOBinding("@name") @DM2LcfBinding(0x01) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@damage") @DM2LcfBinding(0x02) @DMCXInteger(0)
    public IntegerR2kStruct damage;
    @DMFXOBinding("@encounter%_mod") @DM2LcfBinding(0x03) @DMCXInteger(100)
    public IntegerR2kStruct encounterMod;
    @DMFXOBinding("@background_name") @DM2LcfBinding(0x04) @DMCXObject
    public StringR2kStruct backgroundName;
    @DMFXOBinding("@boat_pass") @DM2LcfBinding(0x05) @DMCXBoolean(false)
    public BooleanR2kStruct boatPass;
    @DMFXOBinding("@ship_pass") @DM2LcfBinding(0x06) @DMCXBoolean(false)
    public BooleanR2kStruct shipPass;
    @DMFXOBinding("@airship_pass") @DM2LcfBinding(0x07) @DMCXBoolean(true)
    public BooleanR2kStruct airshipPass;
    @DMFXOBinding("@airship_land") @DM2LcfBinding(0x09) @DMCXBoolean(true)
    public BooleanR2kStruct airshipLand;
    @DMFXOBinding("@bush_depth") @DM2LcfBinding(0x0B) @DMCXInteger(0)
    public IntegerR2kStruct bushDepth;
    @DMFXOBinding("@footstep_sound_2k3") @DM2LcfBinding(0x0F) @DMCXObject
    public Sound footstep;
    @DMFXOBinding("@footstep_for_damage_2k3") @DM2LcfBinding(0x10) @DMCXBoolean(false)
    public BooleanR2kStruct damageSe;
    @DMFXOBinding("@back_as_frame_2k3") @DM2LcfBinding(0x11) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundType;
    @DMFXOBinding("@background_a_name_2k3") @DM2LcfBinding(0x15) @DMCXObject
    public StringR2kStruct backgroundAName;
    @DMFXOBinding("@background_a_scrh_2k3") @DM2LcfBinding(0x16) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundAScrH;

    @DMFXOBinding("@background_a_scrv_2k3") @DM2LcfBinding(0x17) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundAScrV;
    @DMFXOBinding("@background_a_scrh_speed_2k3") @DM2LcfBinding(0x18) @DMCXInteger(0)
    public IntegerR2kStruct backgroundAScrHSpeed;
    @DMFXOBinding("@background_a_scrv_speed_2k3") @DM2LcfBinding(0x19) @DMCXInteger(0)
    public IntegerR2kStruct backgroundAScrVSpeed;

    @DMFXOBinding("@background_b_exists_2k3") @DM2LcfBinding(0x1E) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundB;
    @DMFXOBinding("@background_b_name_2k3") @DM2LcfBinding(0x1F) @DMCXObject
    public StringR2kStruct backgroundBName;
    @DMFXOBinding("@background_b_scrh_2k3") @DM2LcfBinding(0x20) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundBScrH;
    @DMFXOBinding("@background_b_scrv_2k3") @DM2LcfBinding(0x21) @DMCXBoolean(false)
    public BooleanR2kStruct backgroundBScrV;
    @DMFXOBinding("@background_b_scrh_speed_2k3") @DM2LcfBinding(0x22) @DMCXInteger(0)
    public IntegerR2kStruct backgroundBScrHSpeed;
    @DMFXOBinding("@background_b_scrv_speed_2k3") @DM2LcfBinding(0x23) @DMCXInteger(0)
    public IntegerR2kStruct backgroundBScrVSpeed;

    @DMFXOBinding("@special_flags_2k3") @DM2LcfBinding(0x28)
    public BitfieldR2kStruct specialFlags;

    @DMFXOBinding("@special_back_party_2k3") @DM2LcfBinding(0x29) @DMCXInteger(15)
    public IntegerR2kStruct specialBackParty;
    @DMFXOBinding("@special_back_enemies_2k3") @DM2LcfBinding(0x2A) @DMCXInteger(10)
    public IntegerR2kStruct specialBackEnemies;
    @DMFXOBinding("@special_lat_party_2k3") @DM2LcfBinding(0x2B) @DMCXInteger(10)
    public IntegerR2kStruct specialLatParty;
    @DMFXOBinding("@special_lat_enemies_2k3") @DM2LcfBinding(0x2C) @DMCXInteger(5)
    public IntegerR2kStruct specialLatEnemies;

    @DMFXOBinding("@grid_loc_2k3") @DM2LcfBinding(0x2D) @DMCXInteger(0)
    public IntegerR2kStruct gridLoc;
    @DMFXOBinding("@grid_a_2k3") @DM2LcfBinding(0x2E) @DMCXInteger(120)
    public IntegerR2kStruct gridA;
    @DMFXOBinding("@grid_b_2k3") @DM2LcfBinding(0x2F) @DMCXInteger(392)
    public IntegerR2kStruct gridB;
    @DMFXOBinding("@grid_c_2k3") @DM2LcfBinding(0x30) @DMCXInteger(16000)
    public IntegerR2kStruct gridC;

    public Terrain(DMContext ctx) {
        super(ctx, "RPG::Terrain");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@special_flags_2k3"))
            return specialFlags = new BitfieldR2kStruct(context, new String[] {
                    "@back_party",
                    "@back_enemies",
                    "@lat_party",
                    "@lat_enemies",
            }, 0); // Default left unspecified, assumed 0.
        return super.dm2AddIVar(sym);
    }
}
