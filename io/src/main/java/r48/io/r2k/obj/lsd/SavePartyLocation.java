/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;

/**
 * You know exactly why this inheritance is needed
 * Created on December 13th, 2017
 */
public class SavePartyLocation extends SaveCharacter {
    @DM2FXOBinding("@sprite_transparent") @DM2LcfBinding(46) @DMCXBoolean(false)
    public BooleanR2kStruct spriteTransparent;
    @DM2FXOBinding("@vehicle_boarding") @DM2LcfBinding(101) @DMCXBoolean(false)
    public BooleanR2kStruct boarding;
    @DM2FXOBinding("@vehicle_aboard") @DM2LcfBinding(102) @DMCXBoolean(false)
    public BooleanR2kStruct aboard;
    @DM2FXOBinding("@vehicle_type") @DM2LcfBinding(103) @DMCXInteger(0)
    public IntegerR2kStruct vehicleType;
    @DM2FXOBinding("@vehicle_leaving") @DM2LcfBinding(104) @DMCXBoolean(false)
    public BooleanR2kStruct unboarding;
    @DM2FXOBinding("@vehicle_movespeed_backup") @DM2LcfBinding(105) @DMCXInteger(4)
    public IntegerR2kStruct moveSpeedVehicleBackup;
    @DM2FXOBinding("@menu_activation_waiting") @DM2LcfBinding(108) @DMCXBoolean(false)
    public BooleanR2kStruct menuActivationWaiting;
    @DM2FXOBinding("@pan_state") @DM2LcfBinding(111) @DMCXInteger(1)
    public IntegerR2kStruct panState;
    @DM2FXOBinding("@pan_x") @DM2LcfBinding(112) @DMCXInteger(2304)
    public IntegerR2kStruct panCurrentX;
    @DM2FXOBinding("@pan_y") @DM2LcfBinding(113) @DMCXInteger(1792)
    public IntegerR2kStruct panCurrentY;
    @DM2FXOBinding("@pan_end_x") @DM2LcfBinding(114) @DMCXInteger(2304)
    public IntegerR2kStruct panFinishX;
    @DM2FXOBinding("@pan_end_y") @DM2LcfBinding(115) @DMCXInteger(1792)
    public IntegerR2kStruct panFinishY;
    @DM2FXOBinding("@pan_speed") @DM2LcfBinding(121) @DMCXInteger(16)
    public IntegerR2kStruct panSpeed;
    @DM2FXOBinding("@encounter_steps") @DM2LcfBinding(124) @DMCXInteger(0)
    public IntegerR2kStruct encounterProgress;
    @DM2FXOBinding("@encounter_activation_waiting") @DM2LcfBinding(125) @DMCXBoolean(false)
    public BooleanR2kStruct encounterActivationWaiting;
    @DM2FXOBinding("@map_save_count") @DM2LcfBinding(131) @DMCXInteger(0)
    public IntegerR2kStruct mapSaveCount;
    @DM2FXOBinding("@db_save_count") @DM2LcfBinding(132) @DMCXInteger(0)
    public IntegerR2kStruct dbSaveCount;

    public SavePartyLocation(DM2Context ctx) {
        super(ctx, "RPG::SavePartyLocation");
    }
}
