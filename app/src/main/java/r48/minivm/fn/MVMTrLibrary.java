/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import r48.minivm.MVMEnv;
import r48.minivm.MVMU;
import r48.tr.DynTrSlot;
import r48.tr.NLSTr;

/**
 * Translation library.
 * 
 * Created 13th March 2023.
 */
public class MVMTrLibrary {
    public static void add(MVMEnv ctx) {
        ctx.defLib("tr-nls", (a0) -> new NLSTr(MVMU.coerceToString(a0)))
            .attachHelp("(tr-nls VALUE) : Returns a non-localizable constant that still looks like a translation entry.");
        ctx.defLib("tr-set!", (a0, a1) -> {
            ((DynTrSlot) a0).setValue(a1);
            return a1;
        }).attachHelp("(tr-set! DYNTR VALUE) : Compiles a value into a dynamic translation entry. Beware VALUE is unquoted, and tr-set! itself does it's own form of compilation, so writing code directly as VALUE may have unexpected effects.");
    }
}
