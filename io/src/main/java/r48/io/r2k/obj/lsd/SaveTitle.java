/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.DoubleR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

public class SaveTitle extends DM2R2kObject {
    @DMFXOBinding("@timestamp") @DM2LcfBinding(1) @DMCXInteger(0)
    public DoubleR2kStruct timestamp;

    @DMFXOBinding("@hero_name") @DM2LcfBinding(11) @DMCXObject
    public StringR2kStruct heroName;
    @DMFXOBinding("@hero_level") @DM2LcfBinding(12) @DMCXInteger(0)
    public IntegerR2kStruct heroLevel;
    @DMFXOBinding("@hero_hp") @DM2LcfBinding(13) @DMCXInteger(0)
    public IntegerR2kStruct heroHp;

    @DMFXOBinding("@face1_name") @DM2LcfBinding(21) @DMCXObject
    public StringR2kStruct face1Name;
    @DMFXOBinding("@face1_index") @DM2LcfBinding(22) @DMCXInteger(0)
    public IntegerR2kStruct face1Idx;
    @DMFXOBinding("@face2_name") @DM2LcfBinding(23) @DMCXObject
    public StringR2kStruct face2Name;
    @DMFXOBinding("@face2_index") @DM2LcfBinding(24) @DMCXInteger(0)
    public IntegerR2kStruct face2Idx;
    @DMFXOBinding("@face3_name") @DM2LcfBinding(25) @DMCXObject
    public StringR2kStruct face3Name;
    @DMFXOBinding("@face3_index") @DM2LcfBinding(26) @DMCXInteger(0)
    public IntegerR2kStruct face3Idx;
    @DMFXOBinding("@face4_name") @DM2LcfBinding(27) @DMCXObject
    public StringR2kStruct face4Name;
    @DMFXOBinding("@face4_index") @DM2LcfBinding(28) @DMCXInteger(0)
    public IntegerR2kStruct face4Idx;

    public SaveTitle(DMContext ctx) {
        super(ctx, "RPG::SaveTitle");
    }
}
