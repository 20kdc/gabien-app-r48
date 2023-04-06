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
import r48.io.r2k.dm2chk.DM2R2kObject;

public class SaveTarget extends DM2R2kObject {
    @DM2FXOBinding("@map") @DM2LcfBinding(1) @DMCXInteger(0)
    public IntegerR2kStruct map;
    @DM2FXOBinding("@x") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct x;
    @DM2FXOBinding("@y") @DM2LcfBinding(3) @DMCXInteger(0)
    public IntegerR2kStruct y;
    @DM2FXOBinding("@switch_valid") @DM2LcfBinding(4) @DMCXBoolean(false)
    public BooleanR2kStruct switchValid;
    @DM2FXOBinding("@switch_id") @DM2LcfBinding(5) @DMCXInteger(0)
    public IntegerR2kStruct switchId;

    public SaveTarget(DM2Context ctx) {
        super(ctx, "RPG::SaveTarget");
    }
}
