/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Battler Animation Data (used by Skill)
 * Created on 06/06/17.
 */
public class BAD extends DM2R2kObject {
    @DM2FXOBinding("@move_type") @DM2LcfBinding(5) @DMCXInteger(0)
    public IntegerR2kStruct moveType;
    @DM2FXOBinding("@has_afterimage") @DM2LcfBinding(6) @DMCXInteger(0)
    public IntegerR2kStruct aiType;
    @DM2FXOBinding("@pose") @DM2LcfBinding(14) @DMCXInteger(-1)
    public IntegerR2kStruct pose;

    public BAD(DM2Context ctx) {
        super(ctx, "RPG::BattlerAnimationData");
    }
}
