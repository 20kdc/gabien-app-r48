/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.IRIO;
import r48.io.data.obj.DM2CXSupplier;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BitfieldR2kStruct;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.EventCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * COPY jun6-2017
 */
public class Troop extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DM2FXOBinding("@members") @DM2LcfBinding(2) @DM2CXSupplier(TroopMember.class)
    public DM2SparseArrayA<TroopMember> members;
    @DM2FXOBinding("@auto_position") @DM2LcfBinding(3) @DMCXBoolean(false)
    public BooleanR2kStruct autoPosition;

    @DM2FXOBinding("@terrain_set") @DM2LcfSizeBinding(4) @DM2LcfBinding(5)
    public DM2ArraySet<BooleanR2kStruct> terrainSet;

    @DM2FXOBinding("@randomized_memberset_2k3") @DM2LcfBinding(6) @DMCXBoolean(false)
    public BooleanR2kStruct appearRandomly;

    // Final Tear 3 has these be massive enough that if you try copy/pasting the entire troop database,
    //  R48 will effectively freeze for a while as it unpacks everything. It will then crash, due to an out of memory error.
    @DM2FXOBinding("@pages") @DM2LcfBinding(11) @DM2CXSupplier(TroopPage.class)
    public DM2SparseArrayA<TroopPage> pages;

    public Troop(DM2Context ctx) {
        super(ctx, "RPG::Troop");
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
        @DM2FXOBinding("@enemy") @DM2LcfBinding(1) @DMCXInteger(1)
        public IntegerR2kStruct enemyId;
        @DM2FXOBinding("@x") @DM2LcfBinding(2) @DMCXInteger(0)
        public IntegerR2kStruct x;
        @DM2FXOBinding("@y") @DM2LcfBinding(3) @DMCXInteger(0)
        public IntegerR2kStruct y;
        @DM2FXOBinding("@invisible") @DM2LcfBinding(4) @DMCXBoolean(false)
        public BooleanR2kStruct invis;

        public TroopMember(DM2Context ctx) {
            super(ctx, "RPG::Troop::Member");
        }
    }

    public static class TroopPage extends DM2R2kObject {
        @DM2FXOBinding("@condition") @DM2LcfBinding(2) @DMCXObject
        public TroopPageCondition condition;
        @DM2FXOBinding("@list") @DM2LcfSizeBinding(11) @DM2LcfBinding(12)
        public DM2Array<EventCommand> list;

        public TroopPage(DM2Context ctx) {
            super(ctx, "RPG::Troop::Page");
        }

        @Override
        protected IRIO dm2AddIVar(String sym) {
            if (sym.equals("@list"))
                return list = new DM2Array<EventCommand>() {
                    @Override
                    public EventCommand newValue() {
                        return new EventCommand(context);
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
        @DM2FXOBinding("@switch_a_id") @DM2LcfBinding(0x02) @DMCXInteger(1)
        public IntegerR2kStruct switchAId;
        @DM2FXOBinding("@switch_b_id") @DM2LcfBinding(0x03) @DMCXInteger(1)
        public IntegerR2kStruct switchBId;
        @DM2FXOBinding("@variable_id") @DM2LcfBinding(0x04) @DMCXInteger(1)
        public IntegerR2kStruct variableId;
        @DM2FXOBinding("@variable_value") @DM2LcfBinding(0x05) @DMCXInteger(0)
        public IntegerR2kStruct variableVal;
        @DM2FXOBinding("@turn_a") @DM2LcfBinding(0x06) @DMCXInteger(0)
        public IntegerR2kStruct turnA;
        @DM2FXOBinding("@turn_b") @DM2LcfBinding(0x07) @DMCXInteger(0)
        public IntegerR2kStruct turnB;
        @DM2FXOBinding("@fatigue_min") @DM2LcfBinding(0x08) @DMCXInteger(0)
        public IntegerR2kStruct fatigueMin;
        @DM2FXOBinding("@fatigue_max") @DM2LcfBinding(0x09) @DMCXInteger(100)
        public IntegerR2kStruct fatigueMax;
        @DM2FXOBinding("@enemy_index") @DM2LcfBinding(0x0A) @DMCXInteger(0)
        public IntegerR2kStruct enemyId;
        @DM2FXOBinding("@enemy_hp%_min") @DM2LcfBinding(0x0B) @DMCXInteger(0)
        public IntegerR2kStruct enemyHpMin;
        @DM2FXOBinding("@enemy_hp%_max") @DM2LcfBinding(0x0C) @DMCXInteger(100)
        public IntegerR2kStruct enemyHpMax;
        @DM2FXOBinding("@actor_id") @DM2LcfBinding(0x0D) @DMCXInteger(0)
        public IntegerR2kStruct actorId;
        @DM2FXOBinding("@actor_hp%_min") @DM2LcfBinding(0x0E) @DMCXInteger(0)
        public IntegerR2kStruct actorHpMin;
        @DM2FXOBinding("@actor_hp%_max") @DM2LcfBinding(0x0F) @DMCXInteger(100)
        public IntegerR2kStruct actorHpMax;
        @DM2FXOBinding("@turn_enemy_index_2k3") @DM2LcfBinding(0x10) @DMCXInteger(0)
        public IntegerR2kStruct turnEnemyId;
        @DM2FXOBinding("@turn_enemy_a_2k3") @DM2LcfBinding(0x11) @DMCXInteger(0)
        public IntegerR2kStruct turnEnemyA;
        @DM2FXOBinding("@turn_enemy_b_2k3") @DM2LcfBinding(0x12) @DMCXInteger(0)
        public IntegerR2kStruct turnEnemyB;
        @DM2FXOBinding("@turn_actor_id_2k3") @DM2LcfBinding(0x13) @DMCXInteger(1)
        public IntegerR2kStruct turnActorId;
        @DM2FXOBinding("@turn_actor_a_2k3") @DM2LcfBinding(0x14) @DMCXInteger(0)
        public IntegerR2kStruct turnActorA;
        @DM2FXOBinding("@turn_actor_b_2k3") @DM2LcfBinding(0x15) @DMCXInteger(0)
        public IntegerR2kStruct turnActorB;
        @DM2FXOBinding("@command_actor_id_2k3") @DM2LcfBinding(0x16) @DMCXInteger(1)
        public IntegerR2kStruct commandActorId;
        @DM2FXOBinding("@command_id_2k3") @DM2LcfBinding(0x17) @DMCXInteger(0)
        public IntegerR2kStruct commandId;

        public TroopPageCondition(DM2Context ctx) {
            super(ctx, "RPG::Troop::PageCondition");
        }

        @Override
        protected void dm2UnpackFromMapDestructively(HashMap<Integer, byte[]> pcd) {
            super.dm2UnpackFromMapDestructively(pcd);
            byte[] flags = pcd.remove(1);
            if (flags != null) {
                if (flags.length == 1) {
                    flagsA.importData(flags[0]);
                } else if (flags.length == 2) {
                    flagsA.importData(flags[0]);
                    flagsB.importData(flags[1]);
                } else {
                    throw new RuntimeException("bad flags len " + flags.length);
                }
            }
        }

        @Override
        protected void dm2PackIntoMap(HashMap<Integer, byte[]> pcd) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            flagsA.exportData(baos);
            flagsB.exportData(baos);
            pcd.put(1, baos.toByteArray());
            super.dm2PackIntoMap(pcd);
        }

        @Override
        protected IRIO dm2AddIVar(String sym) {
            if (sym.equals("@flags_a"))
                return flagsA = new BitfieldR2kStruct(new String[] {"@switch_a", "@switch_b", "@variable_>=_val", "@turn", "@fatigue", "@enemy_hp", "@actor_hp", "@turn_enemy_2k3"}, 0);
            if (sym.equals("@flags_b_2k3"))
                return flagsB = new BitfieldR2kStruct(new String[] {"@turn_actor", "@command_actor"}, 0);
            return super.dm2AddIVar(sym);
        }
    }
}
