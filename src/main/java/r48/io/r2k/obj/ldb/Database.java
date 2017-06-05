/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.*;
import r48.io.r2k.obj.ldb.Actor;
import r48.io.r2k.obj.ldb.Skill;
import r48.io.r2k.obj.ldb.Tileset;

/**
 * Bare minimum needed to get ChipSet data out for now
 * Created on 01/06/17.
 */
public class Database extends R2kObject {
    public SparseArrayHR2kStruct<Actor> actors = new SparseArrayHR2kStruct<Actor>(new ISupplier<Actor>() {
        @Override
        public Actor get() {
            return new Actor();
        }
    });
    public SparseArrayHR2kStruct<Skill> skills = new SparseArrayHR2kStruct<Skill>(new ISupplier<Skill>() {
        @Override
        public Skill get() {
            return new Skill();
        }
    });
    public SparseArrayHR2kStruct<Item> items = new SparseArrayHR2kStruct<Item>(new ISupplier<Item>() {
        @Override
        public Item get() {
            return new Item();
        }
    });

    public BlobR2kStruct enemies = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public BlobR2kStruct troops = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public BlobR2kStruct terrains = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public BlobR2kStruct attributes = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public BlobR2kStruct states = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public BlobR2kStruct animations = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));

    public SparseArrayHR2kStruct<Tileset> tilesets = new SparseArrayHR2kStruct<Tileset>(new ISupplier<Tileset>() {
        @Override
        public Tileset get() {
            return new Tileset();
        }
    });

    public BlobR2kStruct terms = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public BlobR2kStruct system = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public SparseArrayAR2kStruct<SVStore> switches = new SparseArrayAR2kStruct<SVStore>(new ISupplier<SVStore>() {
        @Override
        public SVStore get() {
            return new SVStore();
        }
    });
    public SparseArrayAR2kStruct<SVStore> variables = new SparseArrayAR2kStruct<SVStore>(new ISupplier<SVStore>() {
        @Override
        public SVStore get() {
            return new SVStore();
        }
    });
    public BlobR2kStruct commonEvents = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public IntegerR2kStruct dbVersion = new IntegerR2kStruct(0);
    public BlobR2kStruct battleCommands2k3 = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public BlobR2kStruct classes2k3 = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
    public BlobR2kStruct battlerAnimation2k3 = new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));

    @Override
    public boolean terminatable() {
        return true;
    }

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x0B, actors, "@actors"),
                new Index(0x0C, skills, "@skills"),
                new Index(0x0D, items, "@items"),
                // --
                new Index(0x0E, enemies, "@enemies"),
                new Index(0x0F, troops, "@troops"),
                new Index(0x10, terrains, "@terrains"),
                new Index(0x11, attributes, "@attributes"),
                new Index(0x12, states, "@states"),
                new Index(0x13, animations, "@animations"),
                // --
                new Index(0x14, tilesets, "@tilesets"),
                // --
                new Index(0x15, terms, "@terms"),
                new Index(0x16, system, "@system"),
                new Index(0x17, switches, "@switches"),
                new Index(0x18, variables, "@variables"),
                new Index(0x19, commonEvents, "@common_events"),
                new Index(0x1A, dbVersion, "@db_version"),
                new Index(0x1D, battleCommands2k3, "@battle_commands_2k3"),
                new Index(0x1E, classes2k3, "@classes_2k3"),
                new Index(0x20, battlerAnimation2k3, "@battler_anims_2k3"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Database", true);
        asRIOISF(mt);
        return mt;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
