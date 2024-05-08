/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DM2CXSupplier;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class BattlerAnimation extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DM2FXOBinding("@speed") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct speed;
    @DM2FXOBinding("@base_data") @DM2LcfBinding(10) @DM2CXSupplier(BAE.class)
    public DM2SparseArrayA<BAE> baseData;
    @DM2FXOBinding("@weapon_data") @DM2LcfBinding(11) @DM2CXSupplier(BAE.class)
    public DM2SparseArrayA<BAE> weaponData;

    public BattlerAnimation(DMContext ctx) {
        super(ctx, "RPG::BattlerAnimationSet");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@base_data"))
            return baseData = genDefault();
        if (sym.equals("@weapon_data"))
            return weaponData = genDefault();
        return super.dm2AddIVar(sym);
    }

    private DM2SparseArrayA<BAE> genDefault() {
        DM2SparseArrayA<BAE> b = new DM2SparseArrayA<BAE>(dm2Ctx, () -> new BAE(dm2Ctx));
        b.arrVal = new IRIO[33];
        for (int i = 0; i < b.arrVal.length; i++)
            b.arrVal[i] = new BAE(dm2Ctx);
        return b;
    }

    public static class BAE extends DM2R2kObject {
        @DM2FXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
        public StringR2kStruct name;
        @DM2FXOBinding("@battler_name") @DM2LcfBinding(2) @DMCXObject
        public StringR2kStruct battlerName;
        @DM2FXOBinding("@battler_index") @DM2LcfBinding(3) @DMCXInteger(0)
        public IntegerR2kStruct battlerIndex;
        @DM2FXOBinding("@type") @DM2LcfBinding(4) @DMCXInteger(0)
        public IntegerR2kStruct animationType;
        @DM2FXOBinding("@animation_id") @DM2LcfBinding(5) @DMCXInteger(1)
        public IntegerR2kStruct animationId;

        public BAE(DMContext ctx) {
            super(ctx, "RPG::BattlerAnimation");
        }
    }
}
