/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.data.DM2Optional;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.chunks.CompatSparseArrayHR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.SVStore;
import r48.io.r2k.struct.Terms;

/**
 * Bare minimum needed to get ChipSet data out for now
 * (Later, Jun 6 2017) Ok, THIS class is complete. The others aren't at T.O.W
 * Created on 01/06/17.
 */
public class Database extends DM2R2kObject {

    public Database() {
        super("RPG::Database");
    }

    @DM2FXOBinding("@actors") @DM2LcfBinding(11) @DM2LcfSparseArrayH(Actor.class)
    public DM2SparseArrayH<Actor> actors;
    @DM2FXOBinding("@skills") @DM2LcfBinding(12) @DM2LcfSparseArrayH(Skill.class)
    public DM2SparseArrayH<Skill> skills;
    @DM2FXOBinding("@items") @DM2LcfBinding(13) @DM2LcfCompatArray(Item.class)
    public CompatSparseArrayHR2kStruct<Item> items;
    @DM2FXOBinding("@enemies") @DM2LcfBinding(14) @DM2LcfCompatArray(Enemy.class)
    public CompatSparseArrayHR2kStruct<Enemy> enemies;
    @DM2FXOBinding("@troops") @DM2LcfBinding(15) @DM2LcfSparseArrayH(Troop.class)
    public DM2SparseArrayH<Troop> troops;
    @DM2FXOBinding("@terrains") @DM2LcfBinding(16) @DM2LcfCompatArray(Terrain.class)
    public CompatSparseArrayHR2kStruct<Terrain> terrains;
    @DM2FXOBinding("@attributes") @DM2LcfBinding(17) @DM2LcfSparseArrayH(Attribute.class)
    public DM2SparseArrayH<Attribute> attributes;
    @DM2FXOBinding("@states") @DM2LcfBinding(18) @DM2LcfCompatArray(State.class)
    public CompatSparseArrayHR2kStruct<State> states;
    @DM2FXOBinding("@animations") @DM2LcfBinding(19) @DM2LcfSparseArrayH(Animation.class)
    public DM2SparseArrayH<Animation> animations;
    @DM2FXOBinding("@tilesets") @DM2LcfBinding(20) @DM2LcfCompatArray(Tileset.class)
    public CompatSparseArrayHR2kStruct<Tileset> tilesets;

    @DM2FXOBinding("@terms") @DM2LcfBinding(21) @DM2LcfObject
    public Terms terms;

    // ---
    @DM2FXOBinding("@system") @DM2LcfBinding(22) @DM2LcfObject
    public LdbSystem system;
    // ---

    @DM2FXOBinding("@switches") @DM2LcfBinding(23) @DM2LcfSparseArrayH(SVStore.class)
    public DM2SparseArrayH<SVStore> switches;
    @DM2FXOBinding("@variables") @DM2LcfBinding(24) @DM2LcfSparseArrayH(SVStore.class)
    public DM2SparseArrayH<SVStore> variables;
    @DM2FXOBinding("@common_events") @DM2LcfBinding(25) @DM2LcfSparseArrayH(CommonEvent.class)
    public DM2SparseArrayH<CommonEvent> commonEvents;

    @DM2FXOBinding("@db_version") @DM2LcfBinding(26)
    public DM2Array<ByteR2kStruct> dbVersion;

    @DM2FXOBinding("@battle_commands_2k3") @DM2LcfBinding(29) @DM2LcfObject
    public BattleCommands battleCommands2k3;

    @DM2FXOBinding("@classes_2k3") @DM2LcfBinding(30) @DM2LcfSparseArrayH(ActorClass.class)
    public DM2SparseArrayH<ActorClass> classes2k3;
    @DM2FXOBinding("@battle_anim_sets_2k3") @DM2LcfBinding(32) @DM2LcfCompatArray(BattlerAnimation.class)
    public CompatSparseArrayHR2kStruct<BattlerAnimation> battlerAnimation2k3;

    @DM2Optional @DM2FXOBinding("@unused_27") @DM2LcfBinding(27)
    public DM2Array<ByteR2kStruct> unused27;
    @DM2Optional @DM2FXOBinding("@unused_28") @DM2LcfBinding(28)
    public DM2Array<ByteR2kStruct> unused28;
    @DM2Optional @DM2FXOBinding("@unused_31") @DM2LcfBinding(31)
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
        return new DM2Array<ByteR2kStruct>() {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(0);
            }
        };
    }

    @Override
    public boolean terminatable() {
        return true;
    }
}
