/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class BattleCommands extends DM2R2kObject {
    @DM2FXOBinding("@auto_placement") @DM2LcfBinding(2) @DM2LcfBoolean(false)
    public BooleanR2kStruct placement;
    @DM2FXOBinding("@death_handler_1") @DM2LcfBinding(4) @DM2LcfInteger(0)
    public IntegerR2kStruct deathHandler1;
    @DM2FXOBinding("@row_back") @DM2LcfBinding(6) @DM2LcfBoolean(false)
    public BooleanR2kStruct row;
    @DM2FXOBinding("@battle_type") @DM2LcfBinding(7) @DM2LcfInteger(0)
    public IntegerR2kStruct battleType;
    @DM2FXOBinding("@commands") @DM2LcfBinding(10) @DM2LcfSparseArrayA(BattleCommand.class)
    public DM2SparseArrayA<BattleCommand> commands;
    @DM2FXOBinding("@death_handler_2") @DM2LcfBinding(15) @DM2LcfInteger(0)
    public IntegerR2kStruct deathHandler2;
    @DM2FXOBinding("@death_event") @DM2LcfBinding(16) @DM2LcfInteger(0)
    public IntegerR2kStruct deathEvent;
    @DM2FXOBinding("@window_small") @DM2LcfBinding(20) @DM2LcfBoolean(false)
    public BooleanR2kStruct windowSize;
    @DM2FXOBinding("@transparent") @DM2LcfBinding(24) @DM2LcfBoolean(false)
    public BooleanR2kStruct transparency;
    @DM2FXOBinding("@death_teleport") @DM2LcfBinding(25) @DM2LcfBoolean(false)
    public BooleanR2kStruct teleport;
    @DM2FXOBinding("@death_teleport_map") @DM2LcfBinding(26) @DM2LcfInteger(0)
    public IntegerR2kStruct teleportId;
    @DM2FXOBinding("@death_teleport_x") @DM2LcfBinding(27) @DM2LcfInteger(0)
    public IntegerR2kStruct teleportX;
    @DM2FXOBinding("@death_teleport_y") @DM2LcfBinding(28) @DM2LcfInteger(0)
    public IntegerR2kStruct teleportY;
    @DM2FXOBinding("@death_teleport_dir") @DM2LcfBinding(29) @DM2LcfInteger(0)
    public IntegerR2kStruct teleportFace;

    public BattleCommands() {
        super("RPG::BattleCommands");
    }

    public static class BattleCommand extends DM2R2kObject {
        @DM2FXOBinding("@name") @DM2LcfBinding(1) @DM2LcfObject
        public StringR2kStruct name;
        @DM2FXOBinding("@type") @DM2LcfBinding(2) @DM2LcfInteger(0)
        public IntegerR2kStruct type;

        public BattleCommand() {
            super("RPG::BattleCommand");
        }
    }
}
