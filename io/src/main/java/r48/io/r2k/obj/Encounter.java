/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * the world's most boring class
 * Created on 01/06/17.
 */
public class Encounter extends DM2R2kObject {
    @DM2FXOBinding("@troop") @DM2LcfBinding(1) @DMCXInteger(0)
    public IntegerR2kStruct troop;

    public Encounter(DM2Context ctx) {
        super(ctx, "RPG::Encounter");
    }
}
