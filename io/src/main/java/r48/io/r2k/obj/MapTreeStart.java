/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Just boring stuff, really...
 * Created on 31/05/17.
 */
public class MapTreeStart extends DM2R2kObject {
    @DMFXOBinding("@player_map") @DM2LcfBinding(1) @DMCXInteger(0)
    public IntegerR2kStruct playerMap;
    @DMFXOBinding("@player_x") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct playerX;
    @DMFXOBinding("@player_y") @DM2LcfBinding(3) @DMCXInteger(0)
    public IntegerR2kStruct playerY;

    @DMFXOBinding("@boat_map") @DM2LcfBinding(11) @DMCXInteger(0)
    public IntegerR2kStruct boatMap;
    @DMFXOBinding("@boat_x") @DM2LcfBinding(12) @DMCXInteger(0)
    public IntegerR2kStruct boatX;
    @DMFXOBinding("@boat_y") @DM2LcfBinding(13) @DMCXInteger(0)
    public IntegerR2kStruct boatY;

    @DMFXOBinding("@ship_map") @DM2LcfBinding(21) @DMCXInteger(0)
    public IntegerR2kStruct shipMap;
    @DMFXOBinding("@ship_x") @DM2LcfBinding(22) @DMCXInteger(0)
    public IntegerR2kStruct shipX;
    @DMFXOBinding("@ship_y") @DM2LcfBinding(23) @DMCXInteger(0)
    public IntegerR2kStruct shipY;

    @DMFXOBinding("@airship_map") @DM2LcfBinding(31) @DMCXInteger(0)
    public IntegerR2kStruct airshipMap;
    @DMFXOBinding("@airship_x") @DM2LcfBinding(32) @DMCXInteger(0)
    public IntegerR2kStruct airshipX;
    @DMFXOBinding("@airship_y") @DM2LcfBinding(33) @DMCXInteger(0)
    public IntegerR2kStruct airshipY;

    public MapTreeStart(DMContext ctx) {
        super(ctx, "RPG::Start");
    }
}
