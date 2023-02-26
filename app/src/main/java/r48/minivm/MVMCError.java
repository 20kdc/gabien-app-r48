/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import r48.io.data.IRIO;

/**
 * MiniVM error stuff
 * Created 26th February 2023.
 */
public final class MVMCError extends MVMCExpr {
    public final String message;
    public MVMCError(String err) {
        super(true);
        message = err;
    }

    @Override
    public IRIO execute(MVMCContext ctx, IRIO l0, IRIO l1, IRIO l2, IRIO l3, IRIO l4, IRIO l5, IRIO l6, IRIO l7) {
        throw new RuntimeException(message);
    }
}