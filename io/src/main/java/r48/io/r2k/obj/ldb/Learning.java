/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 05/06/17.
 */
public class Learning extends DM2R2kObject {
    @DMFXOBinding("@level") @DM2LcfBinding(1) @DMCXInteger(1)
    public IntegerR2kStruct level;
    @DMFXOBinding("@skill") @DM2LcfBinding(2) @DMCXInteger(1)
    public IntegerR2kStruct skill;

    public Learning(DMContext ctx) {
        super(ctx, "RPG::Learning");
    }

    // Skill = Blob;2b 01 04
    public boolean disableSanity() {
        return true;
    }
}
