/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import java.util.LinkedList;

import datum.DatumTreeUtils;
import r48.minivm.MVMScope;
import r48.minivm.MVMType;

/**
 * Creates a list from the input expressions.
 * Created 22nd August, 2024
 */
public class MVMCList extends MVMCExpr {
    private final MVMCExpr[] exprs;

    public MVMCList(MVMCExpr... contents) {
        super(MVMType.LIST);
        exprs = contents;
    }

    @Override
    public Object execute(MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        LinkedList<Object> da = new LinkedList<>();
        for (MVMCExpr me : exprs)
            da.add(me.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7));
        return da;
    }

    @Override
    public Object disasm() {
        LinkedList<Object> da = new LinkedList<>();
        da.add(DatumTreeUtils.sym("list"));
        for (MVMCExpr me : exprs)
            da.add(me.disasm());
        return da;
    }

}
