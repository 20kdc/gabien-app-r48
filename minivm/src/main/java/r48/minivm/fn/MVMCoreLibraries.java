/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import r48.minivm.MVMEnv;

/**
 * MiniVM standard library.
 * Created 1st March 2023.
 */
public class MVMCoreLibraries {
    public static void add(MVMEnv ctx) {
        MVMScopingLibrary.add(ctx);
        MVMBasicsLibrary.add(ctx);
        MVMExtensionsLibrary.add(ctx);
        MVMMathsLibrary.add(ctx);
        MVMStringLibrary.add(ctx);
        MVMListsLibrary.add(ctx);
    }
}
