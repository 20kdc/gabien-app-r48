/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.uslx.append.*;
import r48.io.data.IRIO;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2R2kObject;
import r48.io.r2k.dm2chk.DM2SparseArrayA;

/**
 * Created on 07/06/17.
 */
public class AnimationFrame extends DM2R2kObject {
    @DM2FXOBinding("@cells") @DM2LcfBinding(1)
    public DM2SparseArrayA<AnimationCell> cells;

    public AnimationFrame(DM2Context ctx) {
        super(ctx, "RPG::Animation::Frame");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@cells"))
            return cells = new DM2SparseArrayA<AnimationCell>(new ISupplier<AnimationCell>() {
                @Override
                public AnimationCell get() {
                    return new AnimationCell(context);
                }
            });
        return super.dm2AddIVar(sym);
    }
}
