/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;

/**
 * COPY jun6-2017
 */
public class BattleCommands extends R2kObject {
    public BooleanR2kStruct placement = new BooleanR2kStruct(false);
    public IntegerR2kStruct deathHandler1 = new IntegerR2kStruct(0);
    public BooleanR2kStruct row = new BooleanR2kStruct(false);
    public IntegerR2kStruct battleType = new IntegerR2kStruct(0);
    public SparseArrayAR2kStruct<BattleCommand> commands = new SparseArrayAR2kStruct<BattleCommand>(new ISupplier<BattleCommand>() {
        @Override
        public BattleCommand get() {
            return new BattleCommand();
        }
    });
    public IntegerR2kStruct deathHandler2 = new IntegerR2kStruct(0);
    public IntegerR2kStruct deathEvent = new IntegerR2kStruct(0);
    public BooleanR2kStruct windowSize = new BooleanR2kStruct(false);
    public BooleanR2kStruct transparency = new BooleanR2kStruct(false);
    public BooleanR2kStruct teleport = new BooleanR2kStruct(false);
    public IntegerR2kStruct teleportId = new IntegerR2kStruct(0);
    public IntegerR2kStruct teleportX = new IntegerR2kStruct(0);
    public IntegerR2kStruct teleportY = new IntegerR2kStruct(0);
    public IntegerR2kStruct teleportFace = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x02, placement, "@auto_placement"),
                new Index(0x04, deathHandler1, "@death_handler_1"),
                new Index(0x06, row, "@row_back"),
                new Index(0x07, battleType, "@battle_type"),
                new Index(0x0A, commands, "@commands"),
                new Index(0x0F, deathHandler2, "@death_handler_2"),
                new Index(0x10, deathEvent, "@death_event"),
                new Index(0x14, windowSize, "@window_large"),
                new Index(0x18, transparency, "@transparent"),
                new Index(0x19, teleport, "@teleport"),
                new Index(0x1A, teleportId, "@teleport_id"),
                new Index(0x1B, teleportX, "@teleport_x"),
                new Index(0x1C, teleportY, "@teleport_y"),
                new Index(0x1D, teleportFace, "@teleport_face"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::BattleCommands", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }

    public static class BattleCommand extends R2kObject {
        public StringR2kStruct name = new StringR2kStruct();
        public IntegerR2kStruct type = new IntegerR2kStruct(0);

        @Override
        public Index[] getIndices() {
            return new Index[] {
                    new Index(0x01, name, "@name"),
                    new Index(0x02, type, "@type")
            };
        }

        @Override
        public RubyIO asRIO() {
            RubyIO rio = new RubyIO().setSymlike("RPG::BattleCommand", true);
            asRIOISF(rio);
            return rio;
        }

        @Override
        public void fromRIO(RubyIO src) {
            fromRIOISF(src);
        }
    }
}
