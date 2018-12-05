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
    @DM2FXOBinding(optional = false, iVar = "@player_map")
    @DM2LcfBinding(index = 1)
    @DM2LcfInteger(0)
    public IntegerR2kStruct playerMap;
    @DM2FXOBinding(optional = false, iVar = "@player_x")
    @DM2LcfBinding(index = 2)
    @DM2LcfInteger(0)
    public IntegerR2kStruct playerX;
    @DM2FXOBinding(optional = false, iVar = "@player_y")
    @DM2LcfBinding(index = 3)
    @DM2LcfInteger(0)
    public IntegerR2kStruct playerY;

    @DM2FXOBinding(optional = true, iVar = "@boat_map")
    @DM2LcfBinding(index = 11)
    @DM2LcfInteger(0)
    public IntegerR2kStruct boatMap;
    @DM2FXOBinding(optional = true, iVar = "@boat_x")
    @DM2LcfBinding(index = 12)
    @DM2LcfInteger(0)
    public IntegerR2kStruct boatX;
    @DM2FXOBinding(optional = true, iVar = "@boat_y")
    @DM2LcfBinding(index = 13)
    @DM2LcfInteger(0)
    public IntegerR2kStruct boatY;

    @DM2FXOBinding(optional = true, iVar = "@ship_map")
    @DM2LcfBinding(index = 21)
    @DM2LcfInteger(0)
    public IntegerR2kStruct shipMap;
    @DM2FXOBinding(optional = true, iVar = "@ship_x")
    @DM2LcfBinding(index = 22)
    @DM2LcfInteger(0)
    public IntegerR2kStruct shipX;
    @DM2FXOBinding(optional = true, iVar = "@ship_y")
    @DM2LcfBinding(index = 23)
    @DM2LcfInteger(0)
    public IntegerR2kStruct shipY;

    @DM2FXOBinding(optional = true, iVar = "@airship_map")
    @DM2LcfBinding(index = 31)
    @DM2LcfInteger(0)
    public IntegerR2kStruct airshipMap;
    @DM2FXOBinding(optional = true, iVar = "@airship_x")
    @DM2LcfBinding(index = 32)
    @DM2LcfInteger(0)
    public IntegerR2kStruct airshipX;
    @DM2FXOBinding(optional = true, iVar = "@airship_y")
    @DM2LcfBinding(index = 33)
    @DM2LcfInteger(0)
    public IntegerR2kStruct airshipY;

    public MapTreeStart() {
        super("RPG::Start");
    }
}
