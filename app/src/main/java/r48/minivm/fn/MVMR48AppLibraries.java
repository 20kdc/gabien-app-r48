/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import org.eclipse.jdt.annotation.NonNull;

import r48.R48;
import r48.minivm.MVMEnvR48;

/**
 * MiniVM standard library.
 * Created 10th March 2023.
 */
public class MVMR48AppLibraries {
    public static void add(MVMEnvR48 ctx, R48 app) {
        MVMR48GlobalLibraries.add(ctx);
        MVMDMAppLibrary.add(ctx, app);
        ctx.install(new MVMSDBLibrary(app));
        ctx.install(new MVMSDBElementsLibrary(app));
        ctx.install(new MVMSchemaPathLibrary(app));
        ctx.install(new MVMSchemaOpLibrary(app));
        ctx.install(new MVMAppUILibrary(app));
    }
}
