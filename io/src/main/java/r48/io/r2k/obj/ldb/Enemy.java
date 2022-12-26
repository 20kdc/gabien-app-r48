/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class Enemy extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(0x01) @DM2LcfObject
    public StringR2kStruct name;
    @DM2FXOBinding("@battler_name") @DM2LcfBinding(0x02) @DM2LcfObject
    public StringR2kStruct battlerName;
    @DM2FXOBinding("@battler_hue") @DM2LcfBinding(0x03) @DM2LcfInteger(0)
    public IntegerR2kStruct battlerHue;
    @DM2FXOBinding("@max_hp") @DM2LcfBinding(0x04) @DM2LcfInteger(10)
    public IntegerR2kStruct maxHp;
    @DM2FXOBinding("@max_sp") @DM2LcfBinding(0x05) @DM2LcfInteger(10)
    public IntegerR2kStruct maxSp;
    @DM2FXOBinding("@atk") @DM2LcfBinding(0x06) @DM2LcfInteger(10)
    public IntegerR2kStruct atk;
    @DM2FXOBinding("@def") @DM2LcfBinding(0x07) @DM2LcfInteger(10)
    public IntegerR2kStruct def;
    @DM2FXOBinding("@spi") @DM2LcfBinding(0x08) @DM2LcfInteger(10)
    public IntegerR2kStruct spi;
    @DM2FXOBinding("@agi") @DM2LcfBinding(0x09) @DM2LcfInteger(10)
    public IntegerR2kStruct agi;
    @DM2FXOBinding("@battler_blend_mode") @DM2LcfBinding(0x0A) @DM2LcfBoolean(false)
    public BooleanR2kStruct battlerBlendMode;
    @DM2FXOBinding("@exp") @DM2LcfBinding(0x0B) @DM2LcfInteger(0)
    public IntegerR2kStruct exp;
    @DM2FXOBinding("@gold") @DM2LcfBinding(0x0C) @DM2LcfInteger(0)
    public IntegerR2kStruct gold;
    @DM2FXOBinding("@drop_item") @DM2LcfBinding(0x0D) @DM2LcfInteger(0)
    public IntegerR2kStruct dropId;
    @DM2FXOBinding("@drop_percent") @DM2LcfBinding(0x0E) @DM2LcfInteger(100)
    public IntegerR2kStruct dropProb;
    @DM2FXOBinding("@can_crit") @DM2LcfBinding(0x15) @DM2LcfBoolean(false)
    public BooleanR2kStruct criticalHit;
    @DM2FXOBinding("@crit_chance") @DM2LcfBinding(0x16) @DM2LcfInteger(30)
    public IntegerR2kStruct criticalHitChance;
    @DM2FXOBinding("@miss") @DM2LcfBinding(0x1A) @DM2LcfBoolean(false)
    public BooleanR2kStruct miss;
    @DM2FXOBinding("@levitate") @DM2LcfBinding(0x1C) @DM2LcfBoolean(false)
    public BooleanR2kStruct levitate;

    @DM2FXOBinding("@state_ranks") @DM2LcfSizeBinding(0x1F) @DM2LcfBinding(0x20)
    public DM2ArraySet<ByteR2kStruct> stateRanks;
    @DM2FXOBinding("@attr_ranks") @DM2LcfSizeBinding(0x21) @DM2LcfBinding(0x22)
    public DM2ArraySet<ByteR2kStruct> attrRanks;

    @DM2FXOBinding("@actions") @DM2LcfBinding(0x2A) @DM2LcfSparseArray(EnemyAction.class)
    public DM2SparseArrayH<EnemyAction> enemyActions;

    public Enemy() {
        super("RPG::Enemy");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@state_ranks"))
            return stateRanks = new DM2ArraySet<ByteR2kStruct>() {
                @Override
                public ByteR2kStruct newValue() {
                    return new ByteR2kStruct(2);
                }
            };
        if (sym.equals("@attr_ranks"))
            return attrRanks = new DM2ArraySet<ByteR2kStruct>() {
                @Override
                public ByteR2kStruct newValue() {
                    return new ByteR2kStruct(2);
                }
            };
        return super.dm2AddIVar(sym);
    }
}
