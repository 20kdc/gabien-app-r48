/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import static gabien.datum.DatumTreeUtils.sym;

import org.eclipse.jdt.annotation.NonNull;

import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;

/**
 * MiniVM error stuff
 * Created 26th February 2023.
 */
public final class MVMCError extends MVMCExpr {
    public final String message;
    public MVMCError(String err) {
        super(MVMType.ANY);
        message = err;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        throw new RuntimeException(message);
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("error"), message);
    }
}