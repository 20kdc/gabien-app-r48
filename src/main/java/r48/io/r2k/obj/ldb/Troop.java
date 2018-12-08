/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
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
    @DM2FXOBinding("@members") @DM2LcfBinding(2) @DM2LcfSparseArray(TroopMember.class)
    public DM2SparseArrayA<TroopMember> members;
    @DM2FXOBinding("@auto_position") @DM2LcfBinding(3) @DM2LcfBoolean(false)
    public BooleanR2kStruct autoPosition;

    @DM2FXOBinding("@terrain_set") @DM2LcfSizeBinding(4) @DM2LcfBinding(5)
    public DM2ArraySet<BooleanR2kStruct> terrainSet;

    @DM2FXOBinding("@randomized_memberset_2k3") @DM2LcfBinding(6) @DM2LcfBoolean(false)
    public BooleanR2kStruct appearRandomly;

    // Final Tear 3 has these be massive enough that if you try copy/pasting the entire troop database,
    //  R48 will effectively freeze for a while as it unpacks everything. It will then crash, due to an out of memory error.
    @DM2FXOBinding("@pages") @DM2LcfBinding(11) @DM2LcfSparseArray(TroopPage.class)
    public DM2SparseArrayA<TroopPage> pages;

    public Troop() {
        super("RPG::Troop");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@terrain_set"))
            return terrainSet = new DM2ArraySet<BooleanR2kStruct>() {
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

    public static class TroopPage extends DM2R2kObject {
        @DM2FXOBinding("@condition") @DM2LcfBinding(2) @DM2LcfObject
        public TroopPageCondition condition;
        @DM2FXOBinding("@list") @DM2LcfSizeBinding(11) @DM2LcfBinding(12) @DM2LcfObject
        public DM2Array<EventCommand> list;

        public TroopPage() {
            super("RPG::Troop::Page");
        }

        @Override
        protected IRIO dm2AddIVar(String sym) {
            if (sym.equals("@condition"))
                return condition = new TroopPageCondition();
            if (sym.equals("@condition"))
                return list = new DM2Array<EventCommand>() {
                    @Override
                    public EventCommand newValue() {
                        return new EventCommand();
                    }
                };
            return super.dm2AddIVar(sym);
        }
    }

    public static class TroopPageCondition extends DM2R2kObject {
        @DM2FXOBinding("@flags_a")
        public BitfieldR2kStruct flagsA;
        @DM2FXOBinding("@flags_b_2k3")
        public BitfieldR2kStruct flagsB;
        @DM2LcfBinding(0x01) @DM2LcfObject
        public FlagsplitInterpretable flags = new FlagsplitInterpretable();
        @DM2FXOBinding("@switch_a_id") @DM2LcfBinding(0x02) @DM2LcfInteger(1)
        public IntegerR2kStruct switchAId;
        @DM2FXOBinding("@switch_b_id") @DM2LcfBinding(0x03) @DM2LcfInteger(1)
        public IntegerR2kStruct switchBId;
        @DM2FXOBinding("@variable_id") @DM2LcfBinding(0x04) @DM2LcfInteger(1)
        public IntegerR2kStruct variableId;
        @DM2FXOBinding("@variable_value") @DM2LcfBinding(0x05) @DM2LcfInteger(0)
        public IntegerR2kStruct variableVal;
        @DM2FXOBinding("@turn_a") @DM2LcfBinding(0x06) @DM2LcfInteger(0)
        public IntegerR2kStruct turnA;
        @DM2FXOBinding("@turn_b") @DM2LcfBinding(0x07) @DM2LcfInteger(0)
        public IntegerR2kStruct turnB;
        @DM2FXOBinding("@fatigue_min") @DM2LcfBinding(0x08) @DM2LcfInteger(0)
        public IntegerR2kStruct fatigueMin;
        @DM2FXOBinding("@fatigue_max") @DM2LcfBinding(0x09) @DM2LcfInteger(100)
        public IntegerR2kStruct fatigueMax;
        @DM2FXOBinding("@enemy_index") @DM2LcfBinding(0x0A) @DM2LcfInteger(0)
        public IntegerR2kStruct enemyId;
        @DM2FXOBinding("@enemy_hp%_min") @DM2LcfBinding(0x0B) @DM2LcfInteger(0)
        public IntegerR2kStruct enemyHpMin;
        @DM2FXOBinding("@enemy_hp%_max") @DM2LcfBinding(0x0C) @DM2LcfInteger(100)
        public IntegerR2kStruct enemyHpMax;
        @DM2FXOBinding("@actor_id") @DM2LcfBinding(0x0D) @DM2LcfInteger(0)
        public IntegerR2kStruct actorId;
        @DM2FXOBinding("@actor_hp%_min") @DM2LcfBinding(0x0E) @DM2LcfInteger(0)
        public IntegerR2kStruct actorHpMin;
        @DM2FXOBinding("@actor_hp%_max") @DM2LcfBinding(0x0F) @DM2LcfInteger(100)
        public IntegerR2kStruct actorHpMax;
        @DM2FXOBinding("@turn_enemy_index_2k3") @DM2LcfBinding(0x10) @DM2LcfInteger(0)
        public IntegerR2kStruct turnEnemyId;
        @DM2FXOBinding("@turn_enemy_a_2k3") @DM2LcfBinding(0x11) @DM2LcfInteger(0)
        public IntegerR2kStruct turnEnemyA;
        @DM2FXOBinding("@turn_enemy_b_2k3") @DM2LcfBinding(0x12) @DM2LcfInteger(0)
        public IntegerR2kStruct turnEnemyB;
        @DM2FXOBinding("@turn_actor_id_2k3") @DM2LcfBinding(0x13) @DM2LcfInteger(1)
        public IntegerR2kStruct turnActorId;
        @DM2FXOBinding("@turn_actor_a_2k3") @DM2LcfBinding(0x14) @DM2LcfInteger(0)
        public IntegerR2kStruct turnActorA;
        @DM2FXOBinding("@turn_actor_b_2k3") @DM2LcfBinding(0x15) @DM2LcfInteger(0)
        public IntegerR2kStruct turnActorB;
        @DM2FXOBinding("@command_actor_id_2k3") @DM2LcfBinding(0x16) @DM2LcfInteger(1)
        public IntegerR2kStruct commandActorId;
        @DM2FXOBinding("@command_id_2k3") @DM2LcfBinding(0x17) @DM2LcfInteger(0)
        public IntegerR2kStruct commandId;

        public TroopPageCondition() {
            super("RPG::Troop::PageCondition");
        }

        @Override
        protected IRIO dm2AddIVar(String sym) {
            if (sym.equals("@flag_a"))
                return flagsA = new BitfieldR2kStruct(new String[] {"@switch_a", "@switch_b", "@variable_>=_val", "@turn", "@fatigue", "@enemy_hp", "@actor_hp", "@turn_enemy_2k3"}, 0);
            if (sym.equals("@flag_b_2k3"))
                return flagsB = new BitfieldR2kStruct(new String[] {"@turn_actor", "@command_actor"}, 0);
            return super.dm2AddIVar(sym);
        }

        private class FlagsplitInterpretable implements IR2kInterpretable {
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
        }
    }
}
