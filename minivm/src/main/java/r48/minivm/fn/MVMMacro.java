/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM macro bound into a neat little package.
 * Created 28th February 2023.
 */
public abstract class MVMMacro extends MVMHelpable {
    public MVMMacro(String nh) {
        super(nh);
    }

    public abstract MVMCExpr compile(MVMCompileScope cs, Object[] call);

    @Override
    public String toString() {
        return "macro " + nameHint;
    }
}
