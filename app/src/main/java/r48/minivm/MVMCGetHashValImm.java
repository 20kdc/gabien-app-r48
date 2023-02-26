/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import r48.io.data.IRIO;
import r48.io.data.RORIO;

/**
 * MiniVM PathSyntax immediate hash value getter.
 * Created 26th February 2023.
 */
public class MVMCGetHashValImm extends MVMCExpr {
    public final MVMCExpr base;
    public final RORIO key;

    public MVMCGetHashValImm(MVMCExpr base, RORIO k) {
        super(base.isPure);
        this.base = base;
        key = k;
    }

    @Override
    public IRIO execute(MVMCContext ctx, IRIO l0, IRIO l1, IRIO l2, IRIO l3, IRIO l4, IRIO l5, IRIO l6, IRIO l7) {
        IRIO res = base.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        if (res == null)
            return null;
        return res.getHashVal(key);
    }

}
