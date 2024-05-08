/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.SVStore;
import r48.io.r2k.struct.Terms;

/**
 * Bare minimum needed to get ChipSet data out for now
 * (Later, Jun 6 2017) Ok, THIS class is complete. The others aren't at T.O.W
 * Created on 01/06/17.
 */
public class Database extends DM2R2kObject {

    public Database(DMContext ctx) {
        super(ctx, "RPG::Database");
    }

    @DMFXOBinding("@actors") @DM2LcfBinding(11) @DMCXSupplier(Actor.class)
    public DM2SparseArrayH<Actor> actors;
    @DMFXOBinding("@skills") @DM2LcfBinding(12) @DMCXSupplier(Skill.class)
    public DM2SparseArrayH<Skill> skills;
    @DMFXOBinding("@items") @DM2LcfBinding(13) @DMCXSupplier(Item.class)
    public DM2SparseArrayH<Item> items;
    @DMFXOBinding("@enemies") @DM2LcfBinding(14) @DMCXSupplier(Enemy.class)
    public DM2SparseArrayH<Enemy> enemies;
    @DMFXOBinding("@troops") @DM2LcfBinding(15) @DMCXSupplier(Troop.class)
    public DM2SparseArrayH<Troop> troops;
    @DMFXOBinding("@terrains") @DM2LcfBinding(16) @DMCXSupplier(Terrain.class)
    public DM2SparseArrayH<Terrain> terrains;
    @DMFXOBinding("@attributes") @DM2LcfBinding(17) @DMCXSupplier(Attribute.class)
    public DM2SparseArrayH<Attribute> attributes;
    @DMFXOBinding("@states") @DM2LcfBinding(18) @DMCXSupplier(State.class)
    public DM2SparseArrayH<State> states;
    @DMFXOBinding("@animations") @DM2LcfBinding(19) @DMCXSupplier(Animation.class)
    public DM2SparseArrayH<Animation> animations;
    @DMFXOBinding("@tilesets") @DM2LcfBinding(20) @DMCXSupplier(Tileset.class)
    public DM2SparseArrayH<Tileset> tilesets;

    @DMFXOBinding("@terms") @DM2LcfBinding(21) @DMCXObject
    public Terms terms;

    // ---
    @DMFXOBinding("@system") @DM2LcfBinding(22) @DMCXObject
    public LdbSystem system;
    // ---

    @DMFXOBinding("@switches") @DM2LcfBinding(23) @DMCXSupplier(SVStore.class)
    public DM2SparseArrayH<SVStore> switches;
    @DMFXOBinding("@variables") @DM2LcfBinding(24) @DMCXSupplier(SVStore.class)
    public DM2SparseArrayH<SVStore> variables;
    @DMFXOBinding("@common_events") @DM2LcfBinding(25) @DMCXSupplier(CommonEvent.class)
    public DM2SparseArrayH<CommonEvent> commonEvents;

    @DMFXOBinding("@db_version") @DM2LcfBinding(26)
    public DM2Array<ByteR2kStruct> dbVersion;

    @DMFXOBinding("@battle_commands_2k3") @DM2LcfBinding(29) @DMCXObject
    public BattleCommands battleCommands2k3;

    @DMFXOBinding("@classes_2k3") @DM2LcfBinding(30) @DMCXSupplier(ActorClass.class)
    public DM2SparseArrayH<ActorClass> classes2k3;
    @DMFXOBinding("@battle_anim_sets_2k3") @DM2LcfBinding(32) @DMCXSupplier(BattlerAnimation.class)
    public DM2SparseArrayH<BattlerAnimation> battlerAnimation2k3;

    @DMOptional @DMFXOBinding("@unused_27") @DM2LcfBinding(27)
    public DM2Array<ByteR2kStruct> unused27;
    @DMOptional @DMFXOBinding("@unused_28") @DM2LcfBinding(28)
    public DM2Array<ByteR2kStruct> unused28;
    @DMOptional @DMFXOBinding("@unused_31") @DM2LcfBinding(31)
    public DM2Array<ByteR2kStruct> unused31;

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@unused_27"))
            return unused27 = newDM2A();
        if (sym.equals("@unused_28"))
            return unused28 = newDM2A();
        if (sym.equals("@unused_31"))
            return unused31 = newDM2A();
        if (sym.equals("@db_version"))
            return dbVersion = newDM2A();
        return super.dm2AddIVar(sym);
    }

    private DM2Array<ByteR2kStruct> newDM2A() {
        return new DM2Array<ByteR2kStruct>(context) {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(context, 0);
            }
        };
    }

    @Override
    public boolean terminatable() {
        return true;
    }
}
