/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import r48.minivm.MVMEnv;

/**
 * Wrapper for non-localizable stuff that still looks like a DynTrSlot.
 * Created 13th March 2023.
 */
public final class NLSTr extends DynTrBase {
    private final Object compiled;
    public NLSTr(@NonNull MVMEnv env, DatumSrcLoc srcLoc, String id, @Nullable DatumSymbol mode, Object v) {
        super(id, srcLoc);
        compiled = compileValue(env, mode, srcLoc, v);
    }

    @Override
    public String toString() {
        return id + "@" + sourceLoc + "(NLS)";
    }

    @Override
    public Object getCompiledValue() {
        return compiled;
    }
}
