/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2LcfInteger;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Just boring stuff, really...
 * Created on 31/05/17.
 */
public class MapTreeStart extends DM2R2kObject {
    @DM2FXOBinding("@player_map") @DM2LcfBinding(1) @DM2LcfInteger(0)
    public IntegerR2kStruct playerMap;
    @DM2FXOBinding("@player_x") @DM2LcfBinding(2) @DM2LcfInteger(0)
    public IntegerR2kStruct playerX;
    @DM2FXOBinding("@player_y") @DM2LcfBinding(3) @DM2LcfInteger(0)
    public IntegerR2kStruct playerY;

    @DM2FXOBinding("@boat_map") @DM2LcfBinding(11) @DM2LcfInteger(0)
    public IntegerR2kStruct boatMap;
    @DM2FXOBinding("@boat_x") @DM2LcfBinding(12) @DM2LcfInteger(0)
    public IntegerR2kStruct boatX;
    @DM2FXOBinding("@boat_y") @DM2LcfBinding(13) @DM2LcfInteger(0)
    public IntegerR2kStruct boatY;

    @DM2FXOBinding("@ship_map") @DM2LcfBinding(21) @DM2LcfInteger(0)
    public IntegerR2kStruct shipMap;
    @DM2FXOBinding("@ship_x") @DM2LcfBinding(22) @DM2LcfInteger(0)
    public IntegerR2kStruct shipX;
    @DM2FXOBinding("@ship_y") @DM2LcfBinding(23) @DM2LcfInteger(0)
    public IntegerR2kStruct shipY;

    @DM2FXOBinding("@airship_map") @DM2LcfBinding(31) @DM2LcfInteger(0)
    public IntegerR2kStruct airshipMap;
    @DM2FXOBinding("@airship_x") @DM2LcfBinding(32) @DM2LcfInteger(0)
    public IntegerR2kStruct airshipX;
    @DM2FXOBinding("@airship_y") @DM2LcfBinding(33) @DM2LcfInteger(0)
    public IntegerR2kStruct airshipY;

    public MapTreeStart() {
        super("RPG::Start");
    }
}
