/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import org.eclipse.jdt.annotation.NonNull;

import static gabien.datum.DatumTreeUtils.*;

import r48.io.data.IRIO;
import r48.minivm.MVMScope;
import r48.minivm.MVMU;

/**
 * MiniVM hash stuff
 * Created 14th April 2023.
 */
public final class MVMCDMGetHashDefVal extends MVMCExpr {
    private final MVMCExpr addBase;

    public MVMCDMGetHashDefVal(MVMCExpr addBase) {
        super(addBase.returnType);
        this.addBase = addBase;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        IRIO root = (IRIO) addBase.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        if (root == null)
            return null;
        if (root.getType() != '}')
            return null;
        return root.getHashDefVal();
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("hashDefVal"), addBase.disasm());
    }
}