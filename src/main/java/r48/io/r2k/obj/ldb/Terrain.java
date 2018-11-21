/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
import r48.io.r2k.obj.Sound;

/**
 * COPY jun6-2017
 */
public class Terrain extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct damage = new IntegerR2kStruct(0);
    public IntegerR2kStruct encounterMod = new IntegerR2kStruct(100);
    public StringR2kStruct backgroundName = new StringR2kStruct();
    public BooleanR2kStruct boatPass = new BooleanR2kStruct(false);
    public BooleanR2kStruct shipPass = new BooleanR2kStruct(false);
    public BooleanR2kStruct airshipPass = new BooleanR2kStruct(true);
    public BooleanR2kStruct airshipLand = new BooleanR2kStruct(true);
    public IntegerR2kStruct bushDepth = new IntegerR2kStruct(0);
    public Sound footstep = new Sound();
    public BooleanR2kStruct damageSe = new BooleanR2kStruct(false);
    public BooleanR2kStruct backgroundType = new BooleanR2kStruct(false);

    public StringR2kStruct backgroundAName = new StringR2kStruct();
    public BooleanR2kStruct backgroundAScrH = new BooleanR2kStruct(false);
    public BooleanR2kStruct backgroundAScrV = new BooleanR2kStruct(false);
    public IntegerR2kStruct backgroundAScrHSpeed = new IntegerR2kStruct(0);
    public IntegerR2kStruct backgroundAScrVSpeed = new IntegerR2kStruct(0);

    public BooleanR2kStruct backgroundB = new BooleanR2kStruct(false);
    public StringR2kStruct backgroundBName = new StringR2kStruct();
    public BooleanR2kStruct backgroundBScrH = new BooleanR2kStruct(false);
    public BooleanR2kStruct backgroundBScrV = new BooleanR2kStruct(false);
    public IntegerR2kStruct backgroundBScrHSpeed = new IntegerR2kStruct(0);
    public IntegerR2kStruct backgroundBScrVSpeed = new IntegerR2kStruct(0);

    public BitfieldR2kStruct specialFlags = new BitfieldR2kStruct(new String[] {
            "@back_party",
            "@back_enemies",
            "@lat_party",
            "@lat_enemies",
    }, 0); // Default left unspecified, assumed 0.

    public IntegerR2kStruct specialBackParty = new IntegerR2kStruct(15);
    public IntegerR2kStruct specialBackEnemies = new IntegerR2kStruct(10);
    public IntegerR2kStruct specialLatParty = new IntegerR2kStruct(10);
    public IntegerR2kStruct specialLatEnemies = new IntegerR2kStruct(5);

    public IntegerR2kStruct gridLoc = new IntegerR2kStruct(0);
    public IntegerR2kStruct gridA = new IntegerR2kStruct(120);
    public IntegerR2kStruct gridB = new IntegerR2kStruct(392);
    public IntegerR2kStruct gridC = new IntegerR2kStruct(16000);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, damage, "@damage"),
                new Index(0x03, encounterMod, "@encounter%_mod"),
                new Index(0x04, backgroundName, "@background_name"),
                new Index(0x05, boatPass, "@boat_pass"),
                new Index(0x06, shipPass, "@ship_pass"),
                new Index(0x07, airshipPass, "@airship_pass"),
                new Index(0x09, airshipLand, "@airship_land"),
                new Index(0x0B, bushDepth, "@bush_depth"),
                new Index(0x0F, footstep, "@footstep_sound_2k3"),
                new Index(0x10, damageSe, "@footstep_for_damage_2k3"),
                new Index(0x11, backgroundType, "@back_as_frame_2k3"),

                new Index(0x15, backgroundAName, "@background_a_name_2k3"),
                new Index(0x16, backgroundAScrH, "@background_a_scrh_2k3"),
                new Index(0x17, backgroundAScrV, "@background_a_scrv_2k3"),
                new Index(0x18, backgroundAScrHSpeed, "@background_a_scrh_speed_2k3"),
                new Index(0x19, backgroundAScrVSpeed, "@background_a_scrv_speed_2k3"),

                new Index(0x1E, backgroundB, "@background_b_exists_2k3"),
                new Index(0x1F, backgroundBName, "@background_b_name_2k3"),
                new Index(0x20, backgroundBScrH, "@background_b_scrh_2k3"),
                new Index(0x21, backgroundBScrV, "@background_b_scrv_2k3"),
                new Index(0x22, backgroundBScrHSpeed, "@background_b_scrh_speed_2k3"),
                new Index(0x23, backgroundBScrVSpeed, "@background_b_scrv_speed_2k3"),

                new Index(0x28, specialFlags, "@special_flags_2k3"),
                new Index(0x29, specialBackParty, "@special_back_party_2k3"),
                new Index(0x2A, specialBackEnemies, "@special_back_enemies_2k3"),
                new Index(0x2B, specialLatParty, "@special_lat_party_2k3"),
                new Index(0x2C, specialLatEnemies, "@special_lat_enemies_2k3"),
                new Index(0x2D, gridLoc, "@grid_loc_2k3"),
                new Index(0x2E, gridA, "@grid_a_2k3"),
                new Index(0x2F, gridB, "@grid_b_2k3"),
                new Index(0x30, gridC, "@grid_c_2k3"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Terrain", true);
        asRIOISF(rio);
        return rio;
    }

}
