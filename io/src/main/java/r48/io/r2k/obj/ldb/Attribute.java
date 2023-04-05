/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2Context;
import r48.io.data.DM2FXOBinding;
import r48.io.data.DMCXBoolean;
import r48.io.data.DMCXInteger;
import r48.io.data.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * Progress: Nothing's really changed.
 */
public class Attribute extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DM2FXOBinding("@magical") @DM2LcfBinding(2) @DMCXBoolean(false)
    public BooleanR2kStruct magical;
    @DM2FXOBinding("@a_rate") @DM2LcfBinding(11) @DMCXInteger(300)
    public IntegerR2kStruct aRate;
    @DM2FXOBinding("@b_rate") @DM2LcfBinding(12) @DMCXInteger(200)
    public IntegerR2kStruct bRate;
    @DM2FXOBinding("@c_rate") @DM2LcfBinding(13) @DMCXInteger(100)
    public IntegerR2kStruct cRate;
    @DM2FXOBinding("@d_rate") @DM2LcfBinding(14) @DMCXInteger(50)
    public IntegerR2kStruct dRate;
    @DM2FXOBinding("@e_rate") @DM2LcfBinding(15) @DMCXInteger(0)
    public IntegerR2kStruct eRate;

    public Attribute(DM2Context ctx) {
        super(ctx, "RPG::Attribute");
    }
}
