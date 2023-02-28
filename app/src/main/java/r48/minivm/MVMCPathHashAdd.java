/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import org.eclipse.jdt.annotation.NonNull;

import r48.io.data.IRIO;

/**
 * MiniVM path hash stuff
 * Created 26th February 2023.
 */
public final class MVMCPathHashAdd extends MVMCExpr {
    private final MVMCExpr addBase;
    private final IRIO hashVal;

    public MVMCPathHashAdd(MVMCExpr addBase, IRIO hashVal) {
        super(false);
        this.addBase = addBase;
        this.hashVal = hashVal;
    }

    @Override
    public Object execute(@NonNull MVMCContext ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        IRIO root = (IRIO) addBase.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        if (root == null)
            return null;
        IRIO res = root.getHashVal(hashVal);
        if (res == null)
            res = root.addHashVal(hashVal).setNull();
        return res;
    }
}