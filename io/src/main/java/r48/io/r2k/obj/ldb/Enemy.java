/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import java.util.function.Consumer;

import r48.io.data.DMContext;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class Enemy extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(0x01) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@battler_name") @DM2LcfBinding(0x02) @DMCXObject
    public StringR2kStruct battlerName;
    @DMFXOBinding("@battler_hue") @DM2LcfBinding(0x03) @DMCXInteger(0)
    public IntegerR2kStruct battlerHue;
    @DMFXOBinding("@max_hp") @DM2LcfBinding(0x04) @DMCXInteger(10)
    public IntegerR2kStruct maxHp;
    @DMFXOBinding("@max_sp") @DM2LcfBinding(0x05) @DMCXInteger(10)
    public IntegerR2kStruct maxSp;
    @DMFXOBinding("@atk") @DM2LcfBinding(0x06) @DMCXInteger(10)
    public IntegerR2kStruct atk;
    @DMFXOBinding("@def") @DM2LcfBinding(0x07) @DMCXInteger(10)
    public IntegerR2kStruct def;
    @DMFXOBinding("@spi") @DM2LcfBinding(0x08) @DMCXInteger(10)
    public IntegerR2kStruct spi;
    @DMFXOBinding("@agi") @DM2LcfBinding(0x09) @DMCXInteger(10)
    public IntegerR2kStruct agi;
    @DMFXOBinding("@battler_blend_mode") @DM2LcfBinding(0x0A) @DMCXBoolean(false)
    public BooleanR2kStruct battlerBlendMode;
    @DMFXOBinding("@exp") @DM2LcfBinding(0x0B) @DMCXInteger(0)
    public IntegerR2kStruct exp;
    @DMFXOBinding("@gold") @DM2LcfBinding(0x0C) @DMCXInteger(0)
    public IntegerR2kStruct gold;
    @DMFXOBinding("@drop_item") @DM2LcfBinding(0x0D) @DMCXInteger(0)
    public IntegerR2kStruct dropId;
    @DMFXOBinding("@drop_percent") @DM2LcfBinding(0x0E) @DMCXInteger(100)
    public IntegerR2kStruct dropProb;
    @DMFXOBinding("@can_crit") @DM2LcfBinding(0x15) @DMCXBoolean(false)
    public BooleanR2kStruct criticalHit;
    @DMFXOBinding("@crit_chance") @DM2LcfBinding(0x16) @DMCXInteger(30)
    public IntegerR2kStruct criticalHitChance;
    @DMFXOBinding("@miss") @DM2LcfBinding(0x1A) @DMCXBoolean(false)
    public BooleanR2kStruct miss;
    @DMFXOBinding("@levitate") @DM2LcfBinding(0x1C) @DMCXBoolean(false)
    public BooleanR2kStruct levitate;

    @DMFXOBinding("@state_ranks") @DM2LcfSizeBinding(0x1F) @DM2LcfBinding(0x20)
    public DM2ArraySet<ByteR2kStruct> stateRanks;
    public static Consumer<Enemy> stateRanks_add = (v) -> v.stateRanks = v.byteSet();
    @DMFXOBinding("@attr_ranks") @DM2LcfSizeBinding(0x21) @DM2LcfBinding(0x22)
    public DM2ArraySet<ByteR2kStruct> attrRanks;
    public static Consumer<Enemy> attrRanks_add = (v) -> v.attrRanks = v.byteSet();

    @DMFXOBinding("@actions") @DM2LcfBinding(0x2A) @DMCXSupplier(EnemyAction.class)
    public DM2SparseArrayH<EnemyAction> enemyActions;

    public Enemy(DMContext ctx) {
        super(ctx, "RPG::Enemy");
    }

    private DM2ArraySet<ByteR2kStruct> byteSet() {
        return new DM2ArraySet<ByteR2kStruct>(context) {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(context, 2);
            }
        };
    }
}
