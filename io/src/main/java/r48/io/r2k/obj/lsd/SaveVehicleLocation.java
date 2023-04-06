/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;

/**
 * You know exactly why this inheritance is needed
 * Created on December 13th, 2017
 */
public class SaveVehicleLocation extends SaveCharacter {
    @DM2FXOBinding("@vehicle_type") @DM2LcfBinding(101) @DMCXInteger(0)
    public IntegerR2kStruct vehicleType;
    @DM2FXOBinding("@original_moveroute_index") @DM2LcfBinding(102) @DMCXInteger(0)
    public IntegerR2kStruct originalMoverouteIndex;
    @DM2FXOBinding("@remaining_ascent") @DM2LcfBinding(106) @DMCXInteger(0)
    public IntegerR2kStruct remainingAscent;
    @DM2FXOBinding("@remaining_descent") @DM2LcfBinding(107) @DMCXInteger(0)
    public IntegerR2kStruct remainingDescent;
    @DM2FXOBinding("@sprite2_name") @DM2LcfBinding(111) @DMCXObject
    public StringR2kStruct sprite2Name;
    @DM2FXOBinding("@sprite2_index") @DM2LcfBinding(112) @DMCXInteger(0)
    public IntegerR2kStruct sprite2Index;

    public SaveVehicleLocation(DM2Context ctx) {
        super(ctx, "RPG::SaveVehicleLocation");
    }
}
