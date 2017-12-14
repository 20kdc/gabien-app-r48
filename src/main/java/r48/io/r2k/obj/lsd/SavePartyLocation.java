/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;

/**
 * You know exactly why this inheritance is needed
 * Created on December 13th, 2017
 */
public class SavePartyLocation extends SaveCharacter {
    public BooleanR2kStruct spriteTransparent = new BooleanR2kStruct(false);

    public BooleanR2kStruct boarding = new BooleanR2kStruct(false);
    public BooleanR2kStruct aboard = new BooleanR2kStruct(false);
    public IntegerR2kStruct vehicleType = new IntegerR2kStruct(0);
    public BooleanR2kStruct unboarding = new BooleanR2kStruct(false);
    public IntegerR2kStruct moveSpeedVehicleBackup = new IntegerR2kStruct(4);
    public BooleanR2kStruct menuActivationWaiting = new BooleanR2kStruct(false);
    public IntegerR2kStruct panState = new IntegerR2kStruct(1);
    public IntegerR2kStruct panCurrentX = new IntegerR2kStruct(2304);
    public IntegerR2kStruct panCurrentY = new IntegerR2kStruct(1792);
    public IntegerR2kStruct panFinishX = new IntegerR2kStruct(2304);
    public IntegerR2kStruct panFinishY = new IntegerR2kStruct(1792);
    public IntegerR2kStruct panSpeed = new IntegerR2kStruct(16);
    public IntegerR2kStruct encounterProgress = new IntegerR2kStruct(0);
    public IntegerR2kStruct encounterActivationWaiting = new BooleanR2kStruct(false);
    public IntegerR2kStruct mapSaveCount = new IntegerR2kStruct(0);
    public IntegerR2kStruct dbSaveCount = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return R2kUtil.mergeIndices(super.getIndices(), new Index[] {
                new Index(0x2E, spriteTransparent, "@sprite_transparent"),
                new Index(0x65, boarding, "@vehicle_boarding"),
                new Index(0x66, aboard, "@vehicle_aboard"),
                new Index(0x67, vehicleType, "@vehicle_type"),
                new Index(0x68, unboarding, "@vehicle_leaving"),
                new Index(0x69, moveSpeedVehicleBackup, "@vehicle_movespeed_backup"),
                new Index(0x6C, menuActivationWaiting, "@menu_activation_waiting"),
                new Index(0x6F, panState, "@pan_state"),
                new Index(0x70, panCurrentX, "@pan_x"),
                new Index(0x71, panCurrentY, "@pan_y"),
                new Index(0x72, panFinishX, "@pan_end_x"),
                new Index(0x73, panFinishY, "@pan_end_y"),
                new Index(0x79, panSpeed, "@pan_speed"),
                new Index(0x7C, encounterProgress, "@encounter_steps"),
                new Index(0x7D, encounterActivationWaiting, "@encounter_activation_waiting"),
                new Index(0x83, mapSaveCount, "@map_save_count"),
                new Index(0x84, dbSaveCount, "@db_save_count"),
        });
    }

    @Override
    public RubyIO asRIO() {
        RubyIO root = new RubyIO().setSymlike("RPG::SavePartyLocation", true);
        asRIOISF(root);
        return root;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
