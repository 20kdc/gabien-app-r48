/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class BattleCommands extends DM2R2kObject {
    @DMFXOBinding("@auto_placement") @DM2LcfBinding(2) @DMCXBoolean(false)
    public BooleanR2kStruct placement;
    @DMFXOBinding("@death_handler_1") @DM2LcfBinding(4) @DMCXInteger(0)
    public IntegerR2kStruct deathHandler1;
    @DMFXOBinding("@row_back") @DM2LcfBinding(6) @DMCXBoolean(false)
    public BooleanR2kStruct row;
    @DMFXOBinding("@battle_type") @DM2LcfBinding(7) @DMCXInteger(0)
    public IntegerR2kStruct battleType;
    @DMFXOBinding("@commands") @DM2LcfBinding(10) @DMCXSupplier(BattleCommand.class)
    public DM2SparseArrayA<BattleCommand> commands;
    @DMFXOBinding("@death_handler_2") @DM2LcfBinding(15) @DMCXInteger(0)
    public IntegerR2kStruct deathHandler2;
    @DMFXOBinding("@death_event") @DM2LcfBinding(16) @DMCXInteger(0)
    public IntegerR2kStruct deathEvent;
    @DMFXOBinding("@window_small") @DM2LcfBinding(20) @DMCXBoolean(false)
    public BooleanR2kStruct windowSize;
    @DMFXOBinding("@transparent") @DM2LcfBinding(24) @DMCXBoolean(false)
    public BooleanR2kStruct transparency;
    @DMFXOBinding("@death_teleport") @DM2LcfBinding(25) @DMCXBoolean(false)
    public BooleanR2kStruct teleport;
    @DMFXOBinding("@death_teleport_map") @DM2LcfBinding(26) @DMCXInteger(0)
    public IntegerR2kStruct teleportId;
    @DMFXOBinding("@death_teleport_x") @DM2LcfBinding(27) @DMCXInteger(0)
    public IntegerR2kStruct teleportX;
    @DMFXOBinding("@death_teleport_y") @DM2LcfBinding(28) @DMCXInteger(0)
    public IntegerR2kStruct teleportY;
    @DMFXOBinding("@death_teleport_dir") @DM2LcfBinding(29) @DMCXInteger(0)
    public IntegerR2kStruct teleportFace;

    public BattleCommands(DMContext ctx) {
        super(ctx, "RPG::BattleCommands");
    }

    public static class BattleCommand extends DM2R2kObject {
        @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
        public StringR2kStruct name;
        @DMFXOBinding("@type") @DM2LcfBinding(2) @DMCXInteger(0)
        public IntegerR2kStruct type;

        public BattleCommand(DMContext ctx) {
            super(ctx, "RPG::BattleCommand");
        }
    }
}
