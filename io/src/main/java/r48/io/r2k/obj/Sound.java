/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DM2Context;
import r48.io.data.DM2FXOBinding;
import r48.io.data.DMCXInteger;
import r48.io.data.DMCXObject;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Re-copied off of Music on December 6th 2018, about 20 minutes to midnight
 */
public class Sound extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DM2FXOBinding("@volume") @DM2LcfBinding(3) @DMCXInteger(100)
    public IntegerR2kStruct volume;
    @DM2FXOBinding("@tempo") @DM2LcfBinding(4) @DMCXInteger(100)
    public IntegerR2kStruct tempo;
    @DM2FXOBinding("@balance") @DM2LcfBinding(5) @DMCXInteger(50)
    public IntegerR2kStruct balance;

    public Sound(DM2Context ctx) {
        super(ctx, "RPG::Sound");
    }

    protected Sound(DM2Context ctx, String n) {
        super(ctx, n);
    }
}
