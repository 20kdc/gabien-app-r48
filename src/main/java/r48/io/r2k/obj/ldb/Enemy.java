/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;

/**
 * COPY jun6-2017
 */
public class Enemy extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct battlerName = new StringR2kStruct();
    public IntegerR2kStruct battlerHue = new IntegerR2kStruct(0);
    public IntegerR2kStruct maxHp = new IntegerR2kStruct(10);
    public IntegerR2kStruct maxSp = new IntegerR2kStruct(10);
    public IntegerR2kStruct atk = new IntegerR2kStruct(10);
    public IntegerR2kStruct def = new IntegerR2kStruct(10);
    public IntegerR2kStruct spi = new IntegerR2kStruct(10);
    public IntegerR2kStruct agi = new IntegerR2kStruct(10);
    public BooleanR2kStruct battlerBlendMode = new BooleanR2kStruct(false);
    public IntegerR2kStruct exp = new IntegerR2kStruct(0);
    public IntegerR2kStruct gold = new IntegerR2kStruct(0);
    public IntegerR2kStruct dropId = new IntegerR2kStruct(0);
    public IntegerR2kStruct dropProb = new IntegerR2kStruct(100);
    public BooleanR2kStruct criticalHit = new BooleanR2kStruct(false);
    public IntegerR2kStruct criticalHitChance = new IntegerR2kStruct(30);
    public BooleanR2kStruct miss = new BooleanR2kStruct(false);
    public BooleanR2kStruct levitate = new BooleanR2kStruct(false);

    public ArraySizeR2kInterpretable<ByteR2kStruct> stateRanksSz = new ArraySizeR2kInterpretable<ByteR2kStruct>();
    public ArraySetR2kStruct<ByteR2kStruct> stateRanks = new ArraySetR2kStruct<ByteR2kStruct>(stateRanksSz, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(2);
        }
    }, true);
    public ArraySizeR2kInterpretable<ByteR2kStruct> attrRanksSz = new ArraySizeR2kInterpretable<ByteR2kStruct>();
    public ArraySetR2kStruct<ByteR2kStruct> attrRanks = new ArraySetR2kStruct<ByteR2kStruct>(attrRanksSz, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(2);
        }
    }, true);

    public SparseArrayHR2kStruct<EnemyAction> enemyActions = new SparseArrayHR2kStruct<EnemyAction>(new ISupplier<EnemyAction>() {
        @Override
        public EnemyAction get() {
            return new EnemyAction();
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, battlerName, "@battler_name"),
                new Index(0x03, battlerHue, "@battler_hue"),
                new Index(0x04, maxHp, "@max_hp"),
                new Index(0x05, maxSp, "@max_sp"),
                new Index(0x06, atk, "@atk"),
                new Index(0x07, def, "@def"),
                new Index(0x08, spi, "@spi"),
                new Index(0x09, agi, "@agi"),
                new Index(0x0A, battlerBlendMode, "@battler_blend_mode"),
                new Index(0x0B, exp, "@exp"),
                new Index(0x0C, gold, "@gold"),
                new Index(0x0D, dropId, "@drop_item"),
                new Index(0x0E, dropProb, "@drop_percent"),
                new Index(0x15, criticalHit, "@can_crit"),
                new Index(0x16, criticalHitChance, "@crit_chance"),
                new Index(0x1A, miss, "@miss"),
                new Index(0x1C, levitate, "@levitate"),
                new Index(0x1F, stateRanksSz),
                new Index(0x20, stateRanks, "@state_ranks"),
                new Index(0x21, attrRanksSz),
                new Index(0x22, attrRanks, "@element_ranks"),
                new Index(0x2A, enemyActions, "@actions"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Enemy", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
