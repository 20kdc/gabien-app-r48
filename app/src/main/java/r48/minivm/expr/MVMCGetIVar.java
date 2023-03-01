/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import static gabien.datum.DatumTreeUtils.sym;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import r48.io.data.IRIO;
import r48.minivm.MVMScope;

/**
 * MiniVM PathSyntax IVar getter.
 * Created 26th February 2023.
 */
public class MVMCGetIVar extends MVMCExpr {
    public final MVMCExpr base;
    public final String key;

    public MVMCGetIVar(MVMCExpr base, String k) {
        this.base = base;
        key = k;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        IRIO res = (IRIO) base.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        if (res == null)
            return null;
        return res.getIVar(key);
    }

    @Override
    public Object disasm() {
        return Arrays.asList(sym("getIVar"), base.disasm(), key);
    }
}
