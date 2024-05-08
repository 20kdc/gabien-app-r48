/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.data.obj.DMCXSupplier;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class BattlerAnimation extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@speed") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct speed;
    @DMFXOBinding("@base_data") @DM2LcfBinding(10) @DMCXSupplier(BAE.class)
    public DM2SparseArrayA<BAE> baseData;
    @DMFXOBinding("@weapon_data") @DM2LcfBinding(11) @DMCXSupplier(BAE.class)
    public DM2SparseArrayA<BAE> weaponData;

    public BattlerAnimation(DMContext ctx) {
        super(ctx, "RPG::BattlerAnimationSet");
    }

    public static class BAE extends DM2R2kObject {
        @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
        public StringR2kStruct name;
        @DMFXOBinding("@battler_name") @DM2LcfBinding(2) @DMCXObject
        public StringR2kStruct battlerName;
        @DMFXOBinding("@battler_index") @DM2LcfBinding(3) @DMCXInteger(0)
        public IntegerR2kStruct battlerIndex;
        @DMFXOBinding("@type") @DM2LcfBinding(4) @DMCXInteger(0)
        public IntegerR2kStruct animationType;
        @DMFXOBinding("@animation_id") @DM2LcfBinding(5) @DMCXInteger(1)
        public IntegerR2kStruct animationId;

        public BAE(DMContext ctx) {
            super(ctx, "RPG::BattlerAnimation");
        }
    }
}
