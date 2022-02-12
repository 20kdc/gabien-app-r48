/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2LcfInteger;
import r48.io.r2k.dm2chk.DM2LcfObject;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Re-copied off of Music on December 6th 2018, about 20 minutes to midnight
 */
public class Sound extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DM2LcfObject
    public StringR2kStruct name;
    @DM2FXOBinding("@volume") @DM2LcfBinding(3) @DM2LcfInteger(100)
    public IntegerR2kStruct volume;
    @DM2FXOBinding("@tempo") @DM2LcfBinding(4) @DM2LcfInteger(100)
    public IntegerR2kStruct tempo;
    @DM2FXOBinding("@balance") @DM2LcfBinding(5) @DM2LcfInteger(50)
    public IntegerR2kStruct balance;

    public Sound() {
        super("RPG::Sound");
    }

    protected Sound(String n) {
        super(n);
    }
}
