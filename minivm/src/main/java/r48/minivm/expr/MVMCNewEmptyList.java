/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import org.eclipse.jdt.annotation.NonNull;

import static gabien.datum.DatumTreeUtils.*;

import java.util.LinkedList;

import r48.minivm.MVMScope;
import r48.minivm.MVMU;

/**
 * MiniVM new empty list
 * Created 2nd March 2023.
 */
public final class MVMCNewEmptyList extends MVMCExpr {
    public MVMCNewEmptyList() {
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        return new LinkedList<Object>();
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("newEmptyList"));
    }
}