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
 * Bare minimum needed to get ChipSet data out for now
 * (Later, Jun 6 2017) Ok, THIS class is complete. The others aren't at T.O.W
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

    public SparseArrayHR2kStruct<Enemy> enemies = new SparseArrayHR2kStruct<Enemy>(new ISupplier<Enemy>() {
        @Override
        public Enemy get() {
            return new Enemy();
        }
    });
    public SparseArrayHR2kStruct<Troop> troops = new SparseArrayHR2kStruct<Troop>(new ISupplier<Troop>() {
        @Override
        public Troop get() {
            return new Troop();
        }
    });
    public SparseArrayHR2kStruct<Terrain> terrains = new SparseArrayHR2kStruct<Terrain>(new ISupplier<Terrain>() {
        @Override
        public Terrain get() {
            return new Terrain();
        }
    });
    public SparseArrayHR2kStruct<Attribute> attributes = new SparseArrayHR2kStruct<Attribute>(new ISupplier<Attribute>() {
        @Override
        public Attribute get() {
            return new Attribute();
        }
    });
    public SparseArrayHR2kStruct<State> states = new SparseArrayHR2kStruct<State>(new ISupplier<State>() {
        @Override
        public State get() {
            return new State();
        }
    });
    public SparseArrayHR2kStruct<Animation> animations = new SparseArrayHR2kStruct<Animation>(new ISupplier<Animation>() {
        @Override
        public Animation get() {
            return new Animation();
        }
    });
    public SparseArrayHR2kStruct<Tileset> tilesets = new SparseArrayHR2kStruct<Tileset>(new ISupplier<Tileset>() {
        @Override
        public Tileset get() {
            return new Tileset();
        }
    });
    public Terms terms = new Terms();
    public LdbSystem system = new LdbSystem();
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
    public SparseArrayHR2kStruct<CommonEvent> commonEvents = new SparseArrayHR2kStruct<CommonEvent>(new ISupplier<CommonEvent>() {
        @Override
        public CommonEvent get() {
            return new CommonEvent();
        }
    });
    public ArrayR2kStruct<ByteR2kStruct> dbVersion = new ArrayR2kStruct<ByteR2kStruct>(null, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(0);
        }
    }, true);
    public BattleCommands battleCommands2k3 = new BattleCommands();
    public SparseArrayHR2kStruct<ActorClass> classes2k3 = new SparseArrayHR2kStruct<ActorClass>(new ISupplier<ActorClass>() {
        @Override
        public ActorClass get() {
            return new ActorClass();
        }
    });
    public SparseArrayHR2kStruct<BattlerAnimation> battlerAnimation2k3 = new SparseArrayHR2kStruct<BattlerAnimation>(new ISupplier<BattlerAnimation>() {
        @Override
        public BattlerAnimation get() {
            return new BattlerAnimation();
        }
    });

    @Override
    public boolean terminatable() {
        return true;
    }

    @Override
    public Index[] getIndices() {
        return new Index[] {
                // ONE OF THESE 3
                new Index(0x0B, actors, "@actors"), // <---
                new Index(0x0C, skills, "@skills"),
                new Index(0x0D, items, "@items"),
                new Index(0x0E, enemies, "@enemies"),
                new Index(0x0F, troops, "@troops"),
                new Index(0x10, terrains, "@terrains"),
                new Index(0x11, attributes, "@attributes"),
                new Index(0x12, states, "@states"),
                new Index(0x13, animations, "@animations"),
                new Index(0x14, tilesets, "@tilesets"),
                new Index(0x15, terms, "@terms"),
                new Index(0x16, system, "@system"),

                new Index(0x17, switches, "@switches"),
                new Index(0x18, variables, "@variables"),
                new Index(0x19, commonEvents, "@common_events"),
                new Index(0x1A, dbVersion, "@db_version"),
                new Index(0x1D, battleCommands2k3, "@battle_commands_2k3"),
                new Index(0x1E, classes2k3, "@classes_2k3"),
                new Index(0x20, battlerAnimation2k3, "@battle_anim_sets_2k3"),
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
