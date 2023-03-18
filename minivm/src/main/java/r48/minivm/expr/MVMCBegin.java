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
import r48.minivm.compiler.MVMCompileScope;

/**
 * MiniVM begin
 * Created 28th February 2023.
 */
public final class MVMCBegin extends MVMCExpr {
    public final MVMCExpr[] exprs;

    private MVMCBegin(MVMCExpr[] ex) {
        exprs = ex;
    }

    private MVMCBegin(MVMCompileScope cs, Object[] obj, int base, int len) {
        exprs = new MVMCExpr[len];
        for (int i = 0; i < len; i++)
            exprs[i] = cs.compile(obj[base + i]);
    }

    public static MVMCExpr of(MVMCExpr... content) {
        if (content.length == 1)
            return content[0];
        return new MVMCBegin(content);
    }

    public static MVMCExpr of(MVMCompileScope cs, Object[] obj, int base, int len) {
        if (len == 1)
            return cs.compile(obj[base]);
        return new MVMCBegin(cs, obj, base, len);
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        Object val = null;
        for (MVMCExpr ex : exprs)
            val = ex.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        return val;
    }

    @Override
    public Object disasm() {
        LinkedList<Object> obj = new LinkedList<>();
        obj.add(sym("begin"));
        for (MVMCExpr ex : exprs)
            obj.add(ex.disasm());
        return obj;
    }
}