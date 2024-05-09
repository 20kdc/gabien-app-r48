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
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class Animation extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@animation_name") @DM2LcfBinding(2) @DMCXObject
    public StringR2kStruct animationName;
    @DMFXOBinding("@battle2_2k3") @DM2LcfBinding(3) @DMCXBoolean(false)
    public BooleanR2kStruct unknown3;
    @DMFXOBinding("@timings") @DM2LcfBinding(6) @DMCXSupplier(AnimationTiming.class)
    public DM2SparseArrayA<AnimationTiming> timings;
    @DMFXOBinding("@scope") @DM2LcfBinding(9) @DMCXInteger(0)
    public IntegerR2kStruct scope;
    @DMFXOBinding("@position") @DM2LcfBinding(10) @DMCXInteger(2)
    public IntegerR2kStruct position;
    // Actually a SparseArrayA<AnimationFrame>, but thanks to Final Tear 3, has to be deferred.
    @DMFXOBinding("@frames") @DM2LcfBinding(12) @DMCXSupplier(AnimationFrame.class)
    public DM2SparseArrayA<AnimationFrame> frames;

    public Animation(DMContext ctx) {
        super(ctx, "RPG::Animation");
    }
}
