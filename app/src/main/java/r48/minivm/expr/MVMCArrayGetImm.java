/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import static gabien.datum.DatumTreeUtils.*;
import r48.io.data.IRIO;
import r48.minivm.MVMScope;

/**
 * MiniVM PathSyntax immediate array value getter.
 * Created 26th February 2023.
 */
public class MVMCArrayGetImm extends MVMCExpr {
    public final MVMCExpr base;
    public final int index;

    public MVMCArrayGetImm(MVMCExpr base, int k) {
        this.base = base;
        index = k;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        IRIO res = (IRIO) base.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        if (res == null)
            return null;
        if (res.getType() != '[')
            return null;
        if (index < 0 || index >= res.getALen())
            return null;
        return res.getAElem(index);
    }

    @Override
    public Object disasm() {
        return Arrays.asList(sym("arrayGetImm"), base.disasm(), index);
    }
}
