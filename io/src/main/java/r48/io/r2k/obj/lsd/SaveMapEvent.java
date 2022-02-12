/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2LcfBoolean;
import r48.io.r2k.dm2chk.DM2LcfInteger;
import r48.io.r2k.dm2chk.DM2LcfObject;

/**
 * You know exactly why this inheritance is needed
 * Created on December 13th, 2017
 */
public class SaveMapEvent extends SaveCharacter {
    @DM2FXOBinding("@running") @DM2LcfBinding(101) @DM2LcfBoolean(false)
    public BooleanR2kStruct running;
    @DM2FXOBinding("@original_moveroute_index") @DM2LcfBinding(102) @DM2LcfInteger(0)
    public IntegerR2kStruct originalMoveRouteIndex;
    @DM2FXOBinding("@pending") @DM2LcfBinding(103) @DM2LcfBoolean(false)
    public BooleanR2kStruct pending;
    @DM2FXOBinding("@interpreter") @DM2LcfBinding(108) @DM2LcfObject
    public Interpreter interpreter;

    public SaveMapEvent() {
        super("RPG::SaveMapEvent");
    }
}
