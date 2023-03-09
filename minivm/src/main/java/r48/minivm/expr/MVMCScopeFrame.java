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
import r48.minivm.MVMU;

/**
 * Used to create a formal stack frame when necessary.
 * Created 1st March 2023.
 */
public class MVMCScopeFrame extends MVMCExpr {
    public final MVMCExpr base;
    public final int frameID;
    public final int size;

    public MVMCScopeFrame(MVMCExpr base, int frameID, int size) {
        this.base = base;
        this.frameID = frameID;
        this.size = size;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        ctx = new MVMScope(ctx, frameID, size);
        return base.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("scopeFrame"), base.disasm(), frameID, size);
    }
}