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
import r48.io.r2k.chunks.BitfieldR2kStruct;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.EventCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * COPY jun6-2017
 */
public class Troop extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@members") @DM2LcfBinding(2) @DMCXSupplier(TroopMember.class)
    public DM2SparseArrayA<TroopMember> members;
    @DMFXOBinding("@auto_position") @DM2LcfBinding(3) @DMCXBoolean(false)
    public BooleanR2kStruct autoPosition;

    @DMFXOBinding("@terrain_set") @DM2LcfSizeBinding(4) @DM2LcfBinding(5)
    public DM2ArraySet<BooleanR2kStruct> terrainSet;
    public static Consumer<Troop> terrainSet_add = (v) -> v.terrainSet = new DM2ArraySet<BooleanR2kStruct>(v.context) {
        @Override
        public BooleanR2kStruct newValue() {
            return new BooleanR2kStruct(v.context, true);
        }
    };

    @DMFXOBinding("@randomized_memberset_2k3") @DM2LcfBinding(6) @DMCXBoolean(false)
    public BooleanR2kStruct appearRandomly;

    // Final Tear 3 has these be massive enough that if you try copy/pasting the entire troop database,
    //  R48 will effectively freeze for a while as it unpacks everything. It will then crash, due to an out of memory error.
    @DMFXOBinding("@pages") @DM2LcfBinding(11) @DMCXSupplier(TroopPage.class)
    public DM2SparseArrayA<TroopPage> pages;

    public Troop(DMContext ctx) {
        super(ctx, "RPG::Troop");
    }

    public static class TroopMember extends DM2R2kObject {
        @DMFXOBinding("@enemy") @DM2LcfBinding(1) @DMCXInteger(1)
        public IntegerR2kStruct enemyId;
        @DMFXOBinding("@x") @DM2LcfBinding(2) @DMCXInteger(0)
        public IntegerR2kStruct x;
        @DMFXOBinding("@y") @DM2LcfBinding(3) @DMCXInteger(0)
        public IntegerR2kStruct y;
        @DMFXOBinding("@invisible") @DM2LcfBinding(4) @DMCXBoolean(false)
        public BooleanR2kStruct invis;

        public TroopMember(DMContext ctx) {
            super(ctx, "RPG::Troop::Member");
        }
    }

    public static class TroopPage extends DM2R2kObject {
        @DMFXOBinding("@condition") @DM2LcfBinding(2) @DMCXObject
        public TroopPageCondition condition;
        @DMFXOBinding("@list") @DM2LcfSizeBinding(11) @DM2LcfBinding(12)
        public DM2Array<EventCommand> list;
        public static Consumer<TroopPage> list_add = (v) -> v.list = new DM2Array<EventCommand>(v.context) {
            @Override
            public EventCommand newValue() {
                return new EventCommand(v.context);
            }
        };

        public TroopPage(DMContext ctx) {
            super(ctx, "RPG::Troop::Page");
        }
    }

    public static class TroopPageCondition extends DM2R2kObject {
        @DMFXOBinding("@flags_a")
        public BitfieldR2kStruct flagsA;
        public static Consumer<TroopPageCondition> flagsA_add = (v) -> v.flagsA = new BitfieldR2kStruct(v.context, new String[] {"@switch_a", "@switch_b", "@variable_>=_val", "@turn", "@fatigue", "@enemy_hp", "@actor_hp", "@turn_enemy_2k3"}, 0);
        @DMFXOBinding("@flags_b_2k3")
        public BitfieldR2kStruct flagsB;
        public static Consumer<TroopPageCondition> flagsB_add = (v) -> v.flagsB = new BitfieldR2kStruct(v.context, new String[] {"@turn_actor", "@command_actor"}, 0);
        @DMFXOBinding("@switch_a_id") @DM2LcfBinding(0x02) @DMCXInteger(1)
        public IntegerR2kStruct switchAId;
        @DMFXOBinding("@switch_b_id") @DM2LcfBinding(0x03) @DMCXInteger(1)
        public IntegerR2kStruct switchBId;
        @DMFXOBinding("@variable_id") @DM2LcfBinding(0x04) @DMCXInteger(1)
        public IntegerR2kStruct variableId;
        @DMFXOBinding("@variable_value") @DM2LcfBinding(0x05) @DMCXInteger(0)
        public IntegerR2kStruct variableVal;
        @DMFXOBinding("@turn_a") @DM2LcfBinding(0x06) @DMCXInteger(0)
        public IntegerR2kStruct turnA;
        @DMFXOBinding("@turn_b") @DM2LcfBinding(0x07) @DMCXInteger(0)
        public IntegerR2kStruct turnB;
        @DMFXOBinding("@fatigue_min") @DM2LcfBinding(0x08) @DMCXInteger(0)
        public IntegerR2kStruct fatigueMin;
        @DMFXOBinding("@fatigue_max") @DM2LcfBinding(0x09) @DMCXInteger(100)
        public IntegerR2kStruct fatigueMax;
        @DMFXOBinding("@enemy_index") @DM2LcfBinding(0x0A) @DMCXInteger(0)
        public IntegerR2kStruct enemyId;
        @DMFXOBinding("@enemy_hp%_min") @DM2LcfBinding(0x0B) @DMCXInteger(0)
        public IntegerR2kStruct enemyHpMin;
        @DMFXOBinding("@enemy_hp%_max") @DM2LcfBinding(0x0C) @DMCXInteger(100)
        public IntegerR2kStruct enemyHpMax;
        @DMFXOBinding("@actor_id") @DM2LcfBinding(0x0D) @DMCXInteger(0)
        public IntegerR2kStruct actorId;
        @DMFXOBinding("@actor_hp%_min") @DM2LcfBinding(0x0E) @DMCXInteger(0)
        public IntegerR2kStruct actorHpMin;
        @DMFXOBinding("@actor_hp%_max") @DM2LcfBinding(0x0F) @DMCXInteger(100)
        public IntegerR2kStruct actorHpMax;
        @DMFXOBinding("@turn_enemy_index_2k3") @DM2LcfBinding(0x10) @DMCXInteger(0)
        public IntegerR2kStruct turnEnemyId;
        @DMFXOBinding("@turn_enemy_a_2k3") @DM2LcfBinding(0x11) @DMCXInteger(0)
        public IntegerR2kStruct turnEnemyA;
        @DMFXOBinding("@turn_enemy_b_2k3") @DM2LcfBinding(0x12) @DMCXInteger(0)
        public IntegerR2kStruct turnEnemyB;
        @DMFXOBinding("@turn_actor_id_2k3") @DM2LcfBinding(0x13) @DMCXInteger(1)
        public IntegerR2kStruct turnActorId;
        @DMFXOBinding("@turn_actor_a_2k3") @DM2LcfBinding(0x14) @DMCXInteger(0)
        public IntegerR2kStruct turnActorA;
        @DMFXOBinding("@turn_actor_b_2k3") @DM2LcfBinding(0x15) @DMCXInteger(0)
        public IntegerR2kStruct turnActorB;
        @DMFXOBinding("@command_actor_id_2k3") @DM2LcfBinding(0x16) @DMCXInteger(1)
        public IntegerR2kStruct commandActorId;
        @DMFXOBinding("@command_id_2k3") @DM2LcfBinding(0x17) @DMCXInteger(0)
        public IntegerR2kStruct commandId;

        public TroopPageCondition(DMContext ctx) {
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
    }
}
