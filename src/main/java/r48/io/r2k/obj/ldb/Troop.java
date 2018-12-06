/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.EventCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * COPY jun6-2017
 */
public class Troop extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DM2LcfObject
    public StringR2kStruct name = new StringR2kStruct();
    @DM2FXOBinding("@members") @DM2LcfBinding(2) @DM2LcfSparseArrayH(TroopMember.class)
    public DM2SparseArrayH<TroopMember> members;
    @DM2FXOBinding("@auto_position") @DM2LcfBinding(3) @DM2LcfBoolean(false)
    public BooleanR2kStruct autoPosition;

    @DM2FXOBinding("@terrain_set") @DM2LcfSizeBinding(4) @DM2LcfBinding(5)
    public DM2Array<BooleanR2kStruct> terrainSet;

    @DM2FXOBinding("@randomized_memberset_2k3") @DM2LcfBinding(6) @DM2LcfBoolean(false)
    public BooleanR2kStruct appearRandomly;

    // Actually a SparseArrayAR2kStruct<TroopPage>, but this is "heavily-deferred",
    //  thanks to Final Tear 3.
    @DM2FXOBinding("@pages") @DM2LcfBinding(11) @DM2LcfCompatArray(TroopPage.class)
    public CompatSparseArrayHR2kStruct<TroopPage> pages;

    public Troop() {
        super("RPG::Troop");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@terrain_set"))
            return terrainSet = new DM2Array<BooleanR2kStruct>() {
                @Override
                public BooleanR2kStruct newValue() {
                    return new BooleanR2kStruct(true);
                }
            };
        return super.dm2AddIVar(sym);
    }

    public static class TroopMember extends DM2R2kObject {
        @DM2FXOBinding("@enemy") @DM2LcfBinding(1) @DM2LcfInteger(1)
        public IntegerR2kStruct enemyId;
        @DM2FXOBinding("@x") @DM2LcfBinding(2) @DM2LcfInteger(0)
        public IntegerR2kStruct x;
        @DM2FXOBinding("@y") @DM2LcfBinding(3) @DM2LcfInteger(0)
        public IntegerR2kStruct y;
        @DM2FXOBinding("@invisible") @DM2LcfBinding(4) @DM2LcfBoolean(false)
        public BooleanR2kStruct invis;

        public TroopMember() {
            super("RPG::Troop::Member");
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
        });

        @Override
        public RubyIO asRIO() {
            RubyIO rio = new RubyIO().setSymlike("RPG::Troop::Page", true);
            asRIOISF(rio);
            return rio;
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
        public BitfieldR2kStruct flagsA = new BitfieldR2kStruct(new String[] {"@switch_a", "@switch_b", "@variable_>=_val", "@turn", "@fatigue", "@enemy_hp", "@actor_hp", "@turn_enemy_2k3"}, 0);
        public BitfieldR2kStruct flagsB = new BitfieldR2kStruct(new String[] {"@turn_actor", "@command_actor"}, 0);
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
        public void fromRIO(IRIO src) {
            flagsA.fromRIO(src.getIVar("@flags_a"));
            flagsB.fromRIO(src.getIVar("@flags_b_2k3"));
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
