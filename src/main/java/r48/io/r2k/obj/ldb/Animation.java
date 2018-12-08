/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * COPY jun6-2017
 */
public class Animation extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DM2LcfObject
    public StringR2kStruct name;
    @DM2FXOBinding("@animation_name") @DM2LcfBinding(2) @DM2LcfObject
    public StringR2kStruct animationName;
    @DM2FXOBinding("@battle2_2k3") @DM2LcfBinding(3) @DM2LcfBoolean(false)
    public BooleanR2kStruct unknown3;
    @DM2FXOBinding("@timings") @DM2LcfBinding(6) @DM2LcfSparseArray(AnimationTiming.class)
    public DM2SparseArrayA<AnimationTiming> timings;
    @DM2FXOBinding("@scope") @DM2LcfBinding(9) @DM2LcfInteger(0)
    public IntegerR2kStruct scope;
    @DM2FXOBinding("@position") @DM2LcfBinding(10) @DM2LcfInteger(2)
    public IntegerR2kStruct position;
    // Actually a SparseArrayA<AnimationFrame>, but thanks to Final Tear 3, has to be deferred.
    @DM2FXOBinding("@frames") @DM2LcfBinding(12) @DM2LcfSparseArray(AnimationFrame.class)
    public DM2SparseArrayA<AnimationFrame> frames;

    public Animation() {
        super("RPG::Animation");
    }
}
