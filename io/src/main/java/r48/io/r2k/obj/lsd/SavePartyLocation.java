/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
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
    @DMFXOBinding("@sprite_transparent") @DM2LcfBinding(46) @DMCXBoolean(false)
    public BooleanR2kStruct spriteTransparent;
    @DMFXOBinding("@vehicle_boarding") @DM2LcfBinding(101) @DMCXBoolean(false)
    public BooleanR2kStruct boarding;
    @DMFXOBinding("@vehicle_aboard") @DM2LcfBinding(102) @DMCXBoolean(false)
    public BooleanR2kStruct aboard;
    @DMFXOBinding("@vehicle_type") @DM2LcfBinding(103) @DMCXInteger(0)
    public IntegerR2kStruct vehicleType;
    @DMFXOBinding("@vehicle_leaving") @DM2LcfBinding(104) @DMCXBoolean(false)
    public BooleanR2kStruct unboarding;
    @DMFXOBinding("@vehicle_movespeed_backup") @DM2LcfBinding(105) @DMCXInteger(4)
    public IntegerR2kStruct moveSpeedVehicleBackup;
    @DMFXOBinding("@menu_activation_waiting") @DM2LcfBinding(108) @DMCXBoolean(false)
    public BooleanR2kStruct menuActivationWaiting;
    @DMFXOBinding("@pan_state") @DM2LcfBinding(111) @DMCXInteger(1)
    public IntegerR2kStruct panState;
    @DMFXOBinding("@pan_x") @DM2LcfBinding(112) @DMCXInteger(2304)
    public IntegerR2kStruct panCurrentX;
    @DMFXOBinding("@pan_y") @DM2LcfBinding(113) @DMCXInteger(1792)
    public IntegerR2kStruct panCurrentY;
    @DMFXOBinding("@pan_end_x") @DM2LcfBinding(114) @DMCXInteger(2304)
    public IntegerR2kStruct panFinishX;
    @DMFXOBinding("@pan_end_y") @DM2LcfBinding(115) @DMCXInteger(1792)
    public IntegerR2kStruct panFinishY;
    @DMFXOBinding("@pan_speed") @DM2LcfBinding(121) @DMCXInteger(16)
    public IntegerR2kStruct panSpeed;
    @DMFXOBinding("@encounter_steps") @DM2LcfBinding(124) @DMCXInteger(0)
    public IntegerR2kStruct encounterProgress;
    @DMFXOBinding("@encounter_activation_waiting") @DM2LcfBinding(125) @DMCXBoolean(false)
    public BooleanR2kStruct encounterActivationWaiting;
    @DMFXOBinding("@map_save_count") @DM2LcfBinding(131) @DMCXInteger(0)
    public IntegerR2kStruct mapSaveCount;
    @DMFXOBinding("@db_save_count") @DM2LcfBinding(132) @DMCXInteger(0)
    public IntegerR2kStruct dbSaveCount;

    public SavePartyLocation(DMContext ctx) {
        super(ctx, "RPG::SavePartyLocation");
    }
}
