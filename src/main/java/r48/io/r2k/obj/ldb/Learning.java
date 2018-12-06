/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2LcfInteger;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 05/06/17.
 */
public class Learning extends DM2R2kObject {
    @DM2FXOBinding("@level") @DM2LcfBinding(1) @DM2LcfInteger(1)
    public IntegerR2kStruct level;
    @DM2FXOBinding("@skill") @DM2LcfBinding(2) @DM2LcfInteger(1)
    public IntegerR2kStruct skill;

    public Learning() {
        super("RPG::Learning");
    }

    // Skill = Blob;2b 01 04
    public boolean disableSanity() {
        return true;
    }
}
