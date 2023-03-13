/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import r48.App;
import r48.minivm.MVMEnv;
import r48.minivm.MVMU;
import r48.tr.NLSTr;

/**
 * Translation library.
 * 
 * Created 13th March 2023.
 */
public class MVMTrLibrary {
    public static void add(MVMEnv ctx, App app) {
        ctx.defLib("tr-nls", (a0) -> new NLSTr(MVMU.coerceToString(a0)))
            .attachHelp("(tr-nls VALUE) : Returns a non-localizable value that still acts like a translation entry.");
    }
}
