/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
import r48.io.r2k.struct.EventCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * COPY jun6-2017
 */
public class Troop extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public SparseArrayAR2kStruct<TroopMember> members = new SparseArrayAR2kStruct<TroopMember>(new ISupplier<TroopMember>() {
        @Override
        public TroopMember get() {
            return new TroopMember();
        }
    });
    public BooleanR2kStruct autoPosition = new BooleanR2kStruct(false);
    public BooleanR2kStruct appearRandomly = new BooleanR2kStruct(false);
    public ArraySizeR2kInterpretable<BooleanR2kStruct> terrainSetSize = new ArraySizeR2kInterpretable<BooleanR2kStruct>();
    public ArraySetR2kStruct<BooleanR2kStruct> terrainSet = new ArraySetR2kStruct<BooleanR2kStruct>(terrainSetSize, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(true);
        }
    }, true);
    public SparseArrayAR2kStruct<TroopPage> pages = new SparseArrayAR2kStruct<TroopPage>(new ISupplier<TroopPage>() {
        @Override
        public TroopPage get() {
            return new TroopPage();
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, members, "@members"),
                new Index(0x03, autoPosition, "@auto_position"),
                new Index(0x04, terrainSetSize),
                new Index(0x05, terrainSet, "@terrain_set"),
                new Index(0x06, appearRandomly, "@randomized_memberset_2k3"),
                new Index(0x0B, pages, "@pages")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Troop", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }

    public static class TroopMember extends R2kObject {
        public IntegerR2kStruct enemyId = new IntegerR2kStruct(1);
        public IntegerR2kStruct x = new IntegerR2kStruct(0);
        public IntegerR2kStruct y = new IntegerR2kStruct(0);
        public BooleanR2kStruct invis = new BooleanR2kStruct(false);
        @Override
        public Index[] getIndices() {
            return new Index[] {
                    new Index(0x01, enemyId, "@enemy"),
                    new Index(0x02, x, "@x"),
                    new Index(0x03, y, "@y"),
                    new Index(0x04, invis, "@invisible")
            };
        }

        @Override
        public RubyIO asRIO() {
            RubyIO rio = new RubyIO().setSymlike("RPG::Troop::Member", true);
            asRIOISF(rio);
            return rio;
        }

        @Override
        public void fromRIO(RubyIO src) {
            fromRIOISF(src);
        }
    }

    public static class TroopPage extends R2kObject {
        public TroopPageCondition condition = new TroopPageCondition();
        public ArraySizeR2kInterpretable<EventCommand> listSize = new ArraySizeR2kInterpretable<EventCommand>();
        public ArrayR2kStruct<EventCommand> list = new ArrayR2kStruct<EventCommand>(listSize, new ISupplier<EventCommand>() {
            @Override
            public EventCommand get() {
                return new EventCommand();
            }
        }, true);

        @Override
        public RubyIO asRIO() {
            RubyIO rio = new RubyIO().setSymlike("RPG::Troop::Page", true);
            asRIOISF(rio);
            return rio;
        }

        @Override
        public void fromRIO(RubyIO src) {
            fromRIOISF(src);
        }

        @Override
        public Index[] getIndices() {
            return new Index[] {
                    new Index(0x02, condition, "@condition"),
                    new Index(0x0B, listSize),
                    new Index(0x0C, list, "@list"),
            };
        }
    }

    public static class TroopPageCondition extends R2kObject {
        public BitfieldR2kStruct flagsA = new BitfieldR2kStruct(new String[] {"@switch_a", "@switch_b", "@variable_>=_val", "@turn", "@fatigue", "@enemy_hp", "@actor_hp", "@turn_enemy_2k3"});
        public BitfieldR2kStruct flagsB = new BitfieldR2kStruct(new String[] {"@turn_actor", "@command_actor"});
        public IR2kInterpretable flags = new IR2kInterpretable() {
            @Override
            public void importData(InputStream bais) throws IOException {
                flagsA.importData(bais);
                if (bais.available() > 0)
                    flagsB.importData(bais);
            }

            @Override
            public boolean exportData(OutputStream baos) throws IOException {
                flagsA.exportData(baos);
                flagsB.exportData(baos);
                return false;
            }
        };
        public IntegerR2kStruct switchAId = new IntegerR2kStruct(1);
        public IntegerR2kStruct switchBId = new IntegerR2kStruct(1);
        public IntegerR2kStruct variableId = new IntegerR2kStruct(1);
        public IntegerR2kStruct variableVal = new IntegerR2kStruct(0);
        public IntegerR2kStruct turnA = new IntegerR2kStruct(0);
        public IntegerR2kStruct turnB = new IntegerR2kStruct(0);
        public IntegerR2kStruct fatigueMin = new IntegerR2kStruct(0);
        public IntegerR2kStruct fatigueMax = new IntegerR2kStruct(100);
        public IntegerR2kStruct enemyId = new IntegerR2kStruct(0);
        public IntegerR2kStruct enemyHpMin = new IntegerR2kStruct(0);
        public IntegerR2kStruct enemyHpMax = new IntegerR2kStruct(100);
        public IntegerR2kStruct actorId = new IntegerR2kStruct(0);
        public IntegerR2kStruct actorHpMin = new IntegerR2kStruct(0);
        public IntegerR2kStruct actorHpMax = new IntegerR2kStruct(100);
        public IntegerR2kStruct turnEnemyId = new IntegerR2kStruct(0);
        public IntegerR2kStruct turnEnemyA = new IntegerR2kStruct(0);
        public IntegerR2kStruct turnEnemyB = new IntegerR2kStruct(0);
        public IntegerR2kStruct turnActorId = new IntegerR2kStruct(1);
        public IntegerR2kStruct turnActorA = new IntegerR2kStruct(0);
        public IntegerR2kStruct turnActorB = new IntegerR2kStruct(0);
        public IntegerR2kStruct commandActorId = new IntegerR2kStruct(1);
        public IntegerR2kStruct commandId = new IntegerR2kStruct(0);

        @Override
        public RubyIO asRIO() {
            RubyIO rio = new RubyIO().setSymlike("RPG::Troop::PageCondition", true);
            rio.addIVar("@flags_a", flagsA.asRIO());
            rio.addIVar("@flags_b_2k3", flagsB.asRIO());
            asRIOISF(rio);
            return rio;
        }

        @Override
        public void fromRIO(RubyIO src) {
            flagsA.fromRIO(src.getInstVarBySymbol("@flags_a"));
            flagsB.fromRIO(src.getInstVarBySymbol("@flags_b_2k3"));
            fromRIOISF(src);
        }

        @Override
        public Index[] getIndices() {
            return new Index[] {
                    new Index(0x01, flags),
                    new Index(0x02, switchAId, "@switch_a_id"),
                    new Index(0x03, switchBId, "@switch_b_id"),
                    new Index(0x04, variableId, "@variable_id"),
                    new Index(0x05, variableVal, "@variable_value"),
                    new Index(0x06, turnA, "@turn_a"),
                    new Index(0x07, turnB, "@turn_b"),
                    new Index(0x08, fatigueMin, "@fatigue_min"),
                    new Index(0x09, fatigueMax, "@fatigue_max"),
                    new Index(0x0A, enemyId, "@enemy_index"),
                    new Index(0x0B, enemyHpMin, "@enemy_hp%_min"),
                    new Index(0x0C, enemyHpMax, "@enemy_hp%_max"),
                    new Index(0x0D, actorId, "@actor_id"),
                    new Index(0x0E, actorHpMin, "@actor_hp%_min"),
                    new Index(0x0F, actorHpMax, "@actor_hp%_max"),
                    new Index(0x10, turnEnemyId, "@turn_enemy_index_2k3"),
                    new Index(0x11, turnEnemyA, "@turn_enemy_a_2k3"),
                    new Index(0x12, turnEnemyB, "@turn_enemy_b_2k3"),
                    new Index(0x13, turnActorId, "@turn_actor_id_2k3"),
                    new Index(0x14, turnActorA, "@turn_actor_a_2k3"),
                    new Index(0x15, turnActorB, "@turn_actor_b_2k3"),
                    new Index(0x16, commandActorId, "@command_actor_id_2k3"),
                    new Index(0x17, commandId, "@command_id_2k3"),
            };
        }
    }
}
