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
import r48.io.r2k.chunks.DoubleR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

public class SaveTitle extends DM2R2kObject {
    @DM2FXOBinding("@timestamp") @DM2LcfBinding(1) @DMCXInteger(0)
    public DoubleR2kStruct timestamp;

    @DM2FXOBinding("@hero_name") @DM2LcfBinding(11) @DMCXObject
    public StringR2kStruct heroName;
    @DM2FXOBinding("@hero_level") @DM2LcfBinding(12) @DMCXInteger(0)
    public IntegerR2kStruct heroLevel;
    @DM2FXOBinding("@hero_hp") @DM2LcfBinding(13) @DMCXInteger(0)
    public IntegerR2kStruct heroHp;

    @DM2FXOBinding("@face1_name") @DM2LcfBinding(21) @DMCXObject
    public StringR2kStruct face1Name;
    @DM2FXOBinding("@face1_index") @DM2LcfBinding(22) @DMCXInteger(0)
    public IntegerR2kStruct face1Idx;
    @DM2FXOBinding("@face2_name") @DM2LcfBinding(23) @DMCXObject
    public StringR2kStruct face2Name;
    @DM2FXOBinding("@face2_index") @DM2LcfBinding(24) @DMCXInteger(0)
    public IntegerR2kStruct face2Idx;
    @DM2FXOBinding("@face3_name") @DM2LcfBinding(25) @DMCXObject
    public StringR2kStruct face3Name;
    @DM2FXOBinding("@face3_index") @DM2LcfBinding(26) @DMCXInteger(0)
    public IntegerR2kStruct face3Idx;
    @DM2FXOBinding("@face4_name") @DM2LcfBinding(27) @DMCXObject
    public StringR2kStruct face4Name;
    @DM2FXOBinding("@face4_index") @DM2LcfBinding(28) @DMCXInteger(0)
    public IntegerR2kStruct face4Idx;

    public SaveTitle(DM2Context ctx) {
        super(ctx, "RPG::SaveTitle");
    }
}
